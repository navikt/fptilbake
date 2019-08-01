package no.nav.foreldrepenger.tilbakekreving.behandling.beregning;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAktsomhetEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingGodTroEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class TilbakekrevingBeregnerVilkårTest {

    private VilkårVurderingPeriodeEntitet vurdering = new VilkårVurderingPeriodeEntitet.Builder()
        .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
        .medPeriode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 3))
        .medBegrunnelse("foo")
        .build();

    @Test
    public void skal_kreve_tilbake_alt_med_renter_ved_forsett() {
        vurdering.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
            .medAktsomhet(Aktsomhet.FORSETT)
            .medBegrunnelse("foo")
            .medPeriode(vurdering)
            .build());

        //act
        BeregningResultatPeriode resultat = TilbakekrevingBeregnerVilkår.beregn(vurdering, BigDecimal.valueOf(10000));

        //assert
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(11000));
        assertThat(resultat.getTilbakekrevingBeløpUtenRenter()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(resultat.getRenteBeløp()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(resultat.getRenterProsent()).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(resultat.getAndelAvBeløp()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(resultat.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(resultat.getVurdering()).isEqualByComparingTo(Aktsomhet.FORSETT);
        assertThat(resultat.getPeriode()).isEqualTo(new Periode(LocalDate.of(2019, 5, 1), LocalDate.of(2019, 5, 3)));
        assertThat(resultat.getManueltSattTilbakekrevingsbeløp()).isNull();
    }

    @Test
    public void skal_kreve_tilbake_alt_ved_grov_uaktsomhet_når_ikke_annet_er_valgt() {
        vurdering.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
            .medAktsomhet(Aktsomhet.GROVT_UAKTSOM)
            .medBegrunnelse("foo")
            .medPeriode(vurdering)
            .medSærligGrunnerTilReduksjon(false)
            .medIleggRenter(true)
            .build());

        //assert
        BeregningResultatPeriode resultat = TilbakekrevingBeregnerVilkår.beregn(vurdering, BigDecimal.valueOf(10000));
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(11000));
        assertThat(resultat.getRenterProsent()).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(resultat.getVurdering()).isEqualByComparingTo(Aktsomhet.GROVT_UAKTSOM);
    }

    @Test
    public void skal_kreve_tilbake_deler_ved_grov_uaktsomhet_når_særlige_grunner_er_valgt_og_ilegge_renter_når_det_er_valgt() {
        vurdering.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
            .medAktsomhet(Aktsomhet.GROVT_UAKTSOM)
            .medBegrunnelse("foo")
            .medPeriode(vurdering)
            .medSærligGrunnerTilReduksjon(true)
            .medIleggRenter(true)
            .medAndelSomTilbakekreves(70)
            .build());

        //assert
        BeregningResultatPeriode resultat = TilbakekrevingBeregnerVilkår.beregn(vurdering, BigDecimal.valueOf(10000));
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(7700));
        assertThat(resultat.getRenterProsent()).isEqualByComparingTo(BigDecimal.valueOf(10));
    }

    @Test
    public void skal_kreve_tilbake_deler_ved_grov_uaktsomhet_når_særlige_grunner_er_valgt_og_ikke_ilegge_renter_når_det_er_valgt() {
        vurdering.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
            .medAktsomhet(Aktsomhet.GROVT_UAKTSOM)
            .medBegrunnelse("foo")
            .medPeriode(vurdering)
            .medSærligGrunnerTilReduksjon(true)
            .medIleggRenter(false)
            .medAndelSomTilbakekreves(70)
            .build());

        //assert
        BeregningResultatPeriode resultat = TilbakekrevingBeregnerVilkår.beregn(vurdering, BigDecimal.valueOf(10000));
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(7000));
        assertThat(resultat.getRenterProsent()).isNull();
        assertThat(resultat.getRenteBeløp()).isZero();
    }

    @Test
    public void skal_kreve_tilbake_manuelt_beløp_når_det_er_satt() {
        vurdering.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
            .medAktsomhet(Aktsomhet.GROVT_UAKTSOM)
            .medBegrunnelse("foo")
            .medPeriode(vurdering)
            .medSærligGrunnerTilReduksjon(true)
            .medIleggRenter(false)
            .medBeløpTilbakekreves(BigDecimal.valueOf(6556))
            .build());

        //assert
        BeregningResultatPeriode resultat = TilbakekrevingBeregnerVilkår.beregn(vurdering, BigDecimal.valueOf(10000));
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(6556));
        assertThat(resultat.getRenterProsent()).isNull();
    }

    @Test
    public void skal_kreve_tilbake_manuelt_beløp_med_renter_når_det_er_satt() {
        vurdering.setAktsomhet(VilkårVurderingAktsomhetEntitet.builder()
            .medAktsomhet(Aktsomhet.GROVT_UAKTSOM)
            .medBegrunnelse("foo")
            .medPeriode(vurdering)
            .medSærligGrunnerTilReduksjon(true)
            .medIleggRenter(true)
            .medBeløpTilbakekreves(BigDecimal.valueOf(6000))
            .build());

        //assert
        BeregningResultatPeriode resultat = TilbakekrevingBeregnerVilkår.beregn(vurdering, BigDecimal.valueOf(10000));
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(6600));
        assertThat(resultat.getRenterProsent()).isEqualByComparingTo(BigDecimal.valueOf(10));
    }


    @Test
    public void skal_kreve_tilbake_beløp_som_er_i_behold_uten_renter_ved_god_tro() {
        vurdering.setGodTro(VilkårVurderingGodTroEntitet.builder()
            .medBeløpErIBehold(true)
            .medBeløpTilbakekreves(BigDecimal.valueOf(8991))
            .medBegrunnelse("foo")
            .medPeriode(vurdering)
            .build());

        //assert
        BeregningResultatPeriode resultat = TilbakekrevingBeregnerVilkår.beregn(vurdering, BigDecimal.valueOf(10000));
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.valueOf(8991));
        assertThat(resultat.getRenterProsent()).isNull();
        assertThat(resultat.getAndelAvBeløp()).isNull();
        assertThat(resultat.getVurdering()).isEqualTo(AnnenVurdering.GOD_TRO);
        assertThat(resultat.getManueltSattTilbakekrevingsbeløp()).isEqualByComparingTo(BigDecimal.valueOf(8991));
    }

    @Test
    public void skal_kreve_tilbake_ingenting_når_det_er_god_tro_og_beløp_ikke_er_i_behold() {
        vurdering.setGodTro(VilkårVurderingGodTroEntitet.builder()
            .medBeløpErIBehold(false)
            .medBegrunnelse("foo")
            .medPeriode(vurdering)
            .build());

        //assert
        BeregningResultatPeriode resultat = TilbakekrevingBeregnerVilkår.beregn(vurdering, BigDecimal.valueOf(10000));
        assertThat(resultat.getTilbakekrevingBeløp()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resultat.getRenterProsent()).isNull();
        assertThat(resultat.getAndelAvBeløp()).isZero();
        assertThat(resultat.getVurdering()).isEqualTo(AnnenVurdering.GOD_TRO);
        assertThat(resultat.getManueltSattTilbakekrevingsbeløp()).isNull();
    }


}
