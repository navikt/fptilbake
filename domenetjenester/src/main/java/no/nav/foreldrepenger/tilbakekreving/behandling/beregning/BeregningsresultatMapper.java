package no.nav.foreldrepenger.tilbakekreving.behandling.beregning;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat.BeregningsresultatBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat.BeregningsresultatEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat.BeregningsresultatPeriodeEntitet;

class BeregningsresultatMapper {
    private BeregningsresultatMapper() {
    }

    public static BeregningResultat map(BeregningsresultatEntitet entitet) {
        BeregningResultat resultat = new BeregningResultat();
        resultat.setVedtakResultatType(entitet.getVedtakResultatType());
        resultat.setBeregningResultatPerioder(entitet.getPerioder().stream().map(BeregningsresultatMapper::map).toList());
        return resultat;
    }

    public static BeregningResultatPeriode map(BeregningsresultatPeriodeEntitet periode) {
        return BeregningResultatPeriode.builder()
            .medPeriode(periode.getPeriode())
            .medTilbakekrevingBeløp(periode.getTilbakekrevingBeløp())
            .medTilbakekrevingBeløpUtenRenter(periode.getTilbakekrevingBeløpUtenRenter())
            .medTilbakekrevingBeløpEtterSkatt(periode.getTilbakekrevingBeløpEtterSkatt())
            .medErForeldet(periode.erForeldet())
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
            .medErForeldet(periode.erForeldet())
            .medSkattBeløp(periode.getSkattBeløp())
            .medRenteBeløp(periode.getRenteBeløp())
            .medRenterProsent(periode.getRenterProsent())
            .medFeilutbetaltBeløp(periode.getFeilutbetaltBeløp())
            .medRiktigYtelseBeløp(periode.getRiktigYtelseBeløp())
            .medUtbetaltYtelseBeløp(periode.getUtbetaltYtelseBeløp())
            .build();
    }


}
