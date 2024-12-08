package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.time.LocalDateTime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.task.FortsettBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatus437;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatusRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagValidator;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
public class KravVedtakStatusTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(KravVedtakStatusTjeneste.class);

    private KravVedtakStatusRepository kravVedtakStatusRepository;
    private AutomatiskSaksbehandlingVurderingTjeneste halvtRettsgebyrTjeneste;
    private BehandlingRepository behandlingRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private ProsessTaskTjeneste taskTjeneste;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    KravVedtakStatusTjeneste() {
        // for CDI proxy
    }

    @Inject
    public KravVedtakStatusTjeneste(KravVedtakStatusRepository kravVedtakStatusRepository,
                                    AutomatiskSaksbehandlingVurderingTjeneste halvtRettsgebyrTjeneste,
                                    ProsessTaskTjeneste taskTjeneste,
                                    BehandlingRepository behandlingRepository,
                                    KravgrunnlagRepository kravgrunnlagRepository,
                                    BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.kravVedtakStatusRepository = kravVedtakStatusRepository;
        this.halvtRettsgebyrTjeneste = halvtRettsgebyrTjeneste;
        this.taskTjeneste = taskTjeneste;
        this.behandlingRepository = behandlingRepository;
        this.grunnlagRepository = kravgrunnlagRepository;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    public void håndteresMottakAvKravVedtakStatus(Long behandlingId, KravVedtakStatus437 kravVedtakStatus437) {
        var statusKode = kravVedtakStatus437.getKravStatusKode();

        switch (statusKode) {
            case MANUELL, SPERRET -> sperrGrunnlagOgSettPåVent(behandlingId, statusKode);
            case ENDRET -> håndteresEndretStatusMelding(behandlingId, statusKode.getKode());
            case AVSLUTTET -> { } // NOSONAR intentional noop
            default -> throw new TekniskException("FPT-107928",
                String.format("Har fått ugyldig status kode %s fra økonomisystem, kan ikke aksepteres for behandlingId '%s'",
                    statusKode.getKode(), behandlingId));
        }
        kravVedtakStatusRepository.lagre(behandlingId, kravVedtakStatus437);
    }

    private void sperrGrunnlagOgSettPåVent(Long behandlingId, KravStatusKode statusKode) {
        if (grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)) {
            if (!grunnlagRepository.erKravgrunnlagSperret(behandlingId)) {
                sperrGrunnlag(behandlingId);
                settBehandlingPåVent(behandlingId);
            } else {
                LOG.info("Kravgrunnlag for behandling {} er allerede sperret.", behandlingId);
            }
        } else {
            LOG.info("Mottok {} status melding men behandling {} har ikke et krav grunnlag koblet.", statusKode.getKode(), behandlingId);
        }
    }

    private void settBehandlingPåVent(Long behandlingId) {
        var fristDato = LocalDateTime.now().plusMonths(3);
        var behandling = behandlingRepository.hentBehandling(behandlingId);
        behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG,
            BehandlingStegType.TBKGSTEG, fristDato, Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
    }

    private void sperrGrunnlag(Long behandlingId) {
        grunnlagRepository.sperrGrunnlag(behandlingId);
    }

    private void håndteresEndretStatusMelding(Long behandlingId, String statusKode) {
        var saksnummer = behandlingRepository.hentBehandling(behandlingId).getFagsak().getSaksnummer();
        if (grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)) {
            if (!grunnlagRepository.erKravgrunnlagSperret(behandlingId)) {
                throw kanIkkeHåndtereGrunnlagForBehandling(", men eksisterende grunnlag er ikke sperret", statusKode, behandlingId, saksnummer);
            }
            var kravgrunnlag431 = grunnlagRepository.finnKravgrunnlag(behandlingId);
            KravgrunnlagValidator.validerGrunnlag(kravgrunnlag431);

            if (halvtRettsgebyrTjeneste.lavFeilutbetalingKanVentePåAutomatiskBehandling(behandlingId)) {
                // Bli stående på vent til 8 uker.
                var fristDato = AutomatiskSaksbehandlingVurderingTjeneste.ventefristForTilfelleSomKanAutomatiskSaksbehandles(kravgrunnlag431);
                var behandling = behandlingRepository.hentBehandling(behandlingId);
                behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG,
                    BehandlingStegType.TBKGSTEG, fristDato, Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
            } else {
                taBehandlingAvventOgFortsettBehandling(behandlingId);
            }
            grunnlagRepository.opphevGrunnlag(behandlingId);
        } else {
            throw kanIkkeHåndtereGrunnlagForBehandling(", men det finnes ikke noe grunnlag", statusKode, behandlingId, saksnummer);
        }
    }

    static TekniskException kanIkkeHåndtereGrunnlagForBehandling(String suffix, String statusKode, Long behandlingId, Saksnummer saksnummer) {
        var message = String.format("Mottok statusmelding med kode %s fra oppdrag for behandlingId %d, sak %s", statusKode, behandlingId, saksnummer);
        return new TekniskException("FPT-107929", message + suffix);
    }

    private void taBehandlingAvventOgFortsettBehandling(long behandlingId) {
        var behandling = behandlingRepository.hentBehandling(behandlingId);
        var taskData = ProsessTaskData.forProsessTask(FortsettBehandlingTask.class);
        taskData.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getFagsakId(), behandling.getId());
        taskData.setProperty(FortsettBehandlingTask.GJENOPPTA_STEG, behandling.getAktivtBehandlingSteg().getKode());
        taskTjeneste.lagre(taskData);
    }

}
