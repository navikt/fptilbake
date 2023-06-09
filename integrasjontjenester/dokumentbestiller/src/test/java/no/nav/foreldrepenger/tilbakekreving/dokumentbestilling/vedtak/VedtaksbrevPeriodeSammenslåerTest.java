package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultatPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetaling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.FaktaFeilutbetalingPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingAktsomhetEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingGodTroEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingPeriodeEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.VilkårVurderingSærligGrunnEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.NavOppfulgt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse.VurdertForeldelsePeriode;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

class VedtaksbrevPeriodeSammenslåerTest {

    private static final LocalDate NOW = LocalDate.now();
    private static final Periode PERIODE_1 = Periode.of(NOW.minusDays(30), NOW.minusDays(20));
    private static final Periode PERIODE_2 = Periode.of(NOW.minusDays(10), NOW.minusDays(0));

    @Test
    void to_perioder_slås_sammen_uten_foreldelse() {
        var fakta = new FaktaFeilutbetaling();
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_1));
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_2));

        var tjeneste = new VedtaksbrevPeriodeSammenslåer(
            List.of(
                lagVurdering(PERIODE_1),
                lagVurdering(PERIODE_2)),
            null,
            fakta);

        var perioder = tjeneste.utledPerioder(List.of(lagPeriode(PERIODE_1), lagPeriode(PERIODE_2)));

        assertThat(perioder).isNotNull().hasSize(1);
        var periode = perioder.get(0);
        assertThat(periode.getFom()).isEqualTo(PERIODE_1.getFom());
        assertThat(periode.getTom()).isEqualTo(PERIODE_2.getTom());
    }

    @Test
    void to_perioder_slås_sammen_med_foreldelse() {
        var foreldelse = new VurdertForeldelse();
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_1, ForeldelseVurderingType.FORELDET));
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_2, ForeldelseVurderingType.FORELDET));

        var fakta = new FaktaFeilutbetaling();
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_1));
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_2));

        var tjeneste = new VedtaksbrevPeriodeSammenslåer(
            List.of(
                lagVurdering(PERIODE_1),
                lagVurdering(PERIODE_2)),
            foreldelse,
            fakta);

        var perioder = tjeneste.utledPerioder(List.of(lagPeriode(PERIODE_1), lagPeriode(PERIODE_2)));

        assertThat(perioder).isNotNull().hasSize(1);
        var periode = perioder.get(0);
        assertThat(periode.getFom()).isEqualTo(PERIODE_1.getFom());
        assertThat(periode.getTom()).isEqualTo(PERIODE_2.getTom());
    }

    @Test
    void to_perioder_ikke_slås_sammen_pga_ulik_foreldelse() {
        var foreldelse = new VurdertForeldelse();
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_1, ForeldelseVurderingType.FORELDET));
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_2));

        var fakta = new FaktaFeilutbetaling();
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_1));
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_2));

        var tjeneste = new VedtaksbrevPeriodeSammenslåer(
            List.of(
                lagVurdering(PERIODE_1),
                lagVurdering(PERIODE_2)),
            foreldelse,
            fakta);

        var perioder = tjeneste.utledPerioder(List.of(lagPeriode(PERIODE_1), lagPeriode(PERIODE_2)));

        assertThat(perioder).isNotNull().hasSize(2);
    }

    @Test
    void to_perioder_ikke_slås_sammen_pga_ulik_fakta_hendelse_type() {
        var foreldelse = new VurdertForeldelse();
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_1));
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_2));

        var fakta = new FaktaFeilutbetaling();
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_1, HendelseType.MEDLEMSKAP_TYPE));
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_2, HendelseType.FP_BEREGNING_TYPE));

        var tjeneste = new VedtaksbrevPeriodeSammenslåer(
            List.of(
                lagVurdering(PERIODE_1),
                lagVurdering(PERIODE_2)),
            foreldelse,
            fakta);

        var perioder = tjeneste.utledPerioder(List.of(lagPeriode(PERIODE_1), lagPeriode(PERIODE_2)));

        assertThat(perioder).isNotNull().hasSize(2);
    }

    @Test
    void to_perioder_ikke_slås_sammen_pga_ulik_fakta_hendelse_under_type() {
        var foreldelse = new VurdertForeldelse();
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_1));
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_2));

        var fakta = new FaktaFeilutbetaling();
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_1));
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_2, HendelseType.FP_ANNET_HENDELSE_TYPE, HendelseUnderType.BARN_INNLAGT));

        var tjeneste = new VedtaksbrevPeriodeSammenslåer(
            List.of(
                lagVurdering(PERIODE_1),
                lagVurdering(PERIODE_2)),
            foreldelse,
            fakta);

        var perioder = tjeneste.utledPerioder(List.of(lagPeriode(PERIODE_1), lagPeriode(PERIODE_2)));

        assertThat(perioder).isNotNull().hasSize(2);
    }

    @Test
    void to_perioder_ikke_slås_sammen_pga_ulik_vilkår_resultat_type() {
        var foreldelse = new VurdertForeldelse();
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_1));
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_2));

        var fakta = new FaktaFeilutbetaling();
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_1));
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_2));

        var tjeneste = new VedtaksbrevPeriodeSammenslåer(
            List.of(
                lagVurdering(PERIODE_1, VilkårResultat.GOD_TRO),
                lagVurdering(PERIODE_2, VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER)),
            foreldelse,
            fakta);

        var perioder = tjeneste.utledPerioder(List.of(lagPeriode(PERIODE_1), lagPeriode(PERIODE_2)));

        assertThat(perioder).isNotNull().hasSize(2);
    }

    @Test
    void to_perioder_ikke_slås_sammen_pga_ulik_vilkår_nav_oppfulgt() {
        var foreldelse = new VurdertForeldelse();
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_1));
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_2));

        var fakta = new FaktaFeilutbetaling();
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_1));
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_2));

        var tjeneste = new VedtaksbrevPeriodeSammenslåer(
            List.of(
                lagVurdering(PERIODE_1, NavOppfulgt.HAR_IKKE_SJEKKET),
                lagVurdering(PERIODE_2, NavOppfulgt.UDEFINERT)),
            foreldelse,
            fakta);

        var perioder = tjeneste.utledPerioder(List.of(lagPeriode(PERIODE_1), lagPeriode(PERIODE_2)));

        assertThat(perioder).isNotNull().hasSize(2);
    }

    @Test
    void to_perioder_ikke_slås_sammen_pga_ulik_vilkår_god_tro() {
        var foreldelse = new VurdertForeldelse();
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_1));
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_2));

        var fakta = new FaktaFeilutbetaling();
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_1));
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_2));

        var tjeneste = new VedtaksbrevPeriodeSammenslåer(
            List.of(
                lagVurdering(PERIODE_1, false),
                lagVurdering(PERIODE_2, true)),
            foreldelse,
            fakta);

        var perioder = tjeneste.utledPerioder(List.of(lagPeriode(PERIODE_1), lagPeriode(PERIODE_2)));

        assertThat(perioder).isNotNull().hasSize(2);
    }

    @Test
    void to_perioder_ikke_slås_sammen_pga_ulik_vilkår_aktsomhet() {
        var foreldelse = new VurdertForeldelse();
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_1));
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_2));

        var fakta = new FaktaFeilutbetaling();
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_1));
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_2));

        var tjeneste = new VedtaksbrevPeriodeSammenslåer(
            List.of(
                lagVurdering(PERIODE_1, Aktsomhet.FORSETT, true, List.of()),
                lagVurdering(PERIODE_2, Aktsomhet.SIMPEL_UAKTSOM, true, List.of())),
            foreldelse,
            fakta);

        var perioder = tjeneste.utledPerioder(List.of(lagPeriode(PERIODE_1), lagPeriode(PERIODE_2)));

        assertThat(perioder).isNotNull().hasSize(2);
    }

    @Test
    void to_perioder_ikke_slås_sammen_pga_ulik_vilkår_renter() {
        var foreldelse = new VurdertForeldelse();
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_1));
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_2));

        var fakta = new FaktaFeilutbetaling();
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_1));
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_2));

        var tjeneste = new VedtaksbrevPeriodeSammenslåer(
            List.of(
                lagVurdering(PERIODE_1, Aktsomhet.FORSETT, false, List.of()),
                lagVurdering(PERIODE_2, Aktsomhet.FORSETT, true, List.of())),
            foreldelse,
            fakta);

        var perioder = tjeneste.utledPerioder(List.of(lagPeriode(PERIODE_1), lagPeriode(PERIODE_2)));

        assertThat(perioder).isNotNull().hasSize(2);
    }

    @Test
    void to_perioder_ikke_slås_sammen_pga_ulik_vilkår_grunner() {
        var foreldelse = new VurdertForeldelse();
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_1));
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_2));

        var fakta = new FaktaFeilutbetaling();
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_1));
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_2));

        var tjeneste = new VedtaksbrevPeriodeSammenslåer(
            List.of(
                lagVurdering(PERIODE_1, Aktsomhet.FORSETT, true, List.of(SærligGrunn.STØRRELSE_BELØP)),
                lagVurdering(PERIODE_2, Aktsomhet.FORSETT, true, List.of(SærligGrunn.ANNET))),
            foreldelse,
            fakta);

        var perioder = tjeneste.utledPerioder(List.of(lagPeriode(PERIODE_1), lagPeriode(PERIODE_2)));

        assertThat(perioder).isNotNull().hasSize(2);
    }

    @Test
    void to_perioder_slås_sammen_hvis_lik_vilkår_grunner_ikke_i_rekkefølge() {
        var foreldelse = new VurdertForeldelse();
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_1));
        foreldelse.leggTilVurderForeldelsePerioder(lagForeldelse(PERIODE_2));

        var fakta = new FaktaFeilutbetaling();
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_1));
        fakta.leggTilFeilutbetaltPeriode(lagFakta(PERIODE_2));

        var tjeneste = new VedtaksbrevPeriodeSammenslåer(
            List.of(
                lagVurdering(PERIODE_1, Aktsomhet.FORSETT, true, List.of(SærligGrunn.STØRRELSE_BELØP, SærligGrunn.GRAD_AV_UAKTSOMHET)),
                lagVurdering(PERIODE_2, Aktsomhet.FORSETT, true, List.of(SærligGrunn.GRAD_AV_UAKTSOMHET, SærligGrunn.STØRRELSE_BELØP))),
            foreldelse,
            fakta);

        var perioder = tjeneste.utledPerioder(List.of(lagPeriode(PERIODE_1), lagPeriode(PERIODE_2)));

        assertThat(perioder).isNotNull().hasSize(1);
    }

    private BeregningResultatPeriode lagPeriode(Periode periode) {
        return BeregningResultatPeriode.builder()
            .medPeriode(periode)
            .medTilbakekrevingBeløp(BigDecimal.valueOf(5000))
            .medTilbakekrevingBeløpUtenRenter(BigDecimal.valueOf(5000))
            .medTilbakekrevingBeløpEtterSkatt(BigDecimal.valueOf(5000))
            .medSkattBeløp(BigDecimal.ZERO)
            .medRenteBeløp(BigDecimal.ZERO)
            .medFeilutbetaltBeløp(BigDecimal.valueOf(7000))
            .medUtbetaltYtelseBeløp(BigDecimal.valueOf(7000))
            .medRiktigYtelseBeløp(BigDecimal.ZERO)
            .build();
    }

    private VilkårVurderingPeriodeEntitet lagVurdering(Periode periode, Aktsomhet aktsomhet, boolean ileggRenter, List<SærligGrunn> særligeGrunner) {
        var vurdering = lagVurdering(periode, VilkårResultat.FORSTO_BURDE_FORSTÅTT, NavOppfulgt.UDEFINERT, false);

        var aktsomhetVurdering = VilkårVurderingAktsomhetEntitet.builder()
            .medPeriode(vurdering)
            .medBegrunnelse("test")
            .medAktsomhet(aktsomhet)
            .medIleggRenter(ileggRenter)
            .build();

        særligeGrunner.forEach(særligGrunn -> aktsomhetVurdering.leggTilSærligGrunn(VilkårVurderingSærligGrunnEntitet.builder()
            .medGrunn(særligGrunn)
            .medBegrunnelse("grunnelse")
            .medVurdertAktsomhet(aktsomhetVurdering)
            .build())
        );

        vurdering.setAktsomhet(aktsomhetVurdering);

        return vurdering;
    }

    private VilkårVurderingPeriodeEntitet lagVurdering(Periode periode, VilkårResultat resultat, NavOppfulgt navOppfulgt, boolean iGodTro) {
        var vurdering = new VilkårVurderingPeriodeEntitet.Builder()
            .medVilkårResultat(resultat)
            .medNavOppfulgt(navOppfulgt)
            .medPeriode(periode)
            .medBegrunnelse("foo")
            .build();

        if (iGodTro) {
            vurdering.setGodTro(VilkårVurderingGodTroEntitet.builder()
                    .medPeriode(vurdering)
                    .medBegrunnelse("begrunnelse")
                    .medBeløpErIBehold(true)
                .build());
        }
        return vurdering;
    }

    private VilkårVurderingPeriodeEntitet lagVurdering(Periode periode, boolean iGodTro) {
        return lagVurdering(periode, VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER, NavOppfulgt.UDEFINERT, iGodTro);
    }

    private VilkårVurderingPeriodeEntitet lagVurdering(Periode periode, NavOppfulgt navOppfulgt) {
        return lagVurdering(periode, VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER, navOppfulgt, false);
    }

    private VilkårVurderingPeriodeEntitet lagVurdering(Periode periode, VilkårResultat resultat) {
        return lagVurdering(periode, resultat, NavOppfulgt.UDEFINERT, false);
    }

    private VilkårVurderingPeriodeEntitet lagVurdering(Periode periode) {
        return lagVurdering(periode, VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER, NavOppfulgt.UDEFINERT, false);
    }

    private FaktaFeilutbetalingPeriode lagFakta(Periode periode, HendelseType hendelseType, HendelseUnderType underType) {
        return FaktaFeilutbetalingPeriode.builder()
            .medHendelseType(hendelseType)
            .medHendelseUndertype(underType)
            .medPeriode(periode)
            .build();
    }

    private FaktaFeilutbetalingPeriode lagFakta(Periode periode, HendelseType hendelseType) {
        return lagFakta(periode, hendelseType, HendelseUnderType.ANNET_FRITEKST);
    }

    private FaktaFeilutbetalingPeriode lagFakta(Periode periode) {
        return lagFakta(periode, HendelseType.FP_ANNET_HENDELSE_TYPE, HendelseUnderType.ANNET_FRITEKST);
    }

    private VurdertForeldelsePeriode lagForeldelse(Periode periode, ForeldelseVurderingType vurderingType) {
        return VurdertForeldelsePeriode.builder()
            .medForeldelseVurderingType(vurderingType)
            .medForeldelsesFrist(LocalDate.now())
            .medPeriode(periode)
            .build();
    }

    private VurdertForeldelsePeriode lagForeldelse(Periode periode) {
        return lagForeldelse(periode, ForeldelseVurderingType.IKKE_FORELDET);
    }
}
