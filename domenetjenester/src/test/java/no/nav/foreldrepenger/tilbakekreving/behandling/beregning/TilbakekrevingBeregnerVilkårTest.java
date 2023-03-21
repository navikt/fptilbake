package no.nav.foreldrepenger.tilbakekreving.behandling.beregning;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FordeltKravgrunnlagBeløp;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAktsomhetEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingGodTroEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

class TilbakekrevingBeregnerVilkårTest {

    private VilkårVurderingPeriodeEntitet vurdering;
    private GrunnlagPeriodeMedSkattProsent grunnlagPeriodeMedSkattProsent;
    private VilkårVurderingPeriodeEntitet forstoBurdeForstattVurdering;

    @BeforeEach
    void setup() {
        vurdering = new VilkårVurderingPeriodeEntitet.Builder()
                .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
                .medPeriode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 3))
                .medBegrunnelse("foo")
                .build();
        forstoBurdeForstattVurdering = new VilkårVurderingPeriodeEntitet.Builder()
                .medVilkårResultat(VilkårResultat.FORSTO_BURDE_FORSTÅTT)
                .medPeriode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 3))
                .medBegrunnelse("foo")
                .build();
        grunnlagPeriodeMedSkattProsent = new GrunnlagPeriodeMedSkattProsent(vurdering.getPeriode(), BigDecimal.valueOf(10000), BigDecimal.ZERO);
    }

    @Test
    void skal_kreve_tilbake_alt_med_renter_ved_forsett_og_illeggRenter_ikke_satt() {
        vurdering.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
                .medAktsomhet(Aktsomhet.FORSETT)
                .medBegrunnelse("foo")
                .medPeriode(vurdering)
                .build());


        //act
        BeregningResultatPeriode resultat = beregn(vurdering, BigDecimal.valueOf(10000), List.of(grunnlagPeriodeMedSkattProsent), true);

        //assert
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(11000));
        assertThat(resultat.getTilbakekrevingBeløpUtenRenter()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(resultat.getRenteBeløp()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(resultat.getRenterProsent()).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(resultat.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(resultat.getPeriode()).isEqualTo(new Periode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 3)));
    }

    @Test
    void skal_kreve_tilbake_alt_med_renter_ved_forsett_og_illeggRenter_satt_true() {
        forstoBurdeForstattVurdering.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
                .medAktsomhet(Aktsomhet.FORSETT)
                .medBegrunnelse("foo")
                .medPeriode(forstoBurdeForstattVurdering)
                .medIleggRenter(true)
                .build());

        //act
        BeregningResultatPeriode resultat = beregn(forstoBurdeForstattVurdering, BigDecimal.valueOf(10000), List.of(grunnlagPeriodeMedSkattProsent), true);

        //assert
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(11000));
        assertThat(resultat.getTilbakekrevingBeløpUtenRenter()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(resultat.getRenteBeløp()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(resultat.getRenterProsent()).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(resultat.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(resultat.getPeriode()).isEqualTo(new Periode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 3)));
    }

    @Test
    void skal_kreve_tilbake_alt_uten_renter_ved_forsett_og_illeggRenter_satt_false() {
        forstoBurdeForstattVurdering.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
                .medAktsomhet(Aktsomhet.FORSETT)
                .medBegrunnelse("foo")
                .medPeriode(forstoBurdeForstattVurdering)
                .medIleggRenter(false)
                .build());

        //act
        BeregningResultatPeriode resultat = beregn(forstoBurdeForstattVurdering, BigDecimal.valueOf(10000), List.of(grunnlagPeriodeMedSkattProsent), true);

        //assert
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(resultat.getTilbakekrevingBeløpUtenRenter()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(resultat.getRenteBeløp()).isEqualByComparingTo(BigDecimal.valueOf(0));
        assertThat(resultat.getRenterProsent()).isNull();
        assertThat(resultat.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(resultat.getPeriode()).isEqualTo(new Periode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 3)));
    }

    @Test
    void skal_kreve_tilbake_alt_ved_grov_uaktsomhet_når_ikke_annet_er_valgt() {
        vurdering.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
                .medAktsomhet(Aktsomhet.GROVT_UAKTSOM)
                .medBegrunnelse("foo")
                .medPeriode(vurdering)
                .medSærligGrunnerTilReduksjon(false)
                .medIleggRenter(true)
                .build());

        //assert
        BeregningResultatPeriode resultat = beregn(vurdering, BigDecimal.valueOf(10000), List.of(grunnlagPeriodeMedSkattProsent), true);
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(11000));
        assertThat(resultat.getRenterProsent()).isEqualByComparingTo(BigDecimal.valueOf(10));
    }

    @Test
    void skal_ikke_kreve_noe_når_sjette_ledd_benyttes_for_å_ikke_gjøre_innkreving_av_småbeløp() {
        vurdering.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
                .medAktsomhet(Aktsomhet.SIMPEL_UAKTSOM)
                .medBegrunnelse("foo")
                .medPeriode(vurdering)
                .medSærligGrunnerTilReduksjon(false)
                .medTilbakekrevSmåBeløp(false)
                .build());

        //assert
        BeregningResultatPeriode resultat = beregn(vurdering, BigDecimal.valueOf(522), List.of(grunnlagPeriodeMedSkattProsent), true);
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void skal_kreve_tilbake_deler_ved_grov_uaktsomhet_når_særlige_grunner_er_valgt_og_ilegge_renter_når_det_er_valgt() {
        vurdering.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
                .medAktsomhet(Aktsomhet.GROVT_UAKTSOM)
                .medBegrunnelse("foo")
                .medPeriode(vurdering)
                .medSærligGrunnerTilReduksjon(true)
                .medIleggRenter(true)
                .medProsenterSomTilbakekreves(BigDecimal.valueOf(70))
                .build());

        //assert
        BeregningResultatPeriode resultat = beregn(vurdering, BigDecimal.valueOf(10000), List.of(grunnlagPeriodeMedSkattProsent), true);
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(7700));
        assertThat(resultat.getRenterProsent()).isEqualByComparingTo(BigDecimal.valueOf(10));
    }

    @Test
    void skal_kreve_tilbake_deler_ved_grov_uaktsomhet_når_særlige_grunner_er_valgt_og_ikke_ilegge_renter_når_det_er_valgt() {
        vurdering.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
                .medAktsomhet(Aktsomhet.GROVT_UAKTSOM)
                .medBegrunnelse("foo")
                .medPeriode(vurdering)
                .medSærligGrunnerTilReduksjon(true)
                .medIleggRenter(false)
                .medProsenterSomTilbakekreves(BigDecimal.valueOf(70))
                .build());

        //assert
        BeregningResultatPeriode resultat = beregn(vurdering, BigDecimal.valueOf(10000), List.of(grunnlagPeriodeMedSkattProsent), true);
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(7000));
        assertThat(resultat.getRenterProsent()).isNull();
        assertThat(resultat.getRenteBeløp()).isZero();
    }

    @Test
    void skal_takle_desimaler_på_prosenter_som_tilbakekreves() {
        vurdering.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
                .medAktsomhet(Aktsomhet.GROVT_UAKTSOM)
                .medBegrunnelse("foo")
                .medPeriode(vurdering)
                .medSærligGrunnerTilReduksjon(true)
                .medIleggRenter(false)
                .medProsenterSomTilbakekreves(new BigDecimal("0.01"))
                .build());

        //assert
        BeregningResultatPeriode resultat = beregn(vurdering, BigDecimal.valueOf(70000), List.of(grunnlagPeriodeMedSkattProsent), true);
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(7));
        assertThat(resultat.getRenterProsent()).isNull();
        assertThat(resultat.getRenteBeløp()).isZero();
    }

    @Test
    void skal_kreve_tilbake_manuelt_beløp_når_det_er_satt() {
        vurdering.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
                .medAktsomhet(Aktsomhet.GROVT_UAKTSOM)
                .medBegrunnelse("foo")
                .medPeriode(vurdering)
                .medSærligGrunnerTilReduksjon(true)
                .medIleggRenter(false)
                .medBeløpTilbakekreves(BigDecimal.valueOf(6556))
                .build());

        //assert
        BeregningResultatPeriode resultat = beregn(vurdering, BigDecimal.valueOf(10000), List.of(grunnlagPeriodeMedSkattProsent), true);
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(6556));
        assertThat(resultat.getRenterProsent()).isNull();
    }

    @Test
    void skal_kreve_tilbake_manuelt_beløp_med_renter_når_det_er_satt() {
        vurdering.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
                .medAktsomhet(Aktsomhet.GROVT_UAKTSOM)
                .medBegrunnelse("foo")
                .medPeriode(vurdering)
                .medSærligGrunnerTilReduksjon(true)
                .medIleggRenter(true)
                .medBeløpTilbakekreves(BigDecimal.valueOf(6000))
                .build());

        //assert
        BeregningResultatPeriode resultat = beregn(vurdering, BigDecimal.valueOf(10000), List.of(grunnlagPeriodeMedSkattProsent), true);
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(6600));
        assertThat(resultat.getRenterProsent()).isEqualByComparingTo(BigDecimal.valueOf(10));
    }


    @Test
    void skal_kreve_tilbake_beløp_som_er_i_behold_uten_renter_ved_god_tro() {
        vurdering.setGodTro(VilkårVurderingGodTroEntitet.builder()
                .medBeløpErIBehold(true)
                .medBeløpTilbakekreves(BigDecimal.valueOf(8991))
                .medBegrunnelse("foo")
                .medPeriode(vurdering)
                .build());

        //assert
        BeregningResultatPeriode resultat = beregn(vurdering, BigDecimal.valueOf(10000), List.of(grunnlagPeriodeMedSkattProsent), true);
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(8991));
        assertThat(resultat.getRenterProsent()).isNull();
    }

    @Test
    void skal_kreve_tilbake_ingenting_når_det_er_god_tro_og_beløp_ikke_er_i_behold() {
        vurdering.setGodTro(VilkårVurderingGodTroEntitet.builder()
                .medBeløpErIBehold(false)
                .medBegrunnelse("foo")
                .medPeriode(vurdering)
                .build());

        //assert
        BeregningResultatPeriode resultat = beregn(vurdering, BigDecimal.valueOf(10000), List.of(grunnlagPeriodeMedSkattProsent), true);
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resultat.getRenterProsent()).isNull();
    }

    @Test
    void skal_kreve_tilbake_beløp_som_er_i_behold_uten_renter_ved_god_tro_med_skatt_prosent() {
        vurdering.setGodTro(VilkårVurderingGodTroEntitet.builder()
                .medBeløpErIBehold(true)
                .medBeløpTilbakekreves(BigDecimal.valueOf(8991))
                .medBegrunnelse("foo")
                .medPeriode(vurdering)
                .build());

        GrunnlagPeriodeMedSkattProsent grunnlagPeriodeMedSkattProsent = new GrunnlagPeriodeMedSkattProsent(vurdering.getPeriode(), BigDecimal.valueOf(10000), BigDecimal.valueOf(10));

        //assert
        BeregningResultatPeriode resultat = beregn(vurdering, BigDecimal.valueOf(10000), List.of(grunnlagPeriodeMedSkattProsent), true);
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(8991));
        assertThat(resultat.getRenterProsent()).isNull();
        assertThat(resultat.getSkattBeløp()).isEqualByComparingTo(BigDecimal.valueOf(899));
        assertThat(resultat.getTilbakekrevingBeløpEtterSkatt()).isEqualByComparingTo(BigDecimal.valueOf(8092));
    }

    @Test
    void skal_kreve_tilbake_alt_med_renter_ved_forsett_med_skatt_prosent() {
        vurdering.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
                .medAktsomhet(Aktsomhet.FORSETT)
                .medBegrunnelse("foo")
                .medPeriode(vurdering)
                .build());

        GrunnlagPeriodeMedSkattProsent grunnlagPeriodeMedSkattProsent = new GrunnlagPeriodeMedSkattProsent(vurdering.getPeriode(), BigDecimal.valueOf(10000), BigDecimal.valueOf(10));

        //act
        BeregningResultatPeriode resultat = beregn(vurdering, BigDecimal.valueOf(10000), List.of(grunnlagPeriodeMedSkattProsent), true);

        //assert
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(11000));
        assertThat(resultat.getTilbakekrevingBeløpUtenRenter()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(resultat.getRenteBeløp()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(resultat.getRenterProsent()).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(resultat.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(resultat.getPeriode()).isEqualTo(new Periode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 3)));
        assertThat(resultat.getSkattBeløp()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(resultat.getTilbakekrevingBeløpEtterSkatt()).isEqualByComparingTo(BigDecimal.valueOf(10000));
    }

    @Test
    void skal_kreve_tilbake_alt_uten_renter_ved_forsett_men_frisinn_med_skatt_prosent() {
        vurdering.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
                .medAktsomhet(Aktsomhet.FORSETT)
                .medBegrunnelse("foo")
                .medPeriode(vurdering)
                .build());

        GrunnlagPeriodeMedSkattProsent grunnlagPeriodeMedSkattProsent = new GrunnlagPeriodeMedSkattProsent(vurdering.getPeriode(), BigDecimal.valueOf(10000), BigDecimal.valueOf(10));

        //act
        BeregningResultatPeriode resultat = beregn(vurdering, BigDecimal.valueOf(10000), List.of(grunnlagPeriodeMedSkattProsent), false);

        //assert
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(resultat.getTilbakekrevingBeløpUtenRenter()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(resultat.getRenteBeløp()).isEqualByComparingTo(BigDecimal.valueOf(0));
        assertThat(resultat.getRenterProsent()).isNull();
        assertThat(resultat.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(resultat.getPeriode()).isEqualTo(new Periode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 3)));
        assertThat(resultat.getSkattBeløp()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(resultat.getTilbakekrevingBeløpEtterSkatt()).isEqualByComparingTo(BigDecimal.valueOf(9000));
    }

    BeregningResultatPeriode beregn(VilkårVurderingPeriodeEntitet vilkårVurdering, BigDecimal feilutbetalt, List<GrunnlagPeriodeMedSkattProsent> perioderMedSkattProsent, boolean beregnRenter) {
        FordeltKravgrunnlagBeløp delresultat = new FordeltKravgrunnlagBeløp(feilutbetalt, feilutbetalt, BigDecimal.ZERO);
        return TilbakekrevingBeregnerVilkår.beregn(vilkårVurdering, delresultat, perioderMedSkattProsent, beregnRenter);
    }

}
