package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.task.FortsettBehandlingTaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.prosesstask.UtvidetProsessTaskRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatus437;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatusRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagValidator;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class KravVedtakStatusTjeneste {

    private KravVedtakStatusRepository kravVedtakStatusRepository;
    private BehandlingRepository behandlingRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private ProsessTaskRepository prosessTaskRepository;
    private UtvidetProsessTaskRepository utvidetProsessTaskRepository;

    private HenleggBehandlingTjeneste henleggBehandlingTjeneste;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    KravVedtakStatusTjeneste() {
        // for CDI proxy
    }

    @Inject
    public KravVedtakStatusTjeneste(KravVedtakStatusRepository kravVedtakStatusRepository,
                                    ProsessTaskRepository prosessTaskRepository,
                                    UtvidetProsessTaskRepository utvidetProsessTaskRepository,
                                    BehandlingRepositoryProvider repositoryProvider,
                                    HenleggBehandlingTjeneste henleggBehandlingTjeneste,
                                    BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.kravVedtakStatusRepository = kravVedtakStatusRepository;
        this.prosessTaskRepository = prosessTaskRepository;
        this.utvidetProsessTaskRepository = utvidetProsessTaskRepository;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.henleggBehandlingTjeneste = henleggBehandlingTjeneste;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    public void håndteresMottakAvKravVedtakStatus(Long behandlingId, KravVedtakStatus437 kravVedtakStatus437) {
        if (KravStatusKode.AVSLUTTET.equals(kravVedtakStatus437.getKravStatusKode())) {
            henleggBehandlingTjeneste.henleggBehandling(behandlingId, BehandlingResultatType.HENLAGT_KRAVGRUNNLAG_NULLSTILT);
        } else if (KravStatusKode.MANUELL.equals(kravVedtakStatus437.getKravStatusKode()) || KravStatusKode.SPERRET.equals(kravVedtakStatus437.getKravStatusKode())) {
            settBehandlingPåVent(behandlingId);
            sperrGrunnlag(behandlingId);
        } else if (KravStatusKode.ENDRET.equals(kravVedtakStatus437.getKravStatusKode())) {
            håndteresEndretStatusMelding(behandlingId, kravVedtakStatus437.getKravStatusKode().getKode());
        } else {
            throw KravVedtakStatusTjenesteFeil.FACTORY.ugyldigKravStatusKode(kravVedtakStatus437.getKravStatusKode().getKode(), behandlingId).toException();
        }
        kravVedtakStatusRepository.lagre(behandlingId, kravVedtakStatus437);
    }

    private void settBehandlingPåVent(Long behandlingId) {
        LocalDateTime fristDato = LocalDateTime.now().plusMonths(3);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, BehandlingStegType.TBKGSTEG, fristDato, Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
    }

    private void sperrGrunnlag(Long behandlingId) {
        grunnlagRepository.sperrGrunnlag(behandlingId);
    }

    private void håndteresEndretStatusMelding(Long behandlingId, String statusKode) {
        if (grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)) {
            if (!grunnlagRepository.erKravgrunnlagSperret(behandlingId)) {
                throw KravVedtakStatusTjenesteFeil.FACTORY.kanIkkeFinnesSperretGrunnlagForBehandling(statusKode, behandlingId).toException();
            }
            Kravgrunnlag431 kravgrunnlag431 = grunnlagRepository.finnKravgrunnlag(behandlingId);
            KravgrunnlagValidator.validerGrunnlag(kravgrunnlag431);

            taBehandlingAvventOgFortsettBehandling(behandlingId);
            grunnlagRepository.opphevGrunnlag(behandlingId);
        } else {
            throw KravVedtakStatusTjenesteFeil.FACTORY.kanIkkeFinnesSperretGrunnlagForBehandling(statusKode, behandlingId).toException();
        }
    }

    private void taBehandlingAvventOgFortsettBehandling(long behandlingId) {
        String gruppe = "endr-status-melding-" + behandlingId;
        Optional<ProsessTaskData> eksisterendeProsessTask = utvidetProsessTaskRepository.finnSisteProsessTaskForProsessTaskGruppe(FortsettBehandlingTaskProperties.TASKTYPE, gruppe);
        // Det kan finnes flere ENDR status meldinger for en behandling som kan opprette flere FortsettBehandlingTask.
        // Derfor oppretter en sekvens for å kjøre flere prosesstasker i en rekkefølge
        int sekvens = eksisterendeProsessTask.map(pt -> Integer.parseInt(pt.getSekvens()) + 1).orElse(1);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        ProsessTaskData taskData = new ProsessTaskData(FortsettBehandlingTaskProperties.TASKTYPE);
        taskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        taskData.setCallIdFraEksisterende();
        taskData.setProperty(FortsettBehandlingTaskProperties.GJENOPPTA_STEG, behandling.getAktivtBehandlingSteg().getKode());
        taskData.setGruppe(gruppe);
        taskData.setSekvens(String.valueOf(sekvens));
        prosessTaskRepository.lagre(taskData);
    }

    public interface KravVedtakStatusTjenesteFeil extends DeklarerteFeil {

        KravVedtakStatusTjeneste.KravVedtakStatusTjenesteFeil FACTORY = FeilFactory.create(KravVedtakStatusTjeneste.KravVedtakStatusTjenesteFeil.class);

        @TekniskFeil(feilkode = "FPT-107928", feilmelding = "Har fått ugyldig status kode %s fra økonomisystem, kan ikke akspetere for behandlingId '%s'", logLevel = LogLevel.WARN)
        Feil ugyldigKravStatusKode(String status, long behandlingId);

        @TekniskFeil(feilkode = "FPT-107929", feilmelding = "Har fått ENDR status kode %s fra økonomisystem for behandlingId '%s', men ikke finnes sperret grunnlag", logLevel = LogLevel.WARN)
        Feil kanIkkeFinnesSperretGrunnlagForBehandling(String status, long behandlingId);
    }

}
