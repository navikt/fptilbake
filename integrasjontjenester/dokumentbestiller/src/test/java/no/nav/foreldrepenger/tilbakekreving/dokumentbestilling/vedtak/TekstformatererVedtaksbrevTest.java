package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.EsHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.FellesUndertyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.FpHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.SvpHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.ØkonomiUndertyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbPerson;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbSak;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbTotalresultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVarsel;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevDatoer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbKravgrunnlag;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbResultatTestBuilder;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVurderinger;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class TekstformatererVedtaksbrevTest {

    private final Periode januar = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 31));
    private final Periode februar = Periode.of(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 28));
    private final Periode mars = Periode.of(LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 31));
    private final Periode april = Periode.of(LocalDate.of(2019, 4, 1), LocalDate.of(2019, 4, 30));
    private final Periode førsteNyttårsdag = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 1));

    @Test
    public void skal_generere_vedtaksbrev_for_FP_og_tvillinger_og_simpel_uaktsomhet() throws Exception {
        HbVedtaksbrevFelles vedtaksbrevData = lagTestBuilder()
            .medSak(HbSak.build()
                .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
                .medErFødsel(true)
                .medAntallBarn(2)
                .build())
            .medVedtakResultat(HbTotalresultat.builder()
                .medHovedresultat(VedtakResultatType.DELVIS_TILBAKEBETALING)
                .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(23002))
                .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(23002))
                .medTotaltRentebeløp(BigDecimal.ZERO)
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.valueOf(17601))
                .build())
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medVarsel(HbVarsel.builder()
                .medVarsletBeløp(BigDecimal.valueOf(33001))
                .medVarsletDato(LocalDate.of(2020, 4, 4))
                .build())
            .medKonfigurasjon(HbKonfigurasjon.builder()
                .medKlagefristUker(6)
                .build())
            //.skruAvMidlertidigTekst() //generer tekst som skal brukes i pilot
            .build();
        List<HbVedtaksbrevPeriode> perioder = Arrays.asList(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(30001)))
                .medFakta(HendelseType.FP_ANNET_HENDELSE_TYPE, FellesUndertyper.REFUSJON_ARBEIDSGIVER)
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER)
                    .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                    .medFritekstVilkår("Du er heldig som slapp å betale alt!")
                    .medSærligeGrunner(Arrays.asList(SærligGrunn.TID_FRA_UTBETALING, SærligGrunn.STØRRELSE_BELØP), null, null)
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
                .medFakta(HendelseType.ØKONOMI_FEIL, ØkonomiUndertyper.DOBBELTUTBETALING)
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.FORSTO_BURDE_FORSTÅTT)
                    .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                    .medSærligeGrunner(Arrays.asList(SærligGrunn.HELT_ELLER_DELVIS_NAVS_FEIL, SærligGrunn.STØRRELSE_BELØP), null, null)
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(3000))
                .build()
        );
        HbVedtaksbrevData data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        String generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        String fasit = les("/vedtaksbrev/FP_tvillinger.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_vedtaksbrev_for_FP_og_god_tro_uten_tilbakekreving_uten_varsel() throws Exception {
        List<HbVedtaksbrevPeriode> perioder = Arrays.asList(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medKravgrunnlag(HbKravgrunnlag.builder()
                    .medFeilutbetaltBeløp(BigDecimal.valueOf(1000))
                    .build())
                .medFakta(HendelseType.FP_STONADSPERIODEN_TYPE, FpHendelseUnderTyper.OPPHOR_MOTTAKER_DOD)
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.GOD_TRO)
                    .medAktsomhetResultat(AnnenVurdering.GOD_TRO)
                    .medBeløpIBehold(BigDecimal.ZERO)
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(0))
                .build()
        );
        HbVedtaksbrevFelles vedtaksbrevData = lagTestBuilder()
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
                .build())
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medDatoer(HbVedtaksbrevDatoer.builder()
                .medPerioder(perioder)
                .build())
            .build();
        HbVedtaksbrevData data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        String generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        String fasit = les("/vedtaksbrev/FP_ingen_tilbakekreving.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_vedtaksbrev_for_FP_og_adopsjon_med_mye_fritekst() throws Exception {
        HbVedtaksbrevFelles vedtaksbrevData = lagTestBuilder()
            .medSak(HbSak.build()
                .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
                .medErAdopsjon(true)
                .medAntallBarn(1)
                .build())
            .medVedtakResultat(HbTotalresultat.builder()
                .medHovedresultat(VedtakResultatType.DELVIS_TILBAKEBETALING)
                .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(1234567892))
                .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(1234567892))
                .medTotaltRentebeløp(BigDecimal.ZERO)
                .build())
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medVarsel(HbVarsel.builder()
                .medVarsletBeløp(BigDecimal.valueOf(1234567893))
                .medVarsletDato(LocalDate.of(2019, 1, 3))
                .build())
            .medFritekstOppsummering("Skynd deg å betale, vi trenger pengene med en gang!")
            .build();

        List<HbVedtaksbrevPeriode> perioder = Arrays.asList(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medKravgrunnlag(HbKravgrunnlag.builder()
                    .medFeilutbetaltBeløp(BigDecimal.valueOf(1234567890))
                    .build())
                .medFakta(HendelseType.FP_ANNET_HENDELSE_TYPE, FellesUndertyper.ANNET_FRITEKST, "Ingen vet riktig hva som har skjedd, men du har fått utbetalt alt for mye penger.")
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.FORSTO_BURDE_FORSTÅTT)
                    .medAktsomhetResultat(Aktsomhet.GROVT_UAKTSOM)
                    .medFritekstVilkår("Det er helt utrolig om du ikke har oppdaget dette!")
                    .medSærligeGrunner(Arrays.asList(SærligGrunn.HELT_ELLER_DELVIS_NAVS_FEIL, SærligGrunn.STØRRELSE_BELØP, SærligGrunn.TID_FRA_UTBETALING, SærligGrunn.ANNET), "Gratulerer, du fikk norgesrekord i feilutbetalt beløp! Du skal slippe å betale renter, for det har du ikke råd til uansett!", "at du jobber med foreldrepenger og dermed vet hvordan dette fungerer!")
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
                .medFakta(HendelseType.ØKONOMI_FEIL, ØkonomiUndertyper.FOR_MYE_UTBETALT, "Her har økonomisystemet gjort noe helt feil.")
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
                .medFakta(HendelseType.FP_UTTAK_GRADERT_TYPE, FpHendelseUnderTyper.GRADERT_UTTAK)
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
                .medFakta(HendelseType.FP_UTTAK_KVOTENE_TYPE, FpHendelseUnderTyper.KVO_MOTTAKER_INNLAGT)
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

        HbVedtaksbrevData data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        String generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        String fasit = les("/vedtaksbrev/FP_fritekst_overalt.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_vedtaksbrev_for_SVP_og_ett_barn_og_forsett() throws Exception {
        HbVedtaksbrevFelles vedtaksbrevData = lagTestBuilder()
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
            .build();
        List<HbVedtaksbrevPeriode> perioder = Arrays.asList(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(10000)))
                .medFakta(HendelseType.SVP_FAKTA_TYPE, SvpHendelseUnderTyper.SVP_IKKE_HELSEFARLIG)
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.FORSTO_BURDE_FORSTÅTT)
                    .medAktsomhetResultat(Aktsomhet.FORSETT)
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløpOgRenter(10000, 1000))
                .build()
        );

        HbVedtaksbrevData data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        String generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        String fasit = les("/vedtaksbrev/SVP_forsett.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_vedtaksbrev_for_ES_god_tro_og_ingen_tilbakebetaling() throws Exception {
        HbVedtaksbrevFelles vedtaksbrevData = lagTestBuilder()
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
                .build())
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medVarsel(HbVarsel.builder()
                .medVarsletBeløp(BigDecimal.valueOf(10000))
                .medVarsletDato(LocalDate.of(2020, 4, 4))
                .build())
            .build();

        List<HbVedtaksbrevPeriode> perioder = Arrays.asList(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(førsteNyttårsdag)
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(10000)))
                .medFakta(HendelseType.ES_FODSELSVILKAARET_TYPE, EsHendelseUnderTyper.ES_MOTTAKER_FAR_MEDMOR)
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.GOD_TRO)
                    .medAktsomhetResultat(AnnenVurdering.GOD_TRO)
                    .medBeløpIBehold(BigDecimal.ZERO)
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(0))
                .build()
        );

        HbVedtaksbrevData data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        String generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        String fasit = les("/vedtaksbrev/ES_fødsel_god_tro.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_vedtaksbrev_for_ES_adopsjon_og_grov_uaktsomhet() throws Exception {
        HbVedtaksbrevFelles vedtaksbrevData = lagTestBuilder()
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
                .build())
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medVarsel(HbVarsel.builder()
                .medVarsletBeløp(BigDecimal.valueOf(500000))
                .medVarsletDato(LocalDate.of(2020, 4, 4))
                .build())
            .build();
        List<HbVedtaksbrevPeriode> perioder = Arrays.asList(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(førsteNyttårsdag)
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(500000)))
                .medFakta(HendelseType.ES_ADOPSJONSVILKAARET_TYPE, EsHendelseUnderTyper.ES_BARN_OVER_15)
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

        HbVedtaksbrevData data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        String generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        String fasit = les("/vedtaksbrev/ES_adopsjon_grovt_uaktsom.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_vedtaksbrev_for_FP_med_og_uten_foreldelse() throws Exception {
        HbVedtaksbrevFelles vedtaksbrevData = lagTestBuilder()
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
                .build())
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .build();
        List<HbVedtaksbrevPeriode> perioder = Arrays.asList(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(1000)))
                .medFakta(HendelseType.FP_UTTAK_GRADERT_TYPE, FpHendelseUnderTyper.GRADERT_UTTAK)
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.FORELDET)
                    .medAktsomhetResultat(AnnenVurdering.FORELDET)
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
                    .medVilkårResultat(VilkårResultat.GOD_TRO)
                    .medAktsomhetResultat(AnnenVurdering.GOD_TRO)
                    .medBeløpIBehold(BigDecimal.valueOf(1000))
                    .build())
                .medFakta(HendelseType.FP_UTTAK_GRADERT_TYPE, FpHendelseUnderTyper.GRADERT_UTTAK)
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(1000)))
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(1000))
                .build()
        );
        HbVedtaksbrevData data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        String generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        String fasit = les("/vedtaksbrev/FP_delvis_foreldelse_uten_varsel.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_vedtaksbrev_for_FP_ingen_tilbakekreving_pga_lavt_beløp() throws Exception {
        HbVedtaksbrevFelles vedtaksbrevData = lagTestBuilder()
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
                .build())
            .medLovhjemmelVedtak("Folketrygdloven § 22-15 6.ledd")
            .medVarsel(HbVarsel.builder()
                .medVarsletBeløp(BigDecimal.valueOf(500))
                .medVarsletDato(LocalDate.of(2020, 4, 4))
                .build())
            .build();
        List<HbVedtaksbrevPeriode> perioder = Arrays.asList(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(førsteNyttårsdag)
                .medKravgrunnlag(HbKravgrunnlag.forFeilutbetaltBeløp(BigDecimal.valueOf(500)))
                .medFakta(HendelseType.FP_ANNET_HENDELSE_TYPE, FellesUndertyper.ANNET_FRITEKST, "foo bar baz")
                .medVurderinger(HbVurderinger.builder()
                    .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                    .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
                    .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                    .medUnntasInnkrevingPgaLavtBeløp(true)
                    .build())
                .medResultat(HbResultatTestBuilder.forTilbakekrevesBeløp(0))
                .build()
        );

        HbVedtaksbrevData data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        String generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        String fasit = les("/vedtaksbrev/FP_ikke_tilbakekreves_pga_lavt_beløp.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    private HbVedtaksbrevFelles.Builder lagTestBuilder() {
        return HbVedtaksbrevFelles.builder()
            .medKonfigurasjon(HbKonfigurasjon.builder()
                .skruAvMidlertidigTekst() //generer tekst slik den skal være etter pilot
                .medKlagefristUker(6)
                .build())
            .medSøker(HbPerson.builder()
                .medNavn("Søker Søkersen")
                .medDødsdato(LocalDate.of(2018, 3, 1))
                .medErGift(true)
                .build());
    }

    private String les(String filnavn) throws IOException {
        try (InputStream resource = getClass().getResourceAsStream(filnavn);
             Scanner scanner = new Scanner(resource, "UTF-8")) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : null;
        }
    }

}
