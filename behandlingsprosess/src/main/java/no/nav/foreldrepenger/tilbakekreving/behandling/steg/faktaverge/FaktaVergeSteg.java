package no.nav.foreldrepenger.tilbakekreving.behandling.steg.faktaverge;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_VERGE;

import java.util.Collections;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;

@BehandlingStegRef(kode = "FAKTAVERGESTEG")
@BehandlingTypeRef
@ApplicationScoped
public class FaktaVergeSteg implements BehandlingSteg {

    private BehandlingRepository behandlingRepository;

    FaktaVergeSteg() {
        // For CDI
    }

    @Inject
    public FaktaVergeSteg(BehandlingRepository behandlingRepository) {
        this.behandlingRepository = behandlingRepository;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        Optional<Aksjonspunkt> aktivVergeAksjonspunkt = behandling.getÅpneAksjonspunkter().stream()
            .filter(aksjonspunkt -> aksjonspunkt.getAksjonspunktDefinisjon().equals(AVKLAR_VERGE)).findFirst();
        if (aktivVergeAksjonspunkt.isEmpty()) {
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        }
        return BehandleStegResultat.utførtMedAksjonspunkter(Collections.singletonList(AVKLAR_VERGE));
    }
}
