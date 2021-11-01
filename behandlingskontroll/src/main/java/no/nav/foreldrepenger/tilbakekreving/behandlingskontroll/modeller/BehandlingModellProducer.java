package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.modeller;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;

@ApplicationScoped
public class BehandlingModellProducer {

    @BehandlingTypeRef("BT-007")
    @Produces
    @ApplicationScoped
    public BehandlingModell tilbakekreving() {
        var modellBuilder = BehandlingModellImpl.builder(BehandlingType.TILBAKEKREVING);
        modellBuilder.medSteg(
                BehandlingStegType.INOPPSTEG,
                BehandlingStegType.VARSEL,
                BehandlingStegType.TBKGSTEG,
                BehandlingStegType.FAKTA_VERGE,
                BehandlingStegType.FAKTA_FEILUTBETALING,
                BehandlingStegType.FORELDELSEVURDERINGSTEG,
                BehandlingStegType.VTILBSTEG,
                BehandlingStegType.FORESLÅ_VEDTAK,
                BehandlingStegType.FATTE_VEDTAK,
                BehandlingStegType.IVERKSETT_VEDTAK);
        return modellBuilder.build();
    }

    @BehandlingTypeRef("BT-009")
    @Produces
    @ApplicationScoped
    public BehandlingModell revurdering() {
        var modellBuilder = BehandlingModellImpl.builder(BehandlingType.REVURDERING_TILBAKEKREVING);
        modellBuilder.medSteg(
                BehandlingStegType.HENTGRUNNLAGSTEG,
                BehandlingStegType.FAKTA_VERGE,
                BehandlingStegType.FAKTA_FEILUTBETALING,
                BehandlingStegType.FORELDELSEVURDERINGSTEG,
                BehandlingStegType.VTILBSTEG,
                BehandlingStegType.FORESLÅ_VEDTAK,
                BehandlingStegType.FATTE_VEDTAK,
                BehandlingStegType.IVERKSETT_VEDTAK);
        return modellBuilder.build();
    }

}
