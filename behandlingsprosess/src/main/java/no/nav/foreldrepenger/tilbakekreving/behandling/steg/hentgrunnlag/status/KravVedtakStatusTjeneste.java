package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status;

import java.time.LocalDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatus437;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatusRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class KravVedtakStatusTjeneste {

    private KravVedtakStatusRepository kravVedtakStatusRepository;
    private BehandlingRepository behandlingRepository;
    private KravgrunnlagRepository grunnlagRepository;

    private HenleggBehandlingTjeneste henleggBehandlingTjeneste;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    KravVedtakStatusTjeneste() {
        // for CDI proxy
    }

    @Inject
    public KravVedtakStatusTjeneste(KravVedtakStatusRepository kravVedtakStatusRepository, BehandlingRepositoryProvider repositoryProvider,
                                    HenleggBehandlingTjeneste henleggBehandlingTjeneste, BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.kravVedtakStatusRepository = kravVedtakStatusRepository;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.henleggBehandlingTjeneste = henleggBehandlingTjeneste;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    public void håndteresMottakAvKravVedtakStatus(Long behandlingId, KravVedtakStatus437 kravVedtakStatus437) {
        if (KravStatusKode.AVSLUTTET.equals(kravVedtakStatus437.getKravStatusKode())) {
            henleggBehandlingTjeneste.henleggBehandling(behandlingId, BehandlingResultatType.HENLAGT_KRAVGRUNNLAG_NULLSTILT, true);
        } else if (KravStatusKode.MANUELL.equals(kravVedtakStatus437.getKravStatusKode()) || KravStatusKode.SPERRET.equals(kravVedtakStatus437.getKravStatusKode())) {
            settBehandlingPåVent(behandlingId);
            sperrGrunnlag(behandlingId);
        } else {
            throw KravVedtakStatusTjenesteFeil.FACTORY.ugyldigKravStatusKode(kravVedtakStatus437.getKravStatusKode().getKode(), behandlingId).toException();
        }
        kravVedtakStatusRepository.lagre(behandlingId, kravVedtakStatus437);
    }

    private void settBehandlingPåVent(Long behandlingId) {
        LocalDateTime fristDato = FPDateUtil.nå().plusMonths(3);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, BehandlingStegType.TBKGSTEG, fristDato, Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG);
    }

    private void sperrGrunnlag(Long behandlingId) {
        grunnlagRepository.sperrGrunnlag(behandlingId);
    }

    public interface KravVedtakStatusTjenesteFeil extends DeklarerteFeil {

        KravVedtakStatusTjeneste.KravVedtakStatusTjenesteFeil FACTORY = FeilFactory.create(KravVedtakStatusTjeneste.KravVedtakStatusTjenesteFeil.class);

        @TekniskFeil(feilkode = "FPT-107928", feilmelding = "Har fått ugyldig status kode %s fra økonomisystem, kan ikke akspetere for behandlingId '%s'", logLevel = LogLevel.WARN)
        Feil ugyldigKravStatusKode(String status, long behandlingId);
    }

}
