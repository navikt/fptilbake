package no.nav.foreldrepenger.tilbakekreving.behandling.steg.beregn;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;


@BehandlingStegRef(BehandlingStegType.BEREGN)
@BehandlingTypeRef
@ApplicationScoped
public class BeregnSteg implements BehandlingSteg {

    private BeregningsresultatTjeneste beregningsresultatTjeneste;

    BeregnSteg() {
        // for CDI
    }

    @Inject
    public BeregnSteg(BeregningsresultatTjeneste beregningsresultatTjeneste) {
        this.beregningsresultatTjeneste = beregningsresultatTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        beregningsresultatTjeneste.beregnOgLagre(kontekst.getBehandlingId());
        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }

}
