package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.iverksett.TilbakekrevingVedtakDTO;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsvedtakDto;

@ApplicationScoped
public class TilbakekrevingsvedtakTjeneste {

    private KravgrunnlagRepository kravgrunnlagRepository;
    private BeregningsresultatTjeneste beregningsresultatTjeneste;
    private TilbakekrevingVedtakPeriodeBeregner vedtakPeriodeBeregner;
    private VurdertForeldelseRepository vurdertForeldelseRepository;

    TilbakekrevingsvedtakTjeneste() {
        // for CDI
    }

    @Inject
    public TilbakekrevingsvedtakTjeneste(KravgrunnlagRepository kravgrunnlagRepository,
                                         BeregningsresultatTjeneste beregningsresultatTjeneste,
                                         TilbakekrevingVedtakPeriodeBeregner vedtakPeriodeBeregner,
                                         VurdertForeldelseRepository vurdertForeldelseRepository) {
        this.kravgrunnlagRepository = kravgrunnlagRepository;
        this.beregningsresultatTjeneste = beregningsresultatTjeneste;
        this.vedtakPeriodeBeregner = vedtakPeriodeBeregner;
        this.vurdertForeldelseRepository = vurdertForeldelseRepository;
    }

    @Deprecated
    public TilbakekrevingsvedtakDto lagTilbakekrevingsvedtak(Long behandlingId) {
        var kravgrunnlag = kravgrunnlagRepository.finnKravgrunnlag(behandlingId);
        var vurdertForeldelse = vurdertForeldelseRepository.finnVurdertForeldelse(behandlingId).orElse(null);
        var beregningResultat = beregningsresultatTjeneste.finnEllerBeregn(behandlingId);
        var tilbakekrevingPerioder = vedtakPeriodeBeregner.lagTilbakekrevingsPerioder(kravgrunnlag, vurdertForeldelse,
            beregningResultat);
        validerSkattBeløp(tilbakekrevingPerioder);
        return TilbakekrevingsvedtakMapper.tilDto(kravgrunnlag, tilbakekrevingPerioder);
    }

    public TilbakekrevingVedtakDTO lagTilbakekrevingsvedtakDTOFpwsproxy(Long behandlingId) {
        var kravgrunnlag = kravgrunnlagRepository.finnKravgrunnlag(behandlingId);
        var vurdertForeldelse = vurdertForeldelseRepository.finnVurdertForeldelse(behandlingId).orElse(null);
        var beregningResultat = beregningsresultatTjeneste.finnEllerBeregn(behandlingId);
        var tilbakekrevingPerioder = vedtakPeriodeBeregner.lagTilbakekrevingsPerioder(kravgrunnlag, vurdertForeldelse,
            beregningResultat);
        validerSkattBeløp(tilbakekrevingPerioder);
        return TilbakekrevingsvedtakMapperFpwsproxy.tilDto(kravgrunnlag, tilbakekrevingPerioder);
    }

    private void validerSkattBeløp(final List<TilbakekrevingPeriode> tilbakekrevingPerioder) {
        var klassekoderSomFeilaktigHarSkattebeløp = tilbakekrevingPerioder.stream()
            .flatMap(periode -> periode.getBeløp().stream())
            .filter(TilbakekrevingBeløp::erIkkeSkattepliktig)
            .filter(beløp -> beløp.getSkattBeløp().compareTo(BigDecimal.ZERO) != 0)
            .map(TilbakekrevingBeløp::getKlassekode)
            .collect(Collectors.toSet());

        if (!klassekoderSomFeilaktigHarSkattebeløp.isEmpty()) {
            throw new IllegalStateException(
                String.format("Skattebeløp for ikke skattepliktige ytelser skal være 0, men var ikke dette for posteringer med klassekode %s"
                    , String.join(", ", klassekoderSomFeilaktigHarSkattebeløp)));
        }
    }
}
