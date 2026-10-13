package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.iverksett.TilbakekrevingVedtakDTO;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelseRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.sokos.TilbakekrevingsvedtakRequest;

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

    public TilbakekrevingVedtakDTO lagTilbakekrevingsvedtak(Long behandlingId) {
        var data = lagVedtakData(behandlingId);
        return TilbakekrevingsvedtakMapper.tilDto(data.kravgrunnlag(), data.perioder());
    }

    public TilbakekrevingsvedtakRequest lagSokosTilbakekrevingsvedtak(Long behandlingId) {
        var data = lagVedtakData(behandlingId);
        return TilbakekrevingsvedtakSokosMapper.tilRequest(data.kravgrunnlag(), data.perioder(), data.tillegsfristUtgangspunkt());
    }

    private VedtakData lagVedtakData(Long behandlingId) {
        var kravgrunnlag = kravgrunnlagRepository.finnKravgrunnlag(behandlingId);
        var vurdertForeldelse = vurdertForeldelseRepository.finnVurdertForeldelse(behandlingId).orElse(null);
        var beregningResultat = beregningsresultatTjeneste.finnEllerBeregn(behandlingId);
        var tilbakekrevingPerioder = vedtakPeriodeBeregner.lagTilbakekrevingsPerioder(kravgrunnlag, vurdertForeldelse,
            beregningResultat);
        validerSkattBeløp(tilbakekrevingPerioder);
        var tilleggsfrist = utledTillegsfristUtgangspunkt(vurdertForeldelse);
        return new VedtakData(kravgrunnlag, tilbakekrevingPerioder, tilleggsfrist);
    }

    private record VedtakData(Kravgrunnlag431 kravgrunnlag, List<TilbakekrevingPeriode> perioder, LocalDate tillegsfristUtgangspunkt) {
    }

    private static LocalDate utledTillegsfristUtgangspunkt(VurdertForeldelse vurdertForeldelse) {
        return Optional.ofNullable(vurdertForeldelse).map(VurdertForeldelse::getVurdertForeldelsePerioder).orElse(List.of()).stream()
            .filter(p -> ForeldelseVurderingType.TILLEGGSFRIST.equals(p.getForeldelseVurderingType()))
            .map(VurdertForeldelsePeriode::getOppdagelsesDato)
            .max(Comparator.naturalOrder())
            .orElse(null);

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
