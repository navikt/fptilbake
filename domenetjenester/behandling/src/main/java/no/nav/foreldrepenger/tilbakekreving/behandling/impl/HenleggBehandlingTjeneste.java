package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingFeil;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;

@ApplicationScoped
public class HenleggBehandlingTjeneste {

    private BehandlingRepository behandlingRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    HenleggBehandlingTjeneste() {
        // CDI
    }

    @Inject
    public HenleggBehandlingTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                     BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                     HistorikkinnslagTjeneste historikkinnslagTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();

        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
    }

    public void henleggBehandlingManuelt(long behandlingId, BehandlingResultatType årsakKode, String begrunnelse) {
        if (grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)) {
            throw BehandlingFeil.FACTORY.kanIkkeHenleggeBehandling(behandlingId).toException();
        }
        doHenleggBehandling(behandlingId, årsakKode, begrunnelse);
    }

    public void henleggBehandling(long behandlingId, BehandlingResultatType årsakKode) {
        doHenleggBehandling(behandlingId, årsakKode, null);
    }

    private void doHenleggBehandling(long behandlingId, BehandlingResultatType årsakKode, String begrunnelse) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        if (behandling.isBehandlingPåVent()) {
            behandlingskontrollTjeneste.taBehandlingAvVent(behandling, kontekst);
        }
        behandlingskontrollTjeneste.henleggBehandling(kontekst, årsakKode);

        if (kanSendeHenleggelsebrev(behandling)) {
            sendHenleggelsesbrev();
        }
        opprettHistorikkinnslag(behandling, årsakKode, begrunnelse);
        eksternBehandlingRepository.deaktivateTilkobling(behandlingId);
    }

    private void sendHenleggelsesbrev() {
//        TODO: implementer funksjonalitet for å sende henleggelsebrev
    }

    private boolean kanSendeHenleggelsebrev(Behandling behandling) {
        Optional<Aksjonspunkt> sendVarselAksjonspunkt = behandling.getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.SEND_VARSEL);
        if (sendVarselAksjonspunkt.isEmpty()) {
            return false;
        }
        return AksjonspunktStatus.UTFØRT.equals(sendVarselAksjonspunkt.get().getStatus());
    }

    private void opprettHistorikkinnslag(Behandling behandling, BehandlingResultatType årsakKode, String begrunnelse) {
        if (BehandlingResultatType.HENLAGT_KRAVGRUNNLAG_NULLSTILT.equals(årsakKode)) {
            historikkinnslagTjeneste.opprettHistorikkinnslagForHenleggelse(behandling, HistorikkinnslagType.AVBRUTT_BEH, årsakKode, begrunnelse, HistorikkAktør.VEDTAKSLØSNINGEN);
        } else {
            historikkinnslagTjeneste.opprettHistorikkinnslagForHenleggelse(behandling, HistorikkinnslagType.AVBRUTT_BEH, årsakKode, begrunnelse, HistorikkAktør.SAKSBEHANDLER);
        }

    }
}
