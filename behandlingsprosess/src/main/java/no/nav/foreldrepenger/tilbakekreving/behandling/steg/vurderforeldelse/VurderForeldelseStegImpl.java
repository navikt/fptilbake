package no.nav.foreldrepenger.tilbakekreving.behandling.steg.vurderforeldelse;

import static java.util.Collections.singletonList;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.VurderForeldelseAksjonspunktUtleder;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;

@BehandlingStegRef(kode = "VFORELDETSTEG")
@BehandlingTypeRef
@ApplicationScoped
public class VurderForeldelseStegImpl implements VurderForeldelseSteg {

    private VurderForeldelseAksjonspunktUtleder vurderForeldelseAksjonspunktUtleder;

    VurderForeldelseStegImpl() {
        // For CDI
    }

    @Inject
    public VurderForeldelseStegImpl(VurderForeldelseAksjonspunktUtleder vurderForeldelseAksjonspunktUtleder) {
        this.vurderForeldelseAksjonspunktUtleder = vurderForeldelseAksjonspunktUtleder;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Optional<AksjonspunktDefinisjon> aksjonspunktDefinisjon = vurderForeldelseAksjonspunktUtleder.utledAksjonspunkt(kontekst.getBehandlingId());
        return aksjonspunktDefinisjon.map(ap -> BehandleStegResultat.utførtMedAksjonspunkter(singletonList(ap)))
                .orElseGet(BehandleStegResultat::utførtUtenAksjonspunkter);
    }
}
