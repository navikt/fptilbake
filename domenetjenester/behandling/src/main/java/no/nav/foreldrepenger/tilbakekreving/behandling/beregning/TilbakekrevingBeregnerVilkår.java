package no.nav.foreldrepenger.tilbakekreving.behandling.beregning;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BeregnBeløpUtil;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAktsomhetEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingGodTroEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Vurdering;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

class TilbakekrevingBeregnerVilkår {
    private static final BigDecimal _100_PROSENT = BigDecimal.valueOf(100);

    private static final BigDecimal RENTESATS = BigDecimal.valueOf(10);
    private static final BigDecimal RENTEFAKTOR = RENTESATS.divide(_100_PROSENT, 2, RoundingMode.UNNECESSARY);

    private TilbakekrevingBeregnerVilkår() {
        // for CDI
    }

    static BeregningResultatPeriode beregn(VilkårVurderingPeriodeEntitet vilkårVurdering, BigDecimal kravgrunnlagBeløp, List<GrunnlagPeriodeMedSkattProsent> perioderMedSkattProsent) {
        Periode periode = vilkårVurdering.getPeriode();
        Vurdering vurdering = finnVurdering(vilkårVurdering);
        boolean renter = finnRenter(vilkårVurdering);
        BigDecimal andel = finnAndelAvBeløp(vilkårVurdering);
        BigDecimal manueltBeløp = finnManueltSattBeløp(vilkårVurdering);
        boolean ignoreresPgaLavtBeløp = Boolean.FALSE.equals(vilkårVurdering.tilbakekrevesSmåbeløp());

        BeregningResultatPeriode resulat = new BeregningResultatPeriode();
        resulat.setPeriode(periode);
        resulat.setVurdering(vurdering);
        resulat.setRenterProsent(renter ? RENTESATS : null);
        resulat.setFeilutbetaltBeløp(kravgrunnlagBeløp);
        resulat.setAndelAvBeløp(andel);
        resulat.setManueltSattTilbakekrevingsbeløp(manueltBeløp);

        BigDecimal beløpUtenRenter = ignoreresPgaLavtBeløp
            ? BigDecimal.ZERO
            : finnBeløpUtenRenter(kravgrunnlagBeløp, andel, manueltBeløp);
        BigDecimal rentebeløp = beregnRentebeløp(beløpUtenRenter, renter);
        BigDecimal tilbakekrevingBeløp = beløpUtenRenter.add(rentebeløp);
        BigDecimal skattBeløp = beregnSkattBeløp(periode, beløpUtenRenter, perioderMedSkattProsent).setScale(0, RoundingMode.HALF_DOWN); //skatt beregnet alltid med uten renter
        BigDecimal nettoBeløp = tilbakekrevingBeløp.subtract(skattBeløp);

        resulat.setTilbakekrevingBeløpUtenRenter(beløpUtenRenter);
        resulat.setRenteBeløp(rentebeløp);
        resulat.setTilbakekrevingBeløpEtterSkatt(nettoBeløp);
        resulat.setSkattBeløp(skattBeløp);
        resulat.setTilbakekrevingBeløp(tilbakekrevingBeløp);
        return resulat;
    }

    private static BigDecimal beregnRentebeløp(BigDecimal beløp, boolean renter) {
        return renter ? beløp.multiply(RENTEFAKTOR).setScale(0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    private static BigDecimal beregnSkattBeløp(Periode periode, BigDecimal bruttoTilbakekrevesBeløp, List<GrunnlagPeriodeMedSkattProsent> perioderMedSkattProsent) {
        BigDecimal totalKgTilbakekrevesBeløp = perioderMedSkattProsent.stream().map(GrunnlagPeriodeMedSkattProsent::getTilbakekrevesBeløp).reduce(BigDecimal::add).get(); //NOSONAR
        BigDecimal andel = totalKgTilbakekrevesBeløp.signum() == 0 ? BigDecimal.ZERO : bruttoTilbakekrevesBeløp.divide(totalKgTilbakekrevesBeløp, 4, RoundingMode.HALF_UP);
        BigDecimal skattBeløp = BigDecimal.ZERO;
        for (GrunnlagPeriodeMedSkattProsent grunnlagPeriodeMedSkattProsent : perioderMedSkattProsent) {
            if (periode.overlapper(grunnlagPeriodeMedSkattProsent.getPeriode())) {
                BigDecimal delTilbakekrevesBeløp = grunnlagPeriodeMedSkattProsent.getTilbakekrevesBeløp().multiply(andel);
                skattBeløp = skattBeløp.add(delTilbakekrevesBeløp.multiply(grunnlagPeriodeMedSkattProsent.getSkattProsent()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            }
        }
        return skattBeløp;
    }

    private static BigDecimal finnBeløpUtenRenter(BigDecimal kravgrunnlagBeløp, BigDecimal andel, BigDecimal manueltBeløp) {
        if (manueltBeløp != null) {
            return manueltBeløp;
        }
        if (andel != null) {
            return kravgrunnlagBeløp.multiply(andel).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        }
        throw new IllegalArgumentException("Utvikler-feil: Forventer at utledetandel eller manuelt beløp er satt begge manglet");
    }

    private static boolean finnRenter(VilkårVurderingPeriodeEntitet vurdering) {
        VilkårVurderingAktsomhetEntitet aktsomhet = vurdering.getAktsomhet();
        if (aktsomhet != null) {
            return Aktsomhet.FORSETT.equals(aktsomhet.getAktsomhet()) || (aktsomhet.getIleggRenter() != null && aktsomhet.getIleggRenter());
        }
        return false;
    }

    private static BigDecimal finnAndelAvBeløp(VilkårVurderingPeriodeEntitet vurdering) {
        VilkårVurderingAktsomhetEntitet aktsomhet = vurdering.getAktsomhet();
        VilkårVurderingGodTroEntitet godTro = vurdering.getGodTro();
        if (aktsomhet != null) {
            return finnAndelForAktsomhet(aktsomhet);
        } else if (godTro != null && !godTro.isBeløpErIBehold()) {
            return BigDecimal.ZERO;
        }
        return null;
    }

    private static BigDecimal finnAndelForAktsomhet(VilkårVurderingAktsomhetEntitet aktsomhet) {
        if (Aktsomhet.FORSETT.equals(aktsomhet.getAktsomhet()) || Boolean.FALSE.equals(aktsomhet.getSærligGrunnerTilReduksjon())) {
            return _100_PROSENT;
        }
        return aktsomhet.getProsenterSomTilbakekreves();
    }

    private static BigDecimal finnManueltSattBeløp(VilkårVurderingPeriodeEntitet vurdering) {
        VilkårVurderingAktsomhetEntitet aktsomhet = vurdering.getAktsomhet();
        VilkårVurderingGodTroEntitet godTro = vurdering.getGodTro();
        if (aktsomhet != null) {
            return aktsomhet.getManueltTilbakekrevesBeløp();
        } else if (godTro != null) {
            return godTro.getBeløpTilbakekreves();
        }
        throw new IllegalArgumentException("VVurdering skal peke til GodTro-entiet eller Aktsomhet-entitet");
    }

    private static Vurdering finnVurdering(VilkårVurderingPeriodeEntitet vurdering) {
        if (vurdering.getAktsomhet() != null) {
            return vurdering.getAktsomhet().getAktsomhet();
        }
        if (vurdering.getGodTro() != null) {
            return AnnenVurdering.GOD_TRO;
        }
        throw new IllegalArgumentException("VVurdering skal peke til GodTro-entiet eller Aktsomhet-entitet");
    }

}
