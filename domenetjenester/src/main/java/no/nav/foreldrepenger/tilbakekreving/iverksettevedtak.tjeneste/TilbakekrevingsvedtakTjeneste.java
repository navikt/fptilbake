package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.TilbakekrevingBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsvedtakDto;

@ApplicationScoped
public class TilbakekrevingsvedtakTjeneste {

    private KravgrunnlagRepository kravgrunnlagRepository;
    private TilbakekrevingBeregningTjeneste beregningTjeneste;
    private TilbakekrevingVedtakPeriodeBeregner vedtakPeriodeBeregner;

    TilbakekrevingsvedtakTjeneste() {
        // for CDI
    }

    @Inject
    public TilbakekrevingsvedtakTjeneste(KravgrunnlagRepository kravgrunnlagRepository, TilbakekrevingBeregningTjeneste beregningTjeneste, TilbakekrevingVedtakPeriodeBeregner vedtakPeriodeBeregner) {
        this.kravgrunnlagRepository = kravgrunnlagRepository;
        this.beregningTjeneste = beregningTjeneste;
        this.vedtakPeriodeBeregner = vedtakPeriodeBeregner;
    }

    public TilbakekrevingsvedtakDto lagTilbakekrevingsvedtak(Long behandlingId) {
        Kravgrunnlag431 kravgrunnlag = kravgrunnlagRepository.finnKravgrunnlag(behandlingId);
        BeregningResultat beregningResultat = beregningTjeneste.beregn(behandlingId);
        List<TilbakekrevingPeriode> tilbakekrevingPerioder = vedtakPeriodeBeregner.lagTilbakekrevingsPerioder(kravgrunnlag, beregningResultat);
        return TilbakekrevingsvedtakMapper.tilDto(kravgrunnlag, tilbakekrevingPerioder);
    }
}
