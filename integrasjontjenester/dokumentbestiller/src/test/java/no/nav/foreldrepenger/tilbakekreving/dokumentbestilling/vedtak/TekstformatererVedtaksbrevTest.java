package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbBehandling;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbPerson;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbSak;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbTotalresultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVarsel;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevDatoer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbFakta;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbKravgrunnlag;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbResultatTestBuilder;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVurderinger;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

class TekstformatererVedtaksbrevTest {

    private final Periode januar = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 31));
    private final Periode februar = Periode.of(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 28));
    private final Periode mars = Periode.of(LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 31));
    private final Periode april = Periode.of(LocalDate.of(2019, 4, 1), LocalDate.of(2019, 4, 30));
    private final Periode mai = Periode.of(LocalDate.of(2020, 5, 1), LocalDate.of(2020, 5, 31));
    private final Periode førsteNyttårsdag = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 1));

    @Test
    void skal_generere_vedtaksbrev_for_FP_og_tvillinger_og_simpel_uaktsomhet() throws Exception {
        var data = getVedtaksbrevDataTvilling(FagsakYtelseType.FORELDREPENGER, HendelseType.FP_ANNET_HENDELSE_TYPE, null);

        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        var fasit = les("/vedtaksbrev/FP_tvillinger.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    void skal_generere_vedtaksbrev_for_FP_og_tvillinger_og_simpel_uaktsomhet_nynorsk() throws Exception {
        var data = getVedtaksbrevDataTvilling(FagsakYtelseType.FORELDREPENGER, HendelseType.FP_ANNET_HENDELSE_TYPE, Språkkode.nn);

        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        var fasit = les("/vedtaksbrev/FP_tvillinger_nn.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    private HbVedtaksbrevData getVedtaksbrevDataTvilling(FagsakYtelseType ytelseType, HendelseType hendelseType, Språkkode språkkode) {
        var vedtaksBrevBuilder = lagTestBuilder()
            .medSak(HbSak.build()
                .medYtelsetype(ytelseType)
                .medErFødsel(true)
                .medAntallBarn(2)
                .build())
            .medVedtakResultat(HbTotalresultat.builder()
                .medHovedresultat(VedtakResultatType.DELVIS_TILBAKEBETALING)
                .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(23002))
                .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(23002))
                .medTotaltRentebeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.valueOf(17601))
                .build());
        if (FagsakYtelseType.SVANGERSKAPSPENGER.equals(ytelseType)) {
            vedtaksBrevBuilder
                .medBehandling(HbBehandling.builder()
                    .medErRevurdering(true)
                    .medOriginalBehandlingDatoFagsakvedtak(LocalDate.of(2020, 3, 4))
                    .build());
        }
        vedtaksBrevBuilder
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medVarsel(HbVarsel.builder()
                .medVarsletBeløp(BigDecimal.valueOf(33001))
                .medVarsletDato(LocalDate.of(2020, 4, 4))
                .build())
            .medKonfigurasjon(HbKonfigurasjon.builder()
                .medKlagefristUker(6)
                .build())
            .medSpråkkode(språkkode != null ? språkkode : Språkkode.nb)
            .medVedtaksbrevType(VedtaksbrevType.ORDINÆR)
            .build();
        var perioder = List.of(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(30001)))
                .medFakta(hendelseType, HendelseUnderType.REFUSJON_ARBEIDSGIVER)
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER)
                    .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                    .medFritekstVilkår("Du er heldig som slapp å betale alt!")
                    .medSærligeGrunner(List.of(SærligGrunn.TID_FRA_UTBETALING, SærligGrunn.STØRRELSE_BELØP), null, null)
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(20002))
                .build(),
            HbVedtaksbrevPeriode.builder()
                .medPeriode(februar)
                .medKravgrunnlag(HbKravgrunnlag.builder()
                    .medFeilutbetaltBeløp(BigDecimal.valueOf(3000))
                    .medRiktigBeløp(BigDecimal.valueOf(3000))
                    .medUtbetaltBeløp(BigDecimal.valueOf(6000))
                    .build())
                .medFakta(HendelseType.ØKONOMI_FEIL, HendelseUnderType.DOBBELTUTBETALING)
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.FORSTO_BURDE_FORSTÅTT)
                    .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                    .medSærligeGrunner(List.of(SærligGrunn.HELT_ELLER_DELVIS_NAVS_FEIL, SærligGrunn.STØRRELSE_BELØP), null, null)
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(3000))
                .build()
        );
        var vedtaksbrevData = vedtaksBrevBuilder.build();
        return new HbVedtaksbrevData(vedtaksbrevData, perioder);
    }

    private HbVedtaksbrevData getVedtaksbrevDataOmp() {
        var vedtaksBrevBuilder = lagTestBuilder()
            .medSak(HbSak.build()
                .medYtelsetype(FagsakYtelseType.OMSORGSPENGER)
                .medAntallBarn(1)
                .build())
            .medVedtakResultat(HbTotalresultat.builder()
                .medHovedresultat(VedtakResultatType.DELVIS_TILBAKEBETALING)
                .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(3000))
                .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(3000))
                .medTotaltRentebeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.valueOf(2000))
                .build());
        vedtaksBrevBuilder
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medVarsel(HbVarsel.builder()
                .medVarsletBeløp(BigDecimal.valueOf(3000))
                .medVarsletDato(LocalDate.of(2020, 4, 4))
                .build())
            .medKonfigurasjon(HbKonfigurasjon.builder()
                .medKlagefristUker(6)
                .build())
            .medSpråkkode(Språkkode.nb)
            .medVedtaksbrevType(VedtaksbrevType.ORDINÆR)
            .build();
        var perioder = List.of(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(Periode.of(LocalDate.of(2021, 4, 5), LocalDate.of(2021, 4, 6)))
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(2000)))
                .medFakta(HendelseType.OMP_ANNET_TYPE, HendelseUnderType.ANNET_FRITEKST)
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER)
                    .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                    .medFritekstVilkår("Du er heldig som slapp å betale alt!")
                    .medSærligeGrunner(List.of(SærligGrunn.TID_FRA_UTBETALING, SærligGrunn.STØRRELSE_BELØP), null, null)
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(2000))
                .build(),
            HbVedtaksbrevPeriode.builder()
                .medPeriode(Periode.of(LocalDate.of(2021, 4, 12), LocalDate.of(2021, 4, 12)))
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(1000)))
                .medFakta(HendelseType.OMP_ANNET_TYPE, HendelseUnderType.ANNET_FRITEKST)
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.FORSTO_BURDE_FORSTÅTT)
                    .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(1000))
                .build()
        );
        var vedtaksbrevData = vedtaksBrevBuilder.build();
        return new HbVedtaksbrevData(vedtaksbrevData, perioder);
    }

    @Test
    void skal_generere_vedtaksbrev_for_FP_og_god_tro_uten_tilbakekreving_uten_varsel() throws Exception {
        var perioder = List.of(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medKravgrunnlag(HbKravgrunnlag.builder()
                    .medFeilutbetaltBeløp(BigDecimal.valueOf(1000))
                    .build())
                .medFakta(HendelseType.FP_STONADSPERIODEN_TYPE, HendelseUnderType.OPPHOR_MOTTAKER_DOD)
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.GOD_TRO)
                    .medAktsomhetResultat(AnnenVurdering.GOD_TRO)
                    .medBeløpIBehold(BigDecimal.ZERO)
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(0))
                .build()
        );
        var vedtaksbrevData = lagTestBuilder()
            .medSak(HbSak.build()
                .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
                .medErFødsel(true)
                .medAntallBarn(1)
                .medDatoFagsakvedtak(LocalDate.of(2019, 3, 21))
                .build())
            .medVedtakResultat(HbTotalresultat.builder()
                .medHovedresultat(VedtakResultatType.INGEN_TILBAKEBETALING)
                .medTotaltTilbakekrevesBeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.ZERO)
                .medTotaltRentebeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.ZERO)
                .build())
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medDatoer(HbVedtaksbrevDatoer.builder()
                .medPerioder(perioder)
                .build())
            .medVedtaksbrevType(VedtaksbrevType.ORDINÆR)
            .build();
        var data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        var fasit = les("/vedtaksbrev/FP_ingen_tilbakekreving.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    void skal_generere_vedtaksbrev_for_FP_uten_tilbakekreving_med_tekst_feil_feriepenger() throws Exception {
        var perioder = List.of(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medKravgrunnlag(HbKravgrunnlag.builder()
                    .medFeilutbetaltBeløp(BigDecimal.valueOf(1000))
                    .build())
                .medFakta(HendelseType.FP_ANNET_HENDELSE_TYPE, HendelseUnderType.FEIL_FERIEPENGER_4G)
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.GOD_TRO)
                    .medAktsomhetResultat(AnnenVurdering.GOD_TRO)
                    .medBeløpIBehold(BigDecimal.ZERO)
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(0))
                .build()
        );
        var vedtaksbrevData = lagTestBuilder()
            .medSak(HbSak.build()
                .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
                .medErFødsel(true)
                .medAntallBarn(1)
                .medDatoFagsakvedtak(LocalDate.of(2019, 3, 21))
                .build())
            .medVedtakResultat(HbTotalresultat.builder()
                .medHovedresultat(VedtakResultatType.INGEN_TILBAKEBETALING)
                .medTotaltTilbakekrevesBeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.ZERO)
                .medTotaltRentebeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.ZERO)
                .build())
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medDatoer(HbVedtaksbrevDatoer.builder()
                .medPerioder(perioder)
                .build())
            .medVedtaksbrevType(VedtaksbrevType.ORDINÆR)
            .medSkalFjerneTekstFeriepenger(skalFjerneTekstFeriepenger(perioder))
            .build();
        var data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        var fasit = les("/vedtaksbrev/FP_ingen_tilbakekreving_feil_feriepenger.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }


    @Test
    void skal_generere_vedtaksbrev_SVP_ingenTilbakebetaling_feilFeriepenger() throws Exception {
        var perioder = List.of(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(mai)
                .medKravgrunnlag(HbKravgrunnlag.builder()
                    .medFeilutbetaltBeløp(BigDecimal.valueOf(1000))
                    .build())
                .medFakta(HendelseType.SVP_ANNET_TYPE, HendelseUnderType.FEIL_FERIEPENGER_4G)
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.GOD_TRO)
                    .medAktsomhetResultat(AnnenVurdering.GOD_TRO)
                    .medBeløpIBehold(BigDecimal.ZERO)
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(0))
                .build()
        );
        var vedtaksbrevData = lagTestBuilder()
            .medSak(HbSak.build()
                .medYtelsetype(FagsakYtelseType.SVANGERSKAPSPENGER)
                .medErFødsel(true)
                .medAntallBarn(1)
                .medDatoFagsakvedtak(LocalDate.of(2019, 3, 21))
                .build())
            .medVedtakResultat(HbTotalresultat.builder()
                .medHovedresultat(VedtakResultatType.INGEN_TILBAKEBETALING)
                .medTotaltTilbakekrevesBeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.ZERO)
                .medTotaltRentebeløp(BigDecimal.ZERO)
                .build())
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medDatoer(HbVedtaksbrevDatoer.builder()
                .medPerioder(perioder)
                .build())
            .medKonfigurasjon(HbKonfigurasjon.builder()
                .medKlagefristUker(6)
                .medFireRettsgebyr(BigDecimal.valueOf(4321))
                .build())
            .medVedtaksbrevType(VedtaksbrevType.ORDINÆR)
            .medSkalFjerneTekstFeriepenger(true)
            .build();

        var data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        var fasit = les("/vedtaksbrev/SVP_ingen_tilbakekreving_feil_feriepenger.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }


    @Test
    void skal_generere_vedtaksbrev_for_revurdering_med_SVP_og_tvillinger_og_simpel_uaktsomhet() throws Exception {
        var data = getVedtaksbrevDataTvilling(FagsakYtelseType.SVANGERSKAPSPENGER, HendelseType.SVP_ANNET_TYPE, null);

        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        var fasit = les("/vedtaksbrev/SVP_tvillinger.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    void skal_generere_vedtaksbrev_for_revurdering_med_SVP_og_tvillinger_og_simpel_uaktsomhet_nynorsk() throws Exception {
        var data = getVedtaksbrevDataTvilling(FagsakYtelseType.SVANGERSKAPSPENGER, HendelseType.SVP_ANNET_TYPE, Språkkode.nn);

        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        var fasit = les("/vedtaksbrev/SVP_tvillinger_nn.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    void skal_generere_vedtaksbrev_for_revurdering_med_FP_og_adopsjon_med_mye_fritekst() throws Exception {
        var vedtaksbrevData = lagTestBuilder()
            .medSak(HbSak.build()
                .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
                .medErAdopsjon(true)
                .medAntallBarn(1)
                .build())
            .medBehandling(HbBehandling.builder()
                .medErRevurdering(true)
                .medOriginalBehandlingDatoFagsakvedtak(LocalDate.of(2019, 1, 1))
                .build())
            .medVedtakResultat(HbTotalresultat.builder()
                .medHovedresultat(VedtakResultatType.DELVIS_TILBAKEBETALING)
                .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(1234567892))
                .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(1234567892))
                .medTotaltRentebeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.valueOf(1234567000))
                .build())
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medVarsel(HbVarsel.builder()
                .medVarsletBeløp(BigDecimal.valueOf(1234567893))
                .medVarsletDato(LocalDate.of(2019, 1, 3))
                .build())
            .medFritekstOppsummering("Skynd deg å betale, vi trenger pengene med en gang!")
            .medVedtaksbrevType(VedtaksbrevType.ORDINÆR)
            .build();

        var perioder = List.of(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medKravgrunnlag(HbKravgrunnlag.builder()
                    .medFeilutbetaltBeløp(BigDecimal.valueOf(1234567890))
                    .build())
                .medFakta(HendelseType.FP_ANNET_HENDELSE_TYPE, HendelseUnderType.ANNET_FRITEKST, "Ingen vet riktig hva som har skjedd, men du har fått utbetalt alt for mye penger.")
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.FORSTO_BURDE_FORSTÅTT)
                    .medAktsomhetResultat(Aktsomhet.GROVT_UAKTSOM)
                    .medFritekstVilkår("Det er helt utrolig om du ikke har oppdaget dette!")
                    .medSærligeGrunner(List.of(SærligGrunn.HELT_ELLER_DELVIS_NAVS_FEIL, SærligGrunn.STØRRELSE_BELØP, SærligGrunn.TID_FRA_UTBETALING, SærligGrunn.ANNET), "Gratulerer, du fikk norgesrekord i feilutbetalt beløp! Du skal slippe å betale renter, for det har du ikke råd til uansett!", "at du jobber med foreldrepenger og dermed vet hvordan dette fungerer!")
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(1234567890))
                .build(),
            HbVedtaksbrevPeriode.builder()
                .medPeriode(februar)
                .medKravgrunnlag(HbKravgrunnlag.builder()
                    .medRiktigBeløp(BigDecimal.valueOf(0))
                    .medUtbetaltBeløp(BigDecimal.valueOf(1))
                    .medFeilutbetaltBeløp(BigDecimal.valueOf(1))
                    .build())
                .medFakta(HendelseType.ØKONOMI_FEIL, HendelseUnderType.FOR_MYE_UTBETALT, "Her har økonomisystemet gjort noe helt feil.")
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.GOD_TRO)
                    .medAktsomhetResultat(AnnenVurdering.GOD_TRO)
                    .medFritekstVilkår("Vi skjønner at du ikke har oppdaget beløpet, siden du hadde så mye annet på konto.")
                    .medBeløpIBehold(BigDecimal.valueOf(1))
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(1))
                .build(),
            HbVedtaksbrevPeriode.builder()
                .medPeriode(mars)
                .medKravgrunnlag(HbKravgrunnlag.builder()
                    .medRiktigBeløp(BigDecimal.valueOf(0))
                    .medUtbetaltBeløp(BigDecimal.valueOf(1))
                    .medFeilutbetaltBeløp(BigDecimal.valueOf(1))
                    .build())
                .medFakta(HendelseType.FP_UTTAK_GRADERT_TYPE, HendelseUnderType.GRADERT_UTTAK)
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
                    .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                    .medFritekstVilkår("Her burde du passet mer på!")
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(1))
                .build(),
            HbVedtaksbrevPeriode.builder()
                .medPeriode(april)
                .medFakta(HendelseType.FP_UTTAK_KVOTENE_TYPE, HendelseUnderType.KVO_MOTTAKER_INNLAGT)
                .medKravgrunnlag(HbKravgrunnlag.builder()
                    .medRiktigBeløp(BigDecimal.valueOf(0))
                    .medUtbetaltBeløp(BigDecimal.valueOf(1))
                    .medFeilutbetaltBeløp(BigDecimal.valueOf(1))
                    .build())
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER)
                    .medAktsomhetResultat(Aktsomhet.FORSETT)
                    .medFritekstVilkår("Dette gjorde du med vilje!")
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(1))
                .build()
        );

        var data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        var fasit = les("/vedtaksbrev/FP_fritekst_overalt.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    void skal_generere_vedtaksbrev_for_SVP_og_ett_barn_forstod_at_beløp_var_feil() throws Exception {
        var vedtaksbrevData = lagTestBuilder()
            .medSak(HbSak.build()
                .medYtelsetype(FagsakYtelseType.SVANGERSKAPSPENGER)
                .medErFødsel(true)
                .medAntallBarn(1)
                .build())
            .medVedtakResultat(HbTotalresultat.builder()
                .medHovedresultat(VedtakResultatType.FULL_TILBAKEBETALING)
                .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(10000))
                .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(11000))
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.valueOf(7011))
                .medTotaltRentebeløp(BigDecimal.valueOf(1000))
                .build())
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medVarsel(HbVarsel.builder()
                .medVarsletBeløp(BigDecimal.valueOf(10000))
                .medVarsletDato(LocalDate.of(2020, 4, 4))
                .build())
            .medKonfigurasjon(HbKonfigurasjon.builder()
                .medKlagefristUker(6)
                .build())
            .medVedtaksbrevType(VedtaksbrevType.ORDINÆR)
            .build();
        var perioder = List.of(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(10000)))
                .medFakta(HendelseType.SVP_FAKTA_TYPE, HendelseUnderType.SVP_IKKE_HELSEFARLIG)
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.FORSTO_BURDE_FORSTÅTT)
                    .medAktsomhetResultat(Aktsomhet.FORSETT)
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløpOgRenter(10000, 1000))
                .build()
        );

        var data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        var fasit = les("/vedtaksbrev/SVP_forstod.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    void skal_generere_vedtaksbrev_for_FRISINN_og_forsett() throws Exception {
        var vedtaksbrevData = lagTestBuilder()
            .medSak(HbSak.build()
                .medYtelsetype(FagsakYtelseType.FRISINN)
                .build())
            .medVedtakResultat(HbTotalresultat.builder()
                .medHovedresultat(VedtakResultatType.FULL_TILBAKEBETALING)
                .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(10000))
                .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(10000))
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.valueOf(10000))
                .medTotaltRentebeløp(BigDecimal.valueOf(0))
                .build())
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medVarsel(HbVarsel.builder()
                .medVarsletBeløp(BigDecimal.valueOf(10000))
                .medVarsletDato(LocalDate.of(2020, 4, 4))
                .build())
            .medKonfigurasjon(HbKonfigurasjon.builder()
                .medKlagefristUker(6)
                .build())
            .medVedtaksbrevType(VedtaksbrevType.ORDINÆR)
            .build();
        var perioder = List.of(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(10000)))
                .medFakta(HbFakta.builder()
                    .medHendelsetype(HendelseType.FRISINN_ANNET_TYPE)
                    .medHendelseUndertype(HendelseUnderType.ANNET_FRITEKST)
                    .medFritekstFakta("Dette er svindel!")
                    .build())
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
                    .medAktsomhetResultat(Aktsomhet.FORSETT)
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløpOgRenter(10000, 0))
                .build()
        );

        var data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        var fasit = les("/vedtaksbrev/FRISINN_forsett.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    void skal_generere_vedtaksbrev_for_ES_god_tro_og_ingen_tilbakebetaling() throws Exception {
        var vedtaksbrevData = lagTestBuilder()
            .medSak(HbSak.build()
                .medYtelsetype(FagsakYtelseType.ENGANGSTØNAD)
                .medErFødsel(true)
                .medAntallBarn(1)
                .build())
            .medVedtakResultat(HbTotalresultat.builder()
                .medHovedresultat(VedtakResultatType.INGEN_TILBAKEBETALING)
                .medTotaltTilbakekrevesBeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.ZERO)
                .medTotaltRentebeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.ZERO)
                .build())
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medVarsel(HbVarsel.builder()
                .medVarsletBeløp(BigDecimal.valueOf(10000))
                .medVarsletDato(LocalDate.of(2020, 4, 4))
                .build())
            .medVedtaksbrevType(VedtaksbrevType.ORDINÆR)
            .build();

        var perioder = List.of(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(førsteNyttårsdag)
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(10000)))
                .medFakta(HendelseType.ES_FODSELSVILKAARET_TYPE, HendelseUnderType.ES_MOTTAKER_FAR_MEDMOR)
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.GOD_TRO)
                    .medAktsomhetResultat(AnnenVurdering.GOD_TRO)
                    .medBeløpIBehold(BigDecimal.ZERO)
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(0))
                .build()
        );

        var data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        var fasit = les("/vedtaksbrev/ES_fødsel_god_tro.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    void skal_generere_vedtaksbrev_for_ES_adopsjon_og_grov_uaktsomhet() throws Exception {
        var vedtaksbrevData = lagTestBuilder()
            .medSak(HbSak.build()
                .medErAdopsjon(true)
                .medAntallBarn(5)
                .medYtelsetype(FagsakYtelseType.ENGANGSTØNAD)
                .build())
            .medVedtakResultat(HbTotalresultat.builder()
                .medHovedresultat(VedtakResultatType.FULL_TILBAKEBETALING)
                .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(500000))
                .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(550000))
                .medTotaltRentebeløp(BigDecimal.valueOf(50000))
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.valueOf(550000))
                .build())
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medVarsel(HbVarsel.builder()
                .medVarsletBeløp(BigDecimal.valueOf(500000))
                .medVarsletDato(LocalDate.of(2020, 4, 4))
                .build())
            .medVedtaksbrevType(VedtaksbrevType.ORDINÆR)
            .build();
        var perioder = Arrays.asList(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(førsteNyttårsdag)
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(500000)))
                .medFakta(HendelseType.ES_ADOPSJONSVILKAARET_TYPE, HendelseUnderType.ES_BARN_OVER_15)
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
                    .medAktsomhetResultat(Aktsomhet.GROVT_UAKTSOM)
                    .medSærligeGrunner(Arrays.asList(SærligGrunn.STØRRELSE_BELØP, SærligGrunn.GRAD_AV_UAKTSOMHET), null, null)
                    .build())
                .medResultat(HbResultat.builder()
                    .medTilbakekrevesBeløp(BigDecimal.valueOf(500000))
                    .medTilbakekrevesBeløpUtenSkatt(BigDecimal.valueOf(500000))
                    .medRenterBeløp(BigDecimal.valueOf(50000))
                    .build())
                .build()
        );

        var data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        var fasit = les("/vedtaksbrev/ES_adopsjon_grovt_uaktsom.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    void skal_generere_vedtaksbrev_for_FP_med_og_uten_foreldelse_og_uten_skatt() throws Exception {
        var vedtaksbrevData = lagTestBuilder()
            .medSak(HbSak.build()
                .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
                .medErFødsel(true)
                .medAntallBarn(1)
                .medDatoFagsakvedtak(LocalDate.of(2019, 11, 12))
                .build())
            .medVedtakResultat(HbTotalresultat.builder()
                .medHovedresultat(VedtakResultatType.DELVIS_TILBAKEBETALING)
                .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(1000))
                .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(1000))
                .medTotaltRentebeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.valueOf(1000))
                .build())
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medVedtaksbrevType(VedtaksbrevType.ORDINÆR)
            .build();
        var perioder = Arrays.asList(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(1000)))
                .medFakta(HendelseType.FP_UTTAK_GRADERT_TYPE, HendelseUnderType.GRADERT_UTTAK)
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.FORELDET)
                    .medAktsomhetResultat(AnnenVurdering.FORELDET)
                    .medForeldelsesfrist(januar.getFom().plusMonths(11))
                    .build())
                .medResultat(HbResultat.builder()
                    .medTilbakekrevesBeløp(BigDecimal.ZERO)
                    .medTilbakekrevesBeløpUtenSkatt(BigDecimal.ZERO)
                    .medRenterBeløp(BigDecimal.ZERO)
                    .medForeldetBeløp(BigDecimal.valueOf(1000))
                    .build())
                .build(),
            HbVedtaksbrevPeriode.builder()
                .medPeriode(februar)
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.TILLEGGSFRIST)
                    .medForeldelsesfrist(januar.getFom().plusMonths(11))
                    .medOppdagelsesDato(januar.getFom().plusMonths(8))
                    .medVilkårResultat(VilkårResultat.GOD_TRO)
                    .medAktsomhetResultat(AnnenVurdering.GOD_TRO)
                    .medBeløpIBehold(BigDecimal.valueOf(1000))
                    .build())
                .medFakta(HendelseType.FP_UTTAK_GRADERT_TYPE, HendelseUnderType.GRADERT_UTTAK)
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(1000)))
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(1000))
                .build()
        );
        var data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        var fasit = les("/vedtaksbrev/FP_delvis_foreldelse_uten_varsel.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    void skal_generere_vedtaksbrev_for_FP_ingen_tilbakekreving_pga_lavt_beløp() throws Exception {
        var vedtaksbrevData = lagTestBuilder()
            .medSak(HbSak.build()
                .medErFødsel(true)
                .medAntallBarn(5)
                .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
                .build())
            .medVedtakResultat(HbTotalresultat.builder()
                .medHovedresultat(VedtakResultatType.INGEN_TILBAKEBETALING)
                .medTotaltTilbakekrevesBeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.ZERO)
                .medTotaltRentebeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.ZERO)
                .build())
            .medLovhjemmelVedtak("Folketrygdloven § 22-15 6.ledd")
            .medVarsel(HbVarsel.builder()
                .medVarsletBeløp(BigDecimal.valueOf(500))
                .medVarsletDato(LocalDate.of(2020, 4, 4))
                .build())
            .medVedtaksbrevType(VedtaksbrevType.ORDINÆR)
            .build();
        var perioder = Arrays.asList(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(førsteNyttårsdag)
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(500)))
                .medFakta(HendelseType.FP_ANNET_HENDELSE_TYPE, HendelseUnderType.ANNET_FRITEKST, "foo bar baz")
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
                    .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                    .medUnntasInnkrevingPgaLavtBeløp(true)
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(0))
                .build()
        );

        var data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        var fasit = les("/vedtaksbrev/FP_ikke_tilbakekreves_pga_lavt_beløp.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    void skal_generere_vedtaksbrev_for_FP_ingen_tilbakekreving_pga_lavt_beløp_med_korrigert_beløp() throws Exception {
        var vedtaksbrevData = lagTestBuilder()
            .medSak(HbSak.build()
                .medErFødsel(true)
                .medAntallBarn(5)
                .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
                .build())
            .medVedtakResultat(HbTotalresultat.builder()
                .medHovedresultat(VedtakResultatType.INGEN_TILBAKEBETALING)
                .medTotaltTilbakekrevesBeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.ZERO)
                .medTotaltRentebeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.ZERO)
                .build())
            .medLovhjemmelVedtak("Folketrygdloven § 22-15 6.ledd")
            .medVarsel(HbVarsel.builder()
                .medVarsletBeløp(BigDecimal.valueOf(15000))
                .medVarsletDato(LocalDate.of(2020, 4, 4))
                .build())
            .medErFeilutbetaltBeløpKorrigertNed(true)
            .medTotaltFeilutbetaltBeløp(BigDecimal.valueOf(1000))
            .medVedtaksbrevType(VedtaksbrevType.ORDINÆR)
            .build();
        var perioder = Arrays.asList(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(førsteNyttårsdag)
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(500)))
                .medFakta(HendelseType.FP_ANNET_HENDELSE_TYPE, HendelseUnderType.ANNET_FRITEKST, "foo bar baz")
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
                    .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                    .medUnntasInnkrevingPgaLavtBeløp(true)
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(0))
                .build()
        );

        var data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        var fasit = les("/vedtaksbrev/FP_ikke_tilbakekreves_med_korrigert_beløp.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    void skal_generere_vedtaksbrev_overskrift_foreldrepenger_full_tilbakebetaling() {
        var data = lagBrevOverskriftTestoppsett(FagsakYtelseType.FORELDREPENGER, VedtakResultatType.FULL_TILBAKEBETALING, null);

        var overskrift = TekstformatererVedtaksbrev.lagVedtaksbrevOverskrift(data, null);
        var fasit = "Du må betale tilbake foreldrepengene";
        assertThat(overskrift).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    void skal_generere_vedtaksbrev_overskrift_foreldrepenger_full_tilbakebetaling_nynorsk() {
        var data = lagBrevOverskriftTestoppsett(FagsakYtelseType.FORELDREPENGER, VedtakResultatType.FULL_TILBAKEBETALING, Språkkode.nn);

        var overskrift = TekstformatererVedtaksbrev.lagVedtaksbrevOverskrift(data, Språkkode.nn);
        var fasit = "Du må betale tilbake foreldrepengane";
        assertThat(overskrift).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    void skal_generere_vedtaksbrev_overskrift_engangstønad_ingen_tilbakebetaling() {
        var data = lagBrevOverskriftTestoppsett(FagsakYtelseType.ENGANGSTØNAD, VedtakResultatType.INGEN_TILBAKEBETALING, null);

        var overskrift = TekstformatererVedtaksbrev.lagVedtaksbrevOverskrift(data, null);
        var fasit = "Du må ikke betale tilbake engangsstønaden";
        assertThat(overskrift).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    void skal_generere_fritekst_og_uten_perioder_vedtaksbrev_for_FP_med_full_tilbakebetaling() throws IOException {
        var fritekstVedtaksbrevData = lagFritekstVedtaksbrevData(FagsakYtelseType.FORELDREPENGER, VedtakResultatType.FULL_TILBAKEBETALING, Språkkode.nb);
        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(fritekstVedtaksbrevData);
        assertThat(generertBrev).isNotEmpty();
        var fasit = les("/vedtaksbrev/Fritekst_Vedtaksbrev_FP_full_tilbakebetaling.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    void skal_generere_fritekst_og_uten_perioder_vedtaksbrev_for_ES_med_ingen_tilbakebetaling() throws IOException {
        var fritekstVedtaksbrevData = lagFritekstVedtaksbrevData(FagsakYtelseType.ENGANGSTØNAD, VedtakResultatType.INGEN_TILBAKEBETALING, Språkkode.nb);
        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(fritekstVedtaksbrevData);
        assertThat(generertBrev).isNotEmpty();
        var fasit = les("/vedtaksbrev/Fritekst_Vedtaksbrev_ES_ingen_tilbakebetaling.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    private HbVedtaksbrevData lagBrevOverskriftTestoppsett(FagsakYtelseType ytelsetype,
                                                           VedtakResultatType hovedresultat,
                                                           Språkkode språkkode) {
        var vedtaksbrevFelles = lagTestBuilder()
            .medVedtakResultat(HbTotalresultat.builder()
                .medTotaltTilbakekrevesBeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.ZERO)
                .medTotaltRentebeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.ZERO)
                .medHovedresultat(hovedresultat)
                .build())
            .medLovhjemmelVedtak("uinteressant for testen")
            .medSak(HbSak.build()
                .medErFødsel(true)
                .medYtelsetype(ytelsetype)
                .medAntallBarn(1)
                .medDatoFagsakvedtak(LocalDate.now())
                .build())
            .medSpråkkode(språkkode != null ? språkkode : Språkkode.nb)
            .build();
        return new HbVedtaksbrevData(vedtaksbrevFelles, Collections.emptyList());
    }

    @Test
    void skal_generere_vedtaksbrev_for_OMS() {
        var data = getVedtaksbrevDataOmp();

        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        assertThat(generertBrev).isEqualToNormalizingNewlines("""
Du fikk varsel fra oss 4. april 2020 om at du har fått 3 000 kroner for mye.

Beløpet du skylder før skatt, er 3 000 kroner. Dette er deler av det feilutbetalte beløpet.

Det du skal betale tilbake etter at skatten er trukket fra, er 2 000 kroner.
_Perioden fra og med 5. april 2021 til og med 6. april 2021
_Hvordan har vi kommet fram til at du må betale tilbake?
Du har fått vite om du har rett til omsorgspenger og hvor mye du har rett til. Etter vår vurdering burde du forstått at du ikke ga oss alle opplysningene vi trengte tidsnok for å sikre at du fikk riktig utbetaling. Derfor kan vi kreve pengene tilbake.

Du er heldig som slapp å betale alt!
_Er det særlige grunner til å redusere beløpet?
Vi har vurdert om det er grunner til å redusere beløpet. Vi har lagt vekt på at du ikke har gitt oss alle nødvendige opplysninger tidsnok til at vi kunne unngå feilutbetalingen. Det er også kort tid siden utbetalingen skjedde og beløpet er høyt. Derfor må du betale tilbake hele beløpet.
_12. april 2021
_Hvordan har vi kommet fram til at du må betale tilbake?
Du har fått vite om du har rett til omsorgspenger og hvor mye du har rett til. Selv hvis du har meldt fra til oss, kan vi kreve tilbake det du har fått for mye hvis du burde forstått at beløpet var feil. At du må betale tilbake, betyr ikke at du selv har skyld i feilutbetalingen.

Ut fra informasjonen du har fått, burde du etter vår vurdering forstått at du fikk for mye utbetalt. Derfor kan vi kreve tilbake.
_Lovhjemmelen vi har brukt
Vedtaket er gjort etter Folketrygdloven § 22-15.
_Skatt og skatteoppgjør
Skatten som er trukket fra beløpet du skal betale tilbake, er beregnet etter det du har blitt trukket i skatt i gjennomsnitt per måned. Det betyr at beløpet du skal betale tilbake etter skatt, ikke alltid er likt med det beløpet du fikk inn på konto.

Nav gir opplysninger til Skattetaten om skattebeløpet og om beløpet du skal betale tilbake før skatt er trukket fra. Skatteetaten vil vurdere om det er grunnlag for å endre skatteoppgjør.
_Hvordan betaler du tilbake?
Du vil få faktura fra Skatteetaten på det beløpet du skal betale tilbake.

På fakturaen vil det stå informasjon om nøyaktig beløp, kontonummer og forfallsdato. Du trenger ikke å gjøre noe før du får fakturaen.

Du finner mer informasjon på nav.no/innbetaling.
_Du har rett til å klage
Du kan klage innen 6 uker fra den datoen du mottok vedtaket. Du finner skjema og informasjon på nav.no/klage.

Du må som hovedregel begynne å betale beløpet tilbake når du får fakturaen selv om du klager på dette vedtaket. Dette følger av forvaltningsloven § 42. Hvis du får vedtak om at du ikke trengte å betale tilbake hele eller deler av beløpet du skyldte, betaler vi pengene tilbake til deg.
_Du har rett til innsyn
På nav.no/dittnav kan du se dokumentene i saken din.
_Trenger du mer informasjon?
Du finner informasjon som kan være nyttig for deg på nav.no/omsorgspenger.

Med vennlig hilsen
Nav arbeid og ytelser


Vedlegg: Resultatet av tilbakebetalingssaken""");
    }

    @Test
    void skal_generere_vedtaksbrev_for_OLP() {
        var data = getVedtaksbrevDataOlp();

        var generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        assertThat(generertBrev).isEqualToNormalizingNewlines("""
Du fikk varsel fra oss 4. april 2020 om at du har fått 3 000 kroner for mye.

Beløpet du skylder før skatt, er 3 000 kroner. Dette er deler av det feilutbetalte beløpet.

Det du skal betale tilbake etter at skatten er trukket fra, er 2 000 kroner.
_Perioden fra og med 5. april 2021 til og med 6. april 2021
_Hvordan har vi kommet fram til at du må betale tilbake?
Du har fått vite om du har rett til opplæringspenger og hvor mye du har rett til. Etter vår vurdering burde du forstått at du ikke ga oss alle opplysningene vi trengte tidsnok for å sikre at du fikk riktig utbetaling. Derfor kan vi kreve pengene tilbake.

Du er heldig som slapp å betale alt!
_Er det særlige grunner til å redusere beløpet?
Vi har vurdert om det er grunner til å redusere beløpet. Vi har lagt vekt på at du ikke har gitt oss alle nødvendige opplysninger tidsnok til at vi kunne unngå feilutbetalingen. Det er også kort tid siden utbetalingen skjedde og beløpet er høyt. Derfor må du betale tilbake hele beløpet.
_12. april 2021
_Hvordan har vi kommet fram til at du må betale tilbake?
Du har fått vite om du har rett til opplæringspenger og hvor mye du har rett til. Selv hvis du har meldt fra til oss, kan vi kreve tilbake det du har fått for mye hvis du burde forstått at beløpet var feil. At du må betale tilbake, betyr ikke at du selv har skyld i feilutbetalingen.

Ut fra informasjonen du har fått, burde du etter vår vurdering forstått at du fikk for mye utbetalt. Derfor kan vi kreve tilbake.
_Lovhjemmelen vi har brukt
Vedtaket er gjort etter Folketrygdloven § 22-15.
_Skatt og skatteoppgjør
Skatten som er trukket fra beløpet du skal betale tilbake, er beregnet etter det du har blitt trukket i skatt i gjennomsnitt per måned. Det betyr at beløpet du skal betale tilbake etter skatt, ikke alltid er likt med det beløpet du fikk inn på konto.

Nav gir opplysninger til Skattetaten om skattebeløpet og om beløpet du skal betale tilbake før skatt er trukket fra. Skatteetaten vil vurdere om det er grunnlag for å endre skatteoppgjør.
_Hvordan betaler du tilbake?
Du vil få faktura fra Skatteetaten på det beløpet du skal betale tilbake.

På fakturaen vil det stå informasjon om nøyaktig beløp, kontonummer og forfallsdato. Du trenger ikke å gjøre noe før du får fakturaen.

Du finner mer informasjon på nav.no/innbetaling.
_Du har rett til å klage
Du kan klage innen 6 uker fra den datoen du mottok vedtaket. Du finner skjema og informasjon på nav.no/klage.

Du må som hovedregel begynne å betale beløpet tilbake når du får fakturaen selv om du klager på dette vedtaket. Dette følger av forvaltningsloven § 42. Hvis du får vedtak om at du ikke trengte å betale tilbake hele eller deler av beløpet du skyldte, betaler vi pengene tilbake til deg.
_Du har rett til innsyn
På nav.no/dittnav kan du se dokumentene i saken din.
_Trenger du mer informasjon?
Du finner informasjon som kan være nyttig for deg på nav.no/opplaringspenger.

Du kan også kontakte oss på telefon 55 55 33 33.

Med vennlig hilsen
Nav arbeid og ytelser


Vedlegg: Resultatet av tilbakebetalingssaken""");
    }

    private HbVedtaksbrevData getVedtaksbrevDataOlp() {
        var vedtaksBrevBuilder = lagTestBuilder()
            .medSak(HbSak.build()
                .medYtelsetype(FagsakYtelseType.OPPLÆRINGSPENGER)
                .medAntallBarn(1)
                .build())
            .medVedtakResultat(HbTotalresultat.builder()
                .medHovedresultat(VedtakResultatType.DELVIS_TILBAKEBETALING)
                .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(3000))
                .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(3000))
                .medTotaltRentebeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.valueOf(2000))
                .build());
        vedtaksBrevBuilder
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medVarsel(HbVarsel.builder()
                .medVarsletBeløp(BigDecimal.valueOf(3000))
                .medVarsletDato(LocalDate.of(2020, 4, 4))
                .build())
            .medKonfigurasjon(HbKonfigurasjon.builder()
                .medKlagefristUker(6)
                .build())
            .medSpråkkode(Språkkode.nb)
            .medVedtaksbrevType(VedtaksbrevType.ORDINÆR)
            .build();
        var perioder = List.of(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(Periode.of(LocalDate.of(2021, 4, 5), LocalDate.of(2021, 4, 6)))
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(2000)))
                .medFakta(HendelseType.OLP_ANNET_TYPE, HendelseUnderType.ANNET_FRITEKST)
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER)
                    .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                    .medFritekstVilkår("Du er heldig som slapp å betale alt!")
                    .medSærligeGrunner(List.of(SærligGrunn.TID_FRA_UTBETALING, SærligGrunn.STØRRELSE_BELØP), null, null)
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(2000))
                .build(),
            HbVedtaksbrevPeriode.builder()
                .medPeriode(Periode.of(LocalDate.of(2021, 4, 12), LocalDate.of(2021, 4, 12)))
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(1000)))
                .medFakta(HendelseType.OMP_ANNET_TYPE, HendelseUnderType.ANNET_FRITEKST)
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.FORSTO_BURDE_FORSTÅTT)
                    .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(1000))
                .build()
        );
        var vedtaksbrevData = vedtaksBrevBuilder.build();
        return new HbVedtaksbrevData(vedtaksbrevData, perioder);
    }

    private HbVedtaksbrevData lagFritekstVedtaksbrevData(FagsakYtelseType ytelsetype,
                                                         VedtakResultatType hovedresultat,
                                                         Språkkode språkkode) {
        var vedtaksbrevFelles = lagTestBuilder()
            .medSak(HbSak.build()
                .medErFødsel(true)
                .medYtelsetype(ytelsetype)
                .medAntallBarn(1)
                .medDatoFagsakvedtak(LocalDate.now())
                .build())
            .medVedtakResultat(HbTotalresultat.builder()
                .medTotaltTilbakekrevesBeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.ZERO)
                .medTotaltRentebeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.ZERO)
                .medHovedresultat(hovedresultat)
                .build())
            .medBehandling(HbBehandling.builder()
                .medErRevurdering(true)
                .medOriginalBehandlingDatoFagsakvedtak(LocalDate.of(2020, 3, 4))
                .build())
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medSpråkkode(språkkode != null ? språkkode : Språkkode.nb)
            .medFritekstOppsummering("sender fritekst vedtaksbrev")
            .medVedtaksbrevType(VedtaksbrevType.FRITEKST_FEILUTBETALING_BORTFALT)
            .build();
        return new HbVedtaksbrevData(vedtaksbrevFelles, Collections.emptyList());
    }

    private HbVedtaksbrevFelles.Builder lagTestBuilder() {
        return HbVedtaksbrevFelles.builder()
            .medKonfigurasjon(HbKonfigurasjon.builder()
                .medKlagefristUker(6)
                .build())
            .medSøker(HbPerson.builder()
                .medNavn("Søker Søkersen")
                .medDødsdato(LocalDate.of(2018, 3, 1))
                .medErGift(true)
                .build());
    }

    private String les(String filnavn) throws IOException {
        try (var resource = getClass().getResourceAsStream(filnavn); var scanner = new Scanner(resource, StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : null;
        }
    }

    private boolean skalFjerneTekstFeriepenger(List<HbVedtaksbrevPeriode> perioder) {
        return perioder.stream().anyMatch(p -> HendelseUnderType.FEIL_FERIEPENGER_4G.equals(p.getFakta().getHendelseundertype()));
    }
}
