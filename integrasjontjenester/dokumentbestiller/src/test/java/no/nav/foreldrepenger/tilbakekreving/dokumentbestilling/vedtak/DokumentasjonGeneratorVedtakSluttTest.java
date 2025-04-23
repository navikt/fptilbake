package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbBehandling;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbPerson;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbSak;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbTotalresultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVarsel;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevData;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbKravgrunnlag;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbResultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode.HbVurderinger;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

/**
 * Brukes for å generere tekster for slutten av vedtaksbrevet. Resultatet er tekster med markup, som med "Insert markup"-macroen
 * kan limes inn i Confluence, og dermed bli formattert tekst.
 * <p>
 * Confluence:
 * FP/SVP/ES: https://confluence.adeo.no/display/TVF/Generert+dokumentasjon
 * FRISINN: https://confluence.adeo.no/display/MODNAV/Generert+dokumentasjon
 */
@Disabled("Kjøres ved behov for å regenerere dokumentasjon")
class DokumentasjonGeneratorVedtakSluttTest {

    private static final Periode PERIODE1 = Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 16));
    private static final Periode PERIODE2 = Periode.of(LocalDate.of(2019, 1, 17), LocalDate.of(2019, 1, 31));

    private static final boolean[] trueFalse = new boolean[]{true, false};

    @Test
    void list_ut_vedtak_slutt() {
        lagVedtakSluttTekster(FagsakYtelseType.FORELDREPENGER, null, VedtakResultatType.FULL_TILBAKEBETALING);
        lagVedtakSluttTekster(FagsakYtelseType.FORELDREPENGER, null, VedtakResultatType.INGEN_TILBAKEBETALING);
    }

    @Test
    void list_ut_vedtak_slutt_nn() {
        lagVedtakSluttTekster(FagsakYtelseType.FORELDREPENGER, Språkkode.NN, VedtakResultatType.FULL_TILBAKEBETALING);
        lagVedtakSluttTekster(FagsakYtelseType.FORELDREPENGER, Språkkode.NN, VedtakResultatType.INGEN_TILBAKEBETALING);
    }

    @Test
    void list_ut_vedtak_slutt_es() {
        lagVedtakSluttTekster(FagsakYtelseType.ENGANGSTØNAD, null, VedtakResultatType.FULL_TILBAKEBETALING, false);
        lagVedtakSluttTekster(FagsakYtelseType.ENGANGSTØNAD, null, VedtakResultatType.INGEN_TILBAKEBETALING, false);
    }

    @Test
    void list_ut_vedtak_slutt_es_nn() {
        lagVedtakSluttTekster(FagsakYtelseType.ENGANGSTØNAD, Språkkode.NN, VedtakResultatType.FULL_TILBAKEBETALING, false);
        lagVedtakSluttTekster(FagsakYtelseType.ENGANGSTØNAD, Språkkode.NN, VedtakResultatType.INGEN_TILBAKEBETALING, false);
    }

    @Test
    void list_ut_vedtak_slutt_frisinn() {
        lagVedtakSluttTekster(FagsakYtelseType.FRISINN, null, VedtakResultatType.FULL_TILBAKEBETALING, false);
        lagVedtakSluttTekster(FagsakYtelseType.FRISINN, null, VedtakResultatType.INGEN_TILBAKEBETALING, false);
    }

    @Test
    void list_ut_vedtak_slutt_frisinn_nn() {
        lagVedtakSluttTekster(FagsakYtelseType.FRISINN, Språkkode.NN, VedtakResultatType.FULL_TILBAKEBETALING, false);
        lagVedtakSluttTekster(FagsakYtelseType.FRISINN, Språkkode.NN, VedtakResultatType.INGEN_TILBAKEBETALING, false);
    }

    private void lagVedtakSluttTekster(FagsakYtelseType ytelsetype, Språkkode språkkode, VedtakResultatType resultatType) {
        for (var medSkattetrekk : trueFalse) {
            lagVedtakSluttTekster(ytelsetype, språkkode, resultatType, medSkattetrekk);
        }
    }

    private void lagVedtakSluttTekster(FagsakYtelseType ytelsetype, Språkkode språkkode, VedtakResultatType resultatType, boolean medSkattetrekk) {
        for (var flerePerioder : trueFalse) {
            for (var flereLovhjemler : trueFalse) {
                for (var medVerge : trueFalse) {
                    for (var feilutbetaltBeløpBortfalt : trueFalse) {
                        lagVedtakSluttTekster(ytelsetype, språkkode, resultatType, flerePerioder, medSkattetrekk, flereLovhjemler, medVerge, feilutbetaltBeløpBortfalt, false);
                        if (!VedtakResultatType.INGEN_TILBAKEBETALING.equals(resultatType)) {
                            lagVedtakSluttTekster(ytelsetype, språkkode, resultatType, flerePerioder, medSkattetrekk, flereLovhjemler, medVerge, feilutbetaltBeløpBortfalt, true);
                        }
                    }
                }
            }
        }
    }

    private void lagVedtakSluttTekster(FagsakYtelseType ytelsetype,
                                       Språkkode språkkode,
                                       VedtakResultatType resultatType,
                                       boolean flerePerioder,
                                       boolean medSkattetrekk,
                                       boolean flereLovhjemler,
                                       boolean medVerge,
                                       boolean feilutbetaltBeløpBortfalt, boolean erRevurdering) {
        var felles = lagFellesdel(ytelsetype, språkkode, resultatType, medSkattetrekk, flereLovhjemler, medVerge, feilutbetaltBeløpBortfalt, erRevurdering);
        var perioder = lagPerioder(flerePerioder);
        var sluttTekst = TekstformatererVedtaksbrev.lagVedtakSluttTekst(new HbVedtaksbrevData(felles, perioder));

        System.out.println();
        System.out.println(overskrift(flerePerioder, medSkattetrekk, flereLovhjemler, medVerge, feilutbetaltBeløpBortfalt, erRevurdering));
        System.out.println(prettyprint(sluttTekst));
    }

    private HbVedtaksbrevFelles lagFellesdel(FagsakYtelseType ytelsetype,
                                             Språkkode språkkode,
                                             VedtakResultatType vedtakResultatType,
                                             boolean medSkattetrekk,
                                             boolean flereLovhjemler,
                                             boolean medVerge,
                                             boolean feilutbetaltBeløpBortfalt,
                                             boolean erRevurdering) {
        var builder = HbVedtaksbrevFelles.builder()
                .medSak(HbSak.build()
                        .medYtelsetype(ytelsetype)
                        .medErFødsel(true)
                        .medAntallBarn(1)
                        .build())
                .medVedtakResultat(HbTotalresultat.builder()
                        .medHovedresultat(vedtakResultatType)
                        .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(1000))
                        .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(1100))
                        .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.valueOf(medSkattetrekk ? 900 : 1100))
                        .medTotaltRentebeløp(BigDecimal.valueOf(100))
                        .build())
                .medLovhjemmelVedtak(flereLovhjemler ? "lovhjemmel1 og lovhjemmel2" : "[lovhjemmel]")
                .medVarsel(HbVarsel.builder()
                        .medVarsletBeløp(BigDecimal.valueOf(1000))
                        .medVarsletDato(LocalDate.of(2020, 4, 4))
                        .build())
                .medKonfigurasjon(HbKonfigurasjon.builder()
                        .medKlagefristUker(4)
                        .build())
                .medSøker(HbPerson.builder()
                        .medNavn("Søker Søkersen")
                        .medErGift(true)
                        .build())
                .medVedtaksbrevType(feilutbetaltBeløpBortfalt ? VedtaksbrevType.FRITEKST_FEILUTBETALING_BORTFALT : VedtaksbrevType.ORDINÆR)
                .medSpråkkode(språkkode != null ? språkkode : Språkkode.NB);
        if (erRevurdering) {
            builder
                    .medBehandling(HbBehandling.builder()
                            .medErRevurdering(true)
                            .medOriginalBehandlingDatoFagsakvedtak(PERIODE1.getFom())
                            .build());
        }
        if (medVerge) {
            builder
                    .medFinnesVerge(medVerge)
                    .medAnnenMottakerNavn("[annen mottaker]");
        }
        return builder.build();
    }

    private List<HbVedtaksbrevPeriode> lagPerioder(boolean flerePerioder) {
        List<HbVedtaksbrevPeriode> perioder = new ArrayList<>();
        var hbVedtaksbrevPeriode = lagPeriode(PERIODE1);
        perioder.add(hbVedtaksbrevPeriode);
        if (flerePerioder) {
            hbVedtaksbrevPeriode = lagPeriode(PERIODE2);
            perioder.add(hbVedtaksbrevPeriode);
        }
        return perioder;
    }

    private HbVedtaksbrevPeriode lagPeriode(Periode periode) {
        return HbVedtaksbrevPeriode.builder()
                .medPeriode(periode)
                .medVurderinger(HbVurderinger.builder()
                        .medForeldelsevurdering(ForeldelseVurderingType.IKKE_VURDERT)
                        .medVilkårResultat(VilkårResultat.FORSTO_BURDE_FORSTÅTT)
                        .medAktsomhetResultat(Aktsomhet.SIMPEL_UAKTSOM)
                        .medSærligeGrunner(Collections.emptyList(), null, null)
                        .build())
                .medKravgrunnlag(HbKravgrunnlag.builder()
                        .medFeilutbetaltBeløp(BigDecimal.valueOf(1000))
                        .build())
                .medResultat(HbResultat.builder()
                        .medTilbakekrevesBeløp(BigDecimal.valueOf(1000))
                        .medTilbakekrevesBeløpUtenSkatt(BigDecimal.valueOf(800))
                        .medRenterBeløp(BigDecimal.ZERO)
                        .build())
                .medFakta(HendelseType.FP_UTTAK_GRADERT_TYPE, HendelseUnderType.GRADERT_UTTAK)
                .build();
    }

    private String overskrift(boolean flerePerioder,
                              boolean medSkattetrekk,
                              boolean flereLovhjemler,
                              boolean medVerge,
                              boolean feilutbetaltBeløpBortfalt,
                              boolean erRevurdering) {
        return "*[ " + (flerePerioder ? "flere perioder" : "en periode")
                + " - " + (medSkattetrekk ? "med skattetrekk" : "uten skattetrekk")
                + " - " + (flereLovhjemler ? "flere lovhjemmel" : "en lovhjemmel")
                + " - " + (medVerge ? "med verge" : "uten verge")
                + " - " + (feilutbetaltBeløpBortfalt ? "feilutbetalt beløp bortfalt" : "ordinær")
                + (erRevurdering ? " - revurdering" : "")
                + " ]*";
    }

    private String prettyprint(String s) {
        return s.replace("_Lovhjemlene vi har brukt", "*_Lovhjemlene vi har brukt_*")
                .replace("_Lovhjemmelen vi har brukt", "*_Lovhjemmelen vi har brukt_*")
                .replace("_Skatt", "\n*_Skatt_*")
                .replace("_Hvordan betale tilbake pengene du skylder", "\n*_Hvordan betale tilbake pengene du skylder_*")
                .replace("_Du har rett til å klage", "\n*_Du har rett til å klage_*")
                .replace("_Du har rett til innsyn", "\n*_Du har rett til innsyn_*")
                .replace("_Har du spørsmål?", "\n*_Har du spørsmål?_*")
                .replace("_Lovhjemler vi har brukt", "*_Lovhjemler vi har brukt_*")
                .replace("_Korleis betale tilbake pengane du skuldar", "\n*_Korleis betale tilbake pengane du skuldar_*")
                .replace("lovhjemmel1 og lovhjemmel2", "<lovhjemler her>")
                .replace("[lovhjemmel]", "<lovhjemmel her>")
                .replace("[annen mottaker]", "<annen mottaker>")
                .replace("4 uker", "<klagefrist> uker")
                .replace("4 veker", "<klagefrist> veker")
                .replaceAll("\\[", "[ ")
                .replaceAll("]", " ]");
    }
}
