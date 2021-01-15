package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.TilbakekrevingBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.HelgHarYtelsedager;

@ApplicationScoped
public class TilbakekrevingVedtakPeriodeBeregnerProducer {

    private TilbakekrevingBeregningTjeneste beregningTjeneste;


    TilbakekrevingVedtakPeriodeBeregnerProducer() {
        //for CDI proxy
    }

    @Inject
    public TilbakekrevingVedtakPeriodeBeregnerProducer(TilbakekrevingBeregningTjeneste beregningTjeneste) {
        this.beregningTjeneste = beregningTjeneste;
    }

    public TilbakekrevingVedtakPeriodeBeregner lagVedtakPeriodeBeregner(FagsakYtelseType ytelseType) {
        return lagVedtakPeriodeBeregner(HelgHarYtelsedager.helgHarYtelsedager(ytelseType));
    }

    public TilbakekrevingVedtakPeriodeBeregner lagVedtakPeriodeBeregner(boolean helgHarYtelsedager) {
        return new TilbakekrevingVedtakPeriodeBeregner(beregningTjeneste, helgHarYtelsedager);
    }

}
