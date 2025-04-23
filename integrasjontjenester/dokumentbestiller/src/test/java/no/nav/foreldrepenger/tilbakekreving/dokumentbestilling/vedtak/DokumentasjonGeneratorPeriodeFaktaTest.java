package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import static no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles.builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseTypePrYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUndertypePrHendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbPerson;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbSak;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbTotalresultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVarsel;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevDatoer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevPeriodeOgFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbKravgrunnlag;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVurderinger;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

/**
 * Brukes for å generere faktatekster for perioder. Resultatet er tekster med markup, som med "Insert markup"-macroen
 * kan limes inn i Confluence, og dermed bli formattert tekst.
 * <p>
 * Confluence:
 * FP/SVP/ES: https://confluence.adeo.no/display/TVF/Generert+dokumentasjon
 * FRISINN: https://confluence.adeo.no/display/MODNAV/Generert+dokumentasjon
 */
@Disabled("Kjøres ved behov for å regenerere dokumentasjon")
class DokumentasjonGeneratorPeriodeFaktaTest {

    private final Periode januar = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 31));

    @Test
    void list_ut_permutasjoner_for_FP() {
        var felles = lagFellesBuilder()
                .medSak(HbSak.build()
                        .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
                        .medErFødsel(true)
                        .medAntallBarn(1)
                        .build())
                .build();
        var resultat = lagFaktatekster(felles);
        prettyPrint(resultat);
    }

    @Test
    void list_ut_permutasjoner_for_FP_nynorsk() {
        var felles = lagFellesBuilder()
                .medSak(HbSak.build()
                        .medYtelsetype(FagsakYtelseType.FORELDREPENGER)
                        .medErFødsel(true)
                        .medAntallBarn(1)
                        .build())
                .medSpråkkode(Språkkode.NN)
                .build();
        var resultat = lagFaktatekster(felles);
        prettyPrint(resultat);
    }

    @Test
    void list_ut_permutasjoner_for_SVP() {
        var felles = lagFellesBuilder()
                .medSak(HbSak.build()
                        .medYtelsetype(FagsakYtelseType.SVANGERSKAPSPENGER)
                        .medErFødsel(true)
                        .medAntallBarn(1)
                        .build())
                .build();
        var resultat = lagFaktatekster(felles);
        prettyPrint(resultat);
    }

    @Test
    void list_ut_permutasjoner_for_SVP_nynorsk() {
        var felles = lagFellesBuilder()
                .medSak(HbSak.build()
                        .medYtelsetype(FagsakYtelseType.SVANGERSKAPSPENGER)
                        .medErFødsel(true)
                        .medAntallBarn(1)
                        .build())
                .medSpråkkode(Språkkode.NN)
                .build();
        var resultat = lagFaktatekster(felles);
        prettyPrint(resultat);
    }

    @Test
    void list_ut_permutasjoner_for_ES() {
        var felles = lagFellesBuilder()
                .medSak(HbSak.build()
                        .medYtelsetype(FagsakYtelseType.ENGANGSTØNAD)
                        .medErFødsel(true)
                        .medAntallBarn(1)
                        .build())
                .build();
        var resultat = lagFaktatekster(felles);
        prettyPrint(resultat);
    }

    @Test
    void list_ut_permutasjoner_for_ES_nynorsk() {
        var felles = lagFellesBuilder()
                .medSak(HbSak.build()
                        .medYtelsetype(FagsakYtelseType.ENGANGSTØNAD)
                        .medErFødsel(true)
                        .medAntallBarn(1)
                        .build())
                .medSpråkkode(Språkkode.NN)
                .build();
        var resultat = lagFaktatekster(felles);
        prettyPrint(resultat);
    }

    @Test
    void list_ut_permutasjoner_for_FRISINN() {
        var felles = lagFellesBuilder()
                .medSak(HbSak.build()
                        .medYtelsetype(FagsakYtelseType.FRISINN)
                        .medErFødsel(true)
                        .medAntallBarn(1)
                        .build())
                .build();
        var resultat = lagFaktatekster(felles);
        prettyPrint(resultat);
    }

    @Test
    void list_ut_permutasjoner_for_FRISINN_nynorsk() {
        var felles = lagFellesBuilder()
                .medSak(HbSak.build()
                        .medYtelsetype(FagsakYtelseType.FRISINN)
                        .medErFødsel(true)
                        .medAntallBarn(1)
                        .build())
                .medSpråkkode(Språkkode.NN)
                .build();
        var resultat = lagFaktatekster(felles);
        prettyPrint(resultat);
    }

    private void prettyPrint(Map<HendelseMedUndertype, String> resultat) {
        for (var entry : resultat.entrySet()) {
            var typer = entry.getKey();
            System.out.println("*[ " + typer.hendelseType().getNavn() + " - " + typer.hendelseUnderType().getNavn() + " ]*");
            var generertTekst = entry.getValue();
            var parametrisertTekst = generertTekst
                    .replaceAll(" 10\u00A0000\u00A0kroner", " <feilutbetalt beløp> kroner")
                    .replaceAll(" 33\u00A0333\u00A0kroner", " <utbetalt beløp> kroner")
                    .replaceAll(" 23\u00A0333\u00A0kroner", " <riktig beløp> kroner")
                    .replaceAll("Søker Søkersen", "<søkers navn>")
                    .replaceAll("2. mars 2018", "<opphørsdato søker døde>")
                    .replaceAll("3. mars 2018", "<opphørsdato barn døde>")
                    .replaceAll("4. mars 2018", "<opphørsdato ikke lenger gravid>")
                    .replaceAll("5. mars 2018", "<opphørsdato ikke omsorg>")
                    .replaceAll("ektefellen", "<ektefellen/partneren/samboeren>")
                    .replaceAll("\\[", "[ ")
                    .replaceAll("]", " ]");
            System.out.println(parametrisertTekst);
            System.out.println();
        }
    }

    private Map<HendelseMedUndertype, String> lagFaktatekster(HbVedtaksbrevFelles felles) {
        Map<HendelseMedUndertype, String> resultat = new LinkedHashMap<>();
        for (var undertype : getFeilutbetalingsårsaker(felles.getYtelsetype())) {
            var periode = lagPeriodeBuilder()
                    .medFakta(undertype.hendelseType(), undertype.hendelseUnderType())
                    .build();
            var data = new HbVedtaksbrevPeriodeOgFelles(felles, periode);
            var tekst = TekstformatererVedtaksbrev.lagFaktaTekst(data);
            resultat.put(undertype, tekst);
        }
        return resultat;
    }

    private HbVedtaksbrevPeriode.Builder lagPeriodeBuilder() {
        return HbVedtaksbrevPeriode.builder()
                .medPeriode(januar)
                .medVurderinger(HbVurderinger.builder()
                        .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                        .medAktsomhetResultat(AnnenVurdering.GOD_TRO)
                        .medVilkårResultat(VilkårResultat.GOD_TRO)
                        .medBeløpIBehold(BigDecimal.valueOf(5000))
                        .build())
                .medKravgrunnlag(HbKravgrunnlag.builder()
                        .medFeilutbetaltBeløp(BigDecimal.valueOf(10000))
                        .medRiktigBeløp(BigDecimal.valueOf(23333))
                        .medUtbetaltBeløp(BigDecimal.valueOf(33333))
                        .build())
                .medResultat(HbResultat.builder()
                        .medTilbakekrevesBeløp(BigDecimal.valueOf(5000))
                        .medTilbakekrevesBeløpUtenSkatt(BigDecimal.valueOf(4002))
                        .medRenterBeløp(BigDecimal.ZERO)
                        .build())
                ;
    }

    private HbVedtaksbrevFelles.Builder lagFellesBuilder() {
        var datoer = HbVedtaksbrevDatoer.builder().medDatoer(
                        LocalDate.of(2018, 3, 2)
                        , LocalDate.of(2018, 3, 3)
                        , LocalDate.of(2018, 3, 4)
                        , LocalDate.of(2018, 3, 5))
                .build();
        return builder()
                .medLovhjemmelVedtak("Folketrygdloven")
                .medVedtakResultat(HbTotalresultat.builder()
                        .medHovedresultat(VedtakResultatType.FULL_TILBAKEBETALING)
                        .medTotaltRentebeløp(BigDecimal.valueOf(1000))
                        .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(10000))
                        .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(11000))
                        .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.valueOf(6855))
                        .build())
                .medVarsel(HbVarsel.builder()
                        .medVarsletBeløp(BigDecimal.valueOf(10000))
                        .medVarsletDato(LocalDate.now().minusDays(100))
                        .build())
                .medKonfigurasjon(HbKonfigurasjon.builder()
                        .medKlagefristUker(6)
                        .build())
                .medSøker(HbPerson.builder()
                        .medNavn("Søker Søkersen")
                        .medErGift(true)
                        .build())
                .medDatoer(datoer);
    }

    private List<HendelseMedUndertype> getFeilutbetalingsårsaker(FagsakYtelseType ytelseType) {
        var hendelseTyper = HendelseTypePrYtelseType.getHendelsetyper(ytelseType);
        var hendelseUndertypePrHendelseType = HendelseUndertypePrHendelseType.getHendelsetypeHierarki();

        List<HendelseMedUndertype> resultat = new ArrayList<>();
        for (var hendelseType : hendelseTyper) {
            for (var hendelseUnderType : hendelseUndertypePrHendelseType.get(hendelseType)) {
                resultat.add(new HendelseMedUndertype(hendelseType, hendelseUnderType));
            }
        }

        resultat.sort(new HendelseMedUndertypeComparator());
        return resultat;
    }

    static class HendelseMedUndertypeComparator implements Comparator<HendelseMedUndertype> {

        @Override
        public int compare(HendelseMedUndertype o1, HendelseMedUndertype o2) {
            var hendelseCompare = Long.compare(
                    o1.hendelseType().getSortering(),
                    o2.hendelseType().getSortering());
            if (hendelseCompare != 0) {
                return hendelseCompare;
            }
            return Long.compare(
                    o1.hendelseUnderType().getSortering(),
                    o2.hendelseUnderType().getSortering()
            );
        }
    }

}
