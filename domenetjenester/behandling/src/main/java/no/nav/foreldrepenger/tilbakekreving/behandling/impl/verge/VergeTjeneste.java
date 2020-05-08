package no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType.FAKTA_VERGE;

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

@ApplicationScoped
public class VergeTjeneste {

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;
    private AksjonspunktRepository aksjonspunktRepository;
    private BehandlingRepository behandlingRepository;

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
    }

    public void opprettVergeAksjonspunktOgHoppTilbakeTilFaktaHvisSenereSteg(Behandling behandling) {
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLAR_VERGE, FAKTA_VERGE);
        behandlingskontrollTjeneste.behandlingTilbakeføringHvisTidligereBehandlingSteg(kontekst, FAKTA_VERGE);
        behandlingRepository.lagre(behandling,kontekst.getSkriveLås());
        gjenopptaBehandlingTjeneste.fortsettBehandling(behandling.getId());
    }
}
