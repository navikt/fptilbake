package no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType.FAKTA_VERGE;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;

@ApplicationScoped
public class VergeTjeneste {

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste;
    private BehandlingRepository behandlingRepository;
    private VergeRepository vergeRepository;
    private HistorikkinnslagRepository historikkRepository;

    VergeTjeneste() {
        // for CDI-proxy
    }

    @Inject
    public VergeTjeneste(BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                         BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste,
                         BehandlingRepositoryProvider repositoryProvider) {
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.behandlingskontrollAsynkTjeneste = behandlingskontrollAsynkTjeneste;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.vergeRepository = repositoryProvider.getVergeRepository();
        this.historikkRepository = repositoryProvider.getHistorikkinnslagRepository();
    }

    public void opprettVergeAksjonspunktOgHoppTilbakeTilFaktaHvisSenereSteg(Behandling behandling) {
        var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(kontekst, List.of(AksjonspunktDefinisjon.AVKLAR_VERGE));
        behandlingskontrollTjeneste.behandlingTilbakeføringHvisTidligereBehandlingSteg(kontekst, FAKTA_VERGE);
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
        behandlingskontrollAsynkTjeneste.asynkProsesserBehandling(behandling);
    }

    public void fjernVergeGrunnlagOgAksjonspunkt(Behandling behandling) {
        var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandling.getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.AVKLAR_VERGE)
                .ifPresent(aksjonspunkt -> behandlingskontrollTjeneste.lagreAksjonspunkterAvbrutt(kontekst, behandling.getAktivtBehandlingSteg(), List.of(aksjonspunkt)));
        vergeRepository.fjernVergeInformasjon(behandling.getId());
        opprettHistorikkinnslagForFjernetVerge(behandling);
        behandlingskontrollAsynkTjeneste.asynkProsesserBehandling(behandling);
    }

    public Optional<VergeEntitet> hentVergeInformasjon(Long behandlingId) {
        return vergeRepository.finnVergeInformasjon(behandlingId);
    }

    private void opprettHistorikkinnslagForFjernetVerge(Behandling behandling) {
        var historikkinnslag = new Historikkinnslag.Builder()
            .medAktør(HistorikkAktør.SAKSBEHANDLER)
            .medFagsakId(behandling.getFagsakId())
            .medBehandlingId(behandling.getId())
            .medTittel("Opplysninger om verge/fullmektig fjernet")
            .build();
        historikkRepository.lagre(historikkinnslag);
    }
}
