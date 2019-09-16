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

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.EsHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.FpHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.SvpHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class TekstformatererVedtaksbrevTest {

    private final Periode januar = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 31));
    private final Periode februar = Periode.of(LocalDate.of(2019, 2, 1), LocalDate.of(2019, 2, 28));
    private final Periode mars = Periode.of(LocalDate.of(2019, 3, 1), LocalDate.of(2019, 3, 31));
    private final Periode april = Periode.of(LocalDate.of(2019, 4, 1), LocalDate.of(2019, 4, 30));
    private final Periode førsteNyttårsdag = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 1));

    @Test
    public void skal_generere_vedtaksbrev_for_FP_og_tvillinger_og_simpel_uaktsomhet() throws Exception {
        HbVedtaksbrevFelles vedtaksbrevData = HbVedtaksbrevFelles.builder()
            .medErFødsel(true)
            .medAntallBarn(2)
            .medHovedresultat(VedtakResultatType.DELVIS_TILBAKEBETALING)
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
            .medVarsletBeløp(BigDecimal.valueOf(33001))
            .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(23002))
            .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(23002))
            .medTotaltRentebeløp(BigDecimal.ZERO)
            .medVarsletDato(LocalDate.of(2020, 4, 4))
            .medKlagefristUker(6)
            .build();
        List<HbVedtaksbrevPeriode> perioder = Arrays.asList(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medHendelsetype(HendelseType.FP_UTTAK_GRADERT_TYPE)
                .medHendelseUndertype(FpHendelseUnderTyper.GRADERT_UTTAK)
                .medVilkårResultat(VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER)
                .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                .medSærligeGrunner(Arrays.asList(SærligGrunn.TID_FRA_UTBETALING, SærligGrunn.STØRRELSE_BELØP))
                .medRiktigBeløp(BigDecimal.valueOf(10000))
                .medFeilutbetaltBeløp(BigDecimal.valueOf(30001))
                .medTilbakekrevesBeløp(BigDecimal.valueOf(20002))
                .medFritekstVilkår("Du er heldig som slapp å betale alt!")
                .build(),
            HbVedtaksbrevPeriode.builder()
                .medPeriode(februar)
                .medHendelsetype(HendelseType.FP_ANNET_HENDELSE_TYPE)
                .medHendelseUndertype(FpHendelseUnderTyper.OKONOMI_DOBBELUTBETALING)
                .medVilkårResultat(VilkårResultat.FORSTO_BURDE_FORSTÅTT)
                .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                .medSærligeGrunner(Arrays.asList(SærligGrunn.HELT_ELLER_DELVIS_NAVS_FEIL, SærligGrunn.STØRRELSE_BELØP))
                .medRiktigBeløp(BigDecimal.valueOf(3000))
                .medUtbetaltBeløp(BigDecimal.valueOf(6000))
                .medFeilutbetaltBeløp(BigDecimal.valueOf(3000))
                .medTilbakekrevesBeløp(BigDecimal.valueOf(3000))
                .build()
        );
        HbVedtaksbrevData data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        String generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        String fasit = les("/vedtaksbrev/FP_tvillinger.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_vedtaksbrev_for_FP_og_adopsjon_med_mye_fritekst() throws Exception {
        HbVedtaksbrevFelles vedtaksbrevData = HbVedtaksbrevFelles.builder()
            .medErAdopsjon(true)
            .medAntallBarn(1)
            .medHovedresultat(VedtakResultatType.DELVIS_TILBAKEBETALING)
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
            .medVarsletBeløp(BigDecimal.valueOf(1234567893))
            .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(1234567892))
            .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(1234567892))
            .medTotaltRentebeløp(BigDecimal.ZERO)
            .medVarsletDato(LocalDate.of(2019, 1, 3))
            .medFritekstOppsummering("Skynd deg å betale, vi trenger pengene med en gang!")
            .medKlagefristUker(6)
            .build();

        List<HbVedtaksbrevPeriode> perioder = Arrays.asList(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medHendelsetype(HendelseType.FP_ANNET_HENDELSE_TYPE)
                .medHendelseUndertype(FpHendelseUnderTyper.ANNET_FRITEKST)
                .medVilkårResultat(VilkårResultat.FORSTO_BURDE_FORSTÅTT)
                .medAktsomhetResultat(Aktsomhet.GROVT_UAKTSOM)
                .medSærligeGrunner(Arrays.asList(SærligGrunn.HELT_ELLER_DELVIS_NAVS_FEIL, SærligGrunn.STØRRELSE_BELØP, SærligGrunn.TID_FRA_UTBETALING))
                .medRiktigBeløp(BigDecimal.valueOf(0))
                .medFeilutbetaltBeløp(BigDecimal.valueOf(1234567890))
                .medTilbakekrevesBeløp(BigDecimal.valueOf(1234567890))
                .medRenterBeløp(BigDecimal.valueOf(0))
                .medFritekstFakta("Ingen vet riktig hva som har skjedd, men du har fått utbetalt alt for mye penger.")
                .medFritekstSærligeGrunner("Gratulerer, du fikk norgesrekord i feilutbetalt beløp! Du skal slippe å betale renter, for det har du ikke råd til uansett!")
                .medFritekstVilkår("Det er helt utrolig om du ikke har oppdaget dette!")
                .build(),
            HbVedtaksbrevPeriode.builder()
                .medPeriode(februar)
                .medHendelsetype(HendelseType.FP_ANNET_HENDELSE_TYPE)
                .medHendelseUndertype(FpHendelseUnderTyper.OKONOMI_UTBETALT)
                .medVilkårResultat(VilkårResultat.GOD_TRO)
                .medAktsomhetResultat(AnnenVurdering.GOD_TRO)
                .medBeløpIBehold(BigDecimal.valueOf(1))
                .medRiktigBeløp(BigDecimal.valueOf(0))
                .medUtbetaltBeløp(BigDecimal.valueOf(1))
                .medFeilutbetaltBeløp(BigDecimal.valueOf(1))
                .medTilbakekrevesBeløp(BigDecimal.valueOf(1))
                .medFritekstFakta("Her har økonomisystemet gjort noe helt feil.")
                .medFritekstVilkår("Vi skjønner at du ikke har oppdaget beløpet, siden du hadde så mye annet på konto.")
                .build(),
            HbVedtaksbrevPeriode.builder()
                .medPeriode(mars)
                .medHendelsetype(HendelseType.FP_UTTAK_GRADERT_TYPE)
                .medHendelseUndertype(FpHendelseUnderTyper.GRADERT_UTTAK)
                .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
                .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                .medRiktigBeløp(BigDecimal.valueOf(0))
                .medUtbetaltBeløp(BigDecimal.valueOf(1))
                .medFeilutbetaltBeløp(BigDecimal.valueOf(1))
                .medTilbakekrevesBeløp(BigDecimal.valueOf(1))
                .medFritekstVilkår("Her burde du passet mer på!")
                .build(),
            HbVedtaksbrevPeriode.builder()
                .medPeriode(april)
                .medHendelsetype(HendelseType.FP_UTTAK_KVOTENE_TYPE)
                .medHendelseUndertype(FpHendelseUnderTyper.KVO_MOTTAKER_INNLAGT)
                .medVilkårResultat(VilkårResultat.MANGELFULLE_OPPLYSNINGER_FRA_BRUKER)
                .medAktsomhetResultat(Aktsomhet.FORSETT)
                .medRiktigBeløp(BigDecimal.valueOf(0))
                .medUtbetaltBeløp(BigDecimal.valueOf(1))
                .medFeilutbetaltBeløp(BigDecimal.valueOf(1))
                .medTilbakekrevesBeløp(BigDecimal.valueOf(1))
                .medFritekstVilkår("Dette gjordet du med vilje!")
                .build()
        );

        HbVedtaksbrevData data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        String generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        System.out.println(generertBrev);
        String fasit = les("/vedtaksbrev/FP_fritekst_overalt.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_vedtaksbrev_for_SVP_og_ett_barn_og_forsett() throws Exception {
        HbVedtaksbrevFelles vedtaksbrevData = HbVedtaksbrevFelles.builder()
            .medErFødsel(true)
            .medAntallBarn(1)
            .medHovedresultat(VedtakResultatType.FULL_TILBAKEBETALING)
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medYtelsetype(FagsakYtelseType.SVANGERSKAPSPENGER)
            .medVarsletBeløp(BigDecimal.valueOf(10000))
            .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(10000))
            .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(11000))
            .medTotaltRentebeløp(BigDecimal.valueOf(1000))
            .medVarsletDato(LocalDate.of(2020, 4, 4))
            .medKlagefristUker(6)
            .build();
        List<HbVedtaksbrevPeriode> perioder = Arrays.asList(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medHendelsetype(HendelseType.SVP_FAKTA_TYPE)
                .medHendelseUndertype(SvpHendelseUnderTyper.SVP_IKKE_HELSEFARLIG)
                .medVilkårResultat(VilkårResultat.FORSTO_BURDE_FORSTÅTT)
                .medAktsomhetResultat(Aktsomhet.FORSETT)
                .medRiktigBeløp(BigDecimal.ZERO)
                .medFeilutbetaltBeløp(BigDecimal.valueOf(10000))
                .medTilbakekrevesBeløp(BigDecimal.valueOf(10000))
                .medRenterBeløp(BigDecimal.valueOf(1000))
                .build()
        );

        HbVedtaksbrevData data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        String generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        String fasit = les("/vedtaksbrev/SVP_forsett.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_vedtaksbrev_for_ES_god_tro_og_ingen_tilbakebetaling() throws Exception {
        HbVedtaksbrevFelles vedtaksbrevData = HbVedtaksbrevFelles.builder()
            .medErFødsel(true)
            .medAntallBarn(1)
            .medHovedresultat(VedtakResultatType.INGEN_TILBAKEBETALING)
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medYtelsetype(FagsakYtelseType.ENGANGSTØNAD)
            .medVarsletBeløp(BigDecimal.valueOf(10000))
            .medTotaltTilbakekrevesBeløp(BigDecimal.ZERO)
            .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.ZERO)
            .medTotaltRentebeløp(BigDecimal.ZERO)
            .medVarsletDato(LocalDate.of(2020, 4, 4))
            .medKlagefristUker(6)
            .build();

        List<HbVedtaksbrevPeriode> perioder = Arrays.asList(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(førsteNyttårsdag)
                .medHendelsetype(HendelseType.ES_FODSELSVILKAARET_TYPE)
                .medHendelseUndertype(EsHendelseUnderTyper.ES_MOTTAKER_FAR_MEDMOR)
                .medVilkårResultat(VilkårResultat.GOD_TRO)
                .medAktsomhetResultat(AnnenVurdering.GOD_TRO)
                .medRiktigBeløp(BigDecimal.ZERO)
                .medBeløpIBehold(BigDecimal.ZERO)
                .medFeilutbetaltBeløp(BigDecimal.valueOf(10000))
                .medTilbakekrevesBeløp(BigDecimal.ZERO)
                .medRenterBeløp(BigDecimal.ZERO)
                .build()
        );

        HbVedtaksbrevData data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        String generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        String fasit = les("/vedtaksbrev/ES_fødsel_god_tro.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    @Test
    public void skal_generere_vedtaksbrev_for_ES_adopsjon_og_grov_uaktsomhet() throws Exception {
        HbVedtaksbrevFelles vedtaksbrevData = HbVedtaksbrevFelles.builder()
            .medErAdopsjon(true)
            .medAntallBarn(5)
            .medHovedresultat(VedtakResultatType.FULL_TILBAKEBETALING)
            .medLovhjemmelVedtak("Folketrygdloven § 22-15")
            .medYtelsetype(FagsakYtelseType.ENGANGSTØNAD)
            .medVarsletBeløp(BigDecimal.valueOf(500000))
            .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(500000))
            .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(550000))
            .medTotaltRentebeløp(BigDecimal.valueOf(50000))
            .medVarsletDato(LocalDate.of(2020, 4, 4))
            .medKlagefristUker(6)
            .build();
        List<HbVedtaksbrevPeriode> perioder = Arrays.asList(
            HbVedtaksbrevPeriode.builder()
                .medPeriode(førsteNyttårsdag)
                .medHendelsetype(HendelseType.ES_ADOPSJONSVILKAARET_TYPE)
                .medHendelseUndertype(EsHendelseUnderTyper.ES_BARN_OVER_15)
                .medVilkårResultat(VilkårResultat.FEIL_OPPLYSNINGER_FRA_BRUKER)
                .medAktsomhetResultat(Aktsomhet.GROVT_UAKTSOM)
                .medSærligeGrunner(Arrays.asList(SærligGrunn.STØRRELSE_BELØP, SærligGrunn.GRAD_AV_UAKTSOMHET))
                .medRiktigBeløp(BigDecimal.valueOf(500000))
                .medFeilutbetaltBeløp(BigDecimal.valueOf(500000))
                .medTilbakekrevesBeløp(BigDecimal.valueOf(500000))
                .medRenterBeløp(BigDecimal.valueOf(50000))
                .build()
        );

        HbVedtaksbrevData data = new HbVedtaksbrevData(vedtaksbrevData, perioder);

        String generertBrev = TekstformatererVedtaksbrev.lagVedtaksbrevFritekst(data);
        String fasit = les("/vedtaksbrev/ES_adopsjon_grovt_uaktsom.txt");
        assertThat(generertBrev).isEqualToNormalizingNewlines(fasit);
    }

    private String les(String filnavn) throws IOException {
        try (InputStream resource = getClass().getResourceAsStream(filnavn);
             Scanner scanner = new Scanner(resource, "UTF-8")) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ?scanner.next() : null;
        }
    }

}
