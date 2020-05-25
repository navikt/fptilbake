package no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType.FAKTA_VERGE;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;

@ApplicationScoped
public class VergeTjeneste {

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;
    private AksjonspunktRepository aksjonspunktRepository;
    private BehandlingRepository behandlingRepository;
    private VergeRepository vergeRepository;
    private HistorikkRepository historikkRepository;

    VergeTjeneste() {
        // for CDI-proxy
    }

    @Inject
    public VergeTjeneste(BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                         GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste,
                         BehandlingRepositoryProvider repositoryProvider) {
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.vergeRepository = repositoryProvider.getVergeRepository();
        this.historikkRepository = repositoryProvider.getHistorikkRepository();
    }

    public void opprettVergeAksjonspunktOgHoppTilbakeTilFaktaHvisSenereSteg(Behandling behandling) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLAR_VERGE, FAKTA_VERGE);
        behandlingskontrollTjeneste.behandlingTilbakeføringHvisTidligereBehandlingSteg(kontekst, FAKTA_VERGE);
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
        gjenopptaBehandlingTjeneste.fortsettBehandling(behandling.getId());
    }

    public void fjernVergeGrunnlagOgAksjonspunkt(Behandling behandling) {
        behandling.getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.AVKLAR_VERGE).ifPresent(aksjonspunkt -> aksjonspunktRepository.setTilAvbrutt(aksjonspunkt));
        vergeRepository.fjernVergeInformasjon(behandling.getId());
        opprettHistorikkinnslagForFjernetVerge(behandling);
        gjenopptaBehandlingTjeneste.fortsettBehandling(behandling.getId());
    }

    public Optional<VergeEntitet> hentVergeInformasjon(Long behandlingId){
        return vergeRepository.finnVergeInformasjon(behandlingId);
    }

    private void opprettHistorikkinnslagForFjernetVerge(Behandling behandling) {
        HistorikkInnslagTekstBuilder historikkInnslagTekstBuilder = new HistorikkInnslagTekstBuilder()
            .medHendelse(HistorikkinnslagType.FJERNET_VERGE);
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setAktør(HistorikkAktør.SAKSBEHANDLER);
        historikkinnslag.setType(HistorikkinnslagType.FJERNET_VERGE);
        historikkinnslag.setBehandling(behandling);
        historikkInnslagTekstBuilder.build(historikkinnslag);
        historikkRepository.lagre(historikkinnslag);
    }
}
