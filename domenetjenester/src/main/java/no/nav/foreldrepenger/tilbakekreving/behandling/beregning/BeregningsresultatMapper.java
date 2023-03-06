package no.nav.foreldrepenger.tilbakekreving.behandling.beregning;

import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat.BeregningsresultatBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat.BeregningsresultatEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat.BeregningsresultatPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;

class BeregningsresultatMapper {
    private BeregningsresultatMapper() {
    }

    public static BeregningResultat map(BeregningsresultatEntitet entitet) {
        VedtakResultatType vedtakResultatType = entitet.getVedtakResultatType();
        List<BeregningResultatPeriode> perioder = entitet.getPerioder().stream().map(BeregningsresultatMapper::map).toList();
        return new BeregningResultat(vedtakResultatType, perioder);
    }

    public static BeregningResultatPeriode map(BeregningsresultatPeriodeEntitet periode) {
        return BeregningResultatPeriode.builder()
            .medPeriode(periode.getPeriode())
            .medTilbakekrevingBeløp(periode.getTilbakekrevingBeløp())
            .medTilbakekrevingBeløpUtenRenter(periode.getTilbakekrevingBeløpUtenRenter())
            .medTilbakekrevingBeløpEtterSkatt(periode.getTilbakekrevingBeløpEtterSkatt())
            .medSkattBeløp(periode.getSkattBeløp())
            .medRenteBeløp(periode.getRenteBeløp())
            .medRenterProsent(periode.getRenterProsent())
            .medFeilutbetaltBeløp(periode.getFeilutbetaltBeløp())
            .medRiktigYtelseBeløp(periode.getRiktigYtelseBeløp())
            .medUtbetaltYtelseBeløp(periode.getUtbetaltYtelseBeløp())
            .build();
    }

    public static BeregningsresultatEntitet map(BeregningResultat resultat) {
        return new BeregningsresultatBuilder()
            .medVedtakResultatType(resultat.getVedtakResultatType())
            .medPerioder(resultat.getBeregningResultatPerioder().stream().map(BeregningsresultatMapper::map).toList())
            .build();
    }

    public static BeregningsresultatPeriodeEntitet map(BeregningResultatPeriode periode) {
        return BeregningsresultatPeriodeEntitet.builder()
            .medPeriode(periode.getPeriode())
            .medTilbakekrevingBeløp(periode.getTilbakekrevingBeløp())
            .medTilbakekrevingBeløpUtenRenter(periode.getTilbakekrevingBeløpUtenRenter())
            .medTilbakekrevingBeløpEtterSkatt(periode.getTilbakekrevingBeløpEtterSkatt())
            .medSkattBeløp(periode.getSkattBeløp())
            .medRenteBeløp(periode.getRenteBeløp())
            .medRenterProsent(periode.getRenterProsent())
            .medFeilutbetaltBeløp(periode.getFeilutbetaltBeløp())
            .medRiktigYtelseBeløp(periode.getRiktigYtelseBeløp())
            .medUtbetaltYtelseBeløp(periode.getUtbetaltYtelseBeløp())
            .build();
    }


}
