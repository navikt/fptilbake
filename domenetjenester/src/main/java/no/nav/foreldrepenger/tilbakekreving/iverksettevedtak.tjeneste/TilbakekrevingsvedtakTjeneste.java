package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.TilbakekrevingBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsvedtakDto;

@ApplicationScoped
public class TilbakekrevingsvedtakTjeneste {

    private BehandlingRepository behandlingRepository;
    private KravgrunnlagRepository kravgrunnlagRepository;
    private TilbakekrevingBeregningTjeneste beregningTjeneste;
    private TilbakekrevingVedtakPeriodeBeregnerProducer vedtakPeriodeBeregnerProducer;

    TilbakekrevingsvedtakTjeneste() {
        // for CDI
    }

    @Inject
    public TilbakekrevingsvedtakTjeneste(BehandlingRepository behandlingRepository, KravgrunnlagRepository kravgrunnlagRepository, TilbakekrevingBeregningTjeneste beregningTjeneste, TilbakekrevingVedtakPeriodeBeregnerProducer vedtakPeriodeBeregnerProducer) {
        this.behandlingRepository = behandlingRepository;
        this.kravgrunnlagRepository = kravgrunnlagRepository;
        this.beregningTjeneste = beregningTjeneste;
        this.vedtakPeriodeBeregnerProducer = vedtakPeriodeBeregnerProducer;
    }

    public TilbakekrevingsvedtakDto lagTilbakekrevingsvedtak(Long behandlingId) {
        Kravgrunnlag431 kravgrunnlag = kravgrunnlagRepository.finnKravgrunnlag(behandlingId);
        BeregningResultat beregningResultat = beregningTjeneste.beregn(behandlingId);
        FagsakYtelseType fagsakYtelseType = behandlingRepository.hentBehandling(behandlingId).getFagsak().getFagsakYtelseType();
        TilbakekrevingVedtakPeriodeBeregner vedtakPeriodeBeregner = vedtakPeriodeBeregnerProducer.lagVedtakPeriodeBeregner(fagsakYtelseType);
        List<TilbakekrevingPeriode> tilbakekrevingPerioder = vedtakPeriodeBeregner.lagTilbakekrevingsPerioder(kravgrunnlag, beregningResultat);
        return TilbakekrevingsvedtakMapper.tilDto(kravgrunnlag, tilbakekrevingPerioder);
    }
}
