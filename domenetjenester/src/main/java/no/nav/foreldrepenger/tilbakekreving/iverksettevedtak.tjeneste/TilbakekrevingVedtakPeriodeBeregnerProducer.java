package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.TilbakekrevingBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;

@ApplicationScoped
public class TilbakekrevingVedtakPeriodeBeregnerProducer {

    private TilbakekrevingBeregningTjeneste beregningTjeneste;

    @Inject
    public TilbakekrevingVedtakPeriodeBeregnerProducer(TilbakekrevingBeregningTjeneste beregningTjeneste) {
        this.beregningTjeneste = beregningTjeneste;
    }

    public TilbakekrevingVedtakPeriodeBeregner lagVedtakPeriodeBeregner(FagsakYtelseType ytelseType) {
        return lagVedtakPeriodeBeregner(ytelseType == FagsakYtelseType.OMSORGSPENGER || ytelseType == FagsakYtelseType.ENGANGSTØNAD);
    }

    public TilbakekrevingVedtakPeriodeBeregner lagVedtakPeriodeBeregner(boolean helgHarYtelsedager) {
        return new TilbakekrevingVedtakPeriodeBeregner(beregningTjeneste, helgHarYtelsedager);
    }

}
