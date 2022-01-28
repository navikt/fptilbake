package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbPerson;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbSak;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbTotalresultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVarsel;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles;

/**
 * Brukes for å generere tekster for oppsummering av vedtaket i vedtaksbrevet. Resultatet er tekster med markup, som med
 * "Insert markup"-macroen kan limes inn i Confluence, og dermed bli formattert tekst.
 * <p>
 * Confluence:
 * FP/SVP/ES: https://confluence.adeo.no/display/TVF/Generert+dokumentasjon
 * FRISINN: https://confluence.adeo.no/display/MODNAV/Generert+dokumentasjon
 */
@Disabled("Kjøres ved behov for å regenerere dokumentasjon")
public class DokumentasjonGeneratorVedtakOppsummering {

    private final static LocalDate JANUAR_15 = LocalDate.of(2020, 1, 15);

    private static List<VedtakResultatType> tilbakekrevingsResultat = new ArrayList<>();

    static {
        tilbakekrevingsResultat.add(VedtakResultatType.FULL_TILBAKEBETALING);
        tilbakekrevingsResultat.add(VedtakResultatType.DELVIS_TILBAKEBETALING);
        tilbakekrevingsResultat.add(VedtakResultatType.INGEN_TILBAKEBETALING);
    }

    private static boolean[] trueFalse = new boolean[]{true, false};

    @Test
    public void list_ut_vedtak_start_for_fp() {
        FagsakYtelseType ytelseType = FagsakYtelseType.FORELDREPENGER;
        Språkkode nb = Språkkode.nb;
        for (VedtakResultatType resultatType : tilbakekrevingsResultat) {
            for (boolean medVarsel : trueFalse) {
                listVedtakStartAllePermutasjoner(ytelseType, nb, resultatType, medVarsel);
            }
            listVedtakStartMedKorrigertBeløpAllePermutasjoner(ytelseType, nb, resultatType);
        }
    }

    @Test
    public void list_ut_vedtak_start_for_fp_nynorsk() {
        FagsakYtelseType ytelseType = FagsakYtelseType.FORELDREPENGER;
        Språkkode språkkode = Språkkode.nn;
        for (VedtakResultatType resultatType : tilbakekrevingsResultat) {
            for (boolean medVarsel : trueFalse) {
                listVedtakStartAllePermutasjoner(ytelseType, språkkode, resultatType, medVarsel);
            }
            listVedtakStartMedKorrigertBeløpAllePermutasjoner(ytelseType, språkkode, resultatType);
        }
    }

    @Test
    public void list_ut_vedtak_start_for_es() {
        FagsakYtelseType ytelseType = FagsakYtelseType.ENGANGSTØNAD;
        Språkkode nb = Språkkode.nb;
        for (VedtakResultatType resultatType : tilbakekrevingsResultat) {
            for (boolean medVarsel : trueFalse) {
                listVedtakStartUtenSkatt(ytelseType, nb, resultatType, medVarsel);
            }
            listVedtakStartMedKorrigertBeløpUtenSkatt(ytelseType, nb, resultatType);
        }
    }

    @Test
    public void list_ut_vedtak_start_for_es_nynorsk() {
        FagsakYtelseType ytelseType = FagsakYtelseType.ENGANGSTØNAD;
        Språkkode språkkode = Språkkode.nn;
        for (VedtakResultatType resultatType : tilbakekrevingsResultat) {
            for (boolean medVarsel : trueFalse) {
                listVedtakStartUtenSkatt(ytelseType, språkkode, resultatType, medVarsel);
            }
            listVedtakStartMedKorrigertBeløpUtenSkatt(ytelseType, språkkode, resultatType);
        }
    }

    @Test
    public void list_ut_vedtak_start_for_svp() {
        FagsakYtelseType ytelseType = FagsakYtelseType.SVANGERSKAPSPENGER;
        Språkkode nb = Språkkode.nb;
        for (VedtakResultatType resultatType : tilbakekrevingsResultat) {
            for (boolean medVarsel : trueFalse) {
                listVedtakStartAllePermutasjoner(ytelseType, nb, resultatType, medVarsel);
            }
            listVedtakStartMedKorrigertBeløpAllePermutasjoner(ytelseType, nb, resultatType);
        }
    }

    @Test
    public void list_ut_vedtak_start_for_svp_nynorsk() {
        FagsakYtelseType ytelseType = FagsakYtelseType.SVANGERSKAPSPENGER;
        Språkkode språkkode = Språkkode.nn;
        for (VedtakResultatType resultatType : tilbakekrevingsResultat) {
            for (boolean medVarsel : trueFalse) {
                listVedtakStartAllePermutasjoner(ytelseType, språkkode, resultatType, medVarsel);
            }
            listVedtakStartMedKorrigertBeløpAllePermutasjoner(ytelseType, språkkode, resultatType);
        }
    }

    @Test
    public void list_ut_vedtak_start_for_frisinn() {
        FagsakYtelseType ytelseType = FagsakYtelseType.FRISINN;
        Språkkode nb = Språkkode.nb;
        for (VedtakResultatType resultatType : tilbakekrevingsResultat) {
            for (boolean medVarsel : trueFalse) {
                listVedtakStartUtenRenter(ytelseType, nb, resultatType, medVarsel);
            }
            listVedtakStartMedKorrigertBeløpUtenRenter(ytelseType, nb, resultatType);
        }
    }

    @Test
    public void list_ut_vedtak_start_for_frisinn_nynorsk() {
        FagsakYtelseType ytelseType = FagsakYtelseType.FRISINN;
        Språkkode språkkode = Språkkode.nn;
        for (VedtakResultatType resultatType : tilbakekrevingsResultat) {
            for (boolean medVarsel : trueFalse) {
                listVedtakStartUtenRenter(ytelseType, språkkode, resultatType, medVarsel);
            }
            listVedtakStartMedKorrigertBeløpUtenRenter(ytelseType, språkkode, resultatType);
        }
    }

    private void listVedtakStartAllePermutasjoner(FagsakYtelseType ytelseType, Språkkode nb, VedtakResultatType resultatType, boolean medVarsel) {
        genererVedtakStart(ytelseType, nb, resultatType, medVarsel, 1000, 10, 100);
        genererVedtakStart(ytelseType, nb, resultatType, medVarsel, 1000, 0, 100);
        genererVedtakStart(ytelseType, nb, resultatType, medVarsel, 1000, 10, 0);
        genererVedtakStart(ytelseType, nb, resultatType, medVarsel, 1000, 0, 0);
    }

    private void listVedtakStartUtenRenter(FagsakYtelseType ytelseType, Språkkode nb, VedtakResultatType resultatType, boolean medVarsel) {
        genererVedtakStart(ytelseType, nb, resultatType, medVarsel, 1000, 0, 100);
        genererVedtakStart(ytelseType, nb, resultatType, medVarsel, 1000, 0, 0);
    }

    private void listVedtakStartUtenSkatt(FagsakYtelseType ytelseType, Språkkode nb, VedtakResultatType resultatType, boolean medVarsel) {
        genererVedtakStart(ytelseType, nb, resultatType, medVarsel, 1000, 10, 0);
        genererVedtakStart(ytelseType, nb, resultatType, medVarsel, 1000, 0, 0);
    }

    private void listVedtakStartMedKorrigertBeløpAllePermutasjoner(FagsakYtelseType ytelseType, Språkkode nb, VedtakResultatType resultatType) {
        genererVedtakStartMedKorrigertBeløp(ytelseType, nb, resultatType, 1000, 10, 100);
        genererVedtakStartMedKorrigertBeløp(ytelseType, nb, resultatType, 1000, 0, 100);
        genererVedtakStartMedKorrigertBeløp(ytelseType, nb, resultatType, 1000, 10, 0);
        genererVedtakStartMedKorrigertBeløp(ytelseType, nb, resultatType, 1000, 0, 0);
    }

    private void listVedtakStartMedKorrigertBeløpUtenRenter(FagsakYtelseType ytelseType, Språkkode nb, VedtakResultatType resultatType) {
        genererVedtakStartMedKorrigertBeløp(ytelseType, nb, resultatType, 1000, 0, 100);
        genererVedtakStartMedKorrigertBeløp(ytelseType, nb, resultatType, 1000, 0, 0);
    }

    private void listVedtakStartMedKorrigertBeløpUtenSkatt(FagsakYtelseType ytelseType, Språkkode nb, VedtakResultatType resultatType) {
        genererVedtakStartMedKorrigertBeløp(ytelseType, nb, resultatType, 1000, 10, 0);
        genererVedtakStartMedKorrigertBeløp(ytelseType, nb, resultatType, 1000, 0, 0);
    }

    private void genererVedtakStart(FagsakYtelseType ytelseType,
                                    Språkkode språkkode,
                                    VedtakResultatType tilbakebetaling,
                                    boolean medVarsel,
                                    int totalt,
                                    int renter,
                                    int skatt) {
        genererVedtakStart(ytelseType, språkkode, tilbakebetaling, medVarsel, totalt, renter, skatt, false);
    }

    private void genererVedtakStartMedKorrigertBeløp(FagsakYtelseType ytelseType,
                                                     Språkkode språkkode,
                                                     VedtakResultatType tilbakebetaling,
                                                     int totalt,
                                                     int renter,
                                                     int skatt) {
        genererVedtakStart(ytelseType, språkkode, tilbakebetaling, true, totalt, renter, skatt, true);
    }

    private void genererVedtakStart(FagsakYtelseType ytelseType,
                                    Språkkode språkkode,
                                    VedtakResultatType tilbakebetaling,
                                    boolean medVarsel,
                                    int totalt,
                                    int renter,
                                    int skatt,
                                    boolean medKorrigertBeløp) {
        HbSak.Builder sakBuilder = HbSak.build()
                .medYtelsetype(ytelseType)
                .medAntallBarn(1)
                .medErFødsel(true);
        if (!medVarsel) {
            sakBuilder.medDatoFagsakvedtak(JANUAR_15);
        }
        HbSak sak = sakBuilder.build();

        int totaltMedRenter = totalt + renter;
        HbTotalresultat resultat = HbTotalresultat.builder()
                .medHovedresultat(tilbakebetaling)
                .medTotaltTilbakekrevesBeløp(BigDecimal.valueOf(totalt))
                .medTotaltTilbakekrevesBeløpMedRenter(BigDecimal.valueOf(totaltMedRenter))
                .medTotaltTilbakekrevesBeløpMedRenterUtenSkatt(BigDecimal.valueOf(totaltMedRenter - skatt))
                .medTotaltRentebeløp(BigDecimal.valueOf(renter))
                .build();
        HbVedtaksbrevFelles.Builder fellesBuilder = HbVedtaksbrevFelles.builder()
                .medSak(sak)
                .medVedtakResultat(resultat)
                .medSøker(HbPerson.builder()
                        .medNavn("")
                        .build())
                .medKonfigurasjon(HbKonfigurasjon.builder()
                        .medKlagefristUker(6)
                        .build())
                .medLovhjemmelVedtak("")
                .medVedtaksbrevType(VedtaksbrevType.ORDINÆR);
        if (medKorrigertBeløp) {
            fellesBuilder.medVarsel(HbVarsel.builder()
                            .medVarsletBeløp(25000L)
                            .medVarsletDato(JANUAR_15)
                            .build())
                    .medErFeilutbetaltBeløpKorrigertNed(true)
                    .medTotaltFeilutbetaltBeløp(BigDecimal.valueOf(1000));
        } else if (medVarsel) {
            fellesBuilder.medVarsel(HbVarsel.builder()
                    .medVarsletBeløp(1000L)
                    .medVarsletDato(JANUAR_15)
                    .build());
        }
        HbVedtaksbrevFelles felles = fellesBuilder
                .medSpråkkode(språkkode).build();
        String vedtakStart = TekstformatererVedtaksbrev.lagVedtakStart(felles);

        prettyPrint(tilbakebetaling, medVarsel, renter, skatt, vedtakStart, medKorrigertBeløp);
    }

    private void prettyPrint(VedtakResultatType tilbakebetaling,
                             boolean medVarsel,
                             int renter,
                             int skatt,
                             String generertTekst,
                             boolean medKorrigertBeløp) {
        System.out.println("*[ "
                + tilbakebetaling.getNavn() + " - "
                + (medVarsel ? "med varsel" : "uten varsel") + " - "
                + (skatt != 0 ? "med skatt" : "uten skatt") + " - "
                + (renter != 0 ? "med renter" : "uten renter")
                + (medKorrigertBeløp ? " - med korrigert beløp" : "")
                + " ]*");
        String parametrisertTekst = generertTekst
                .replaceAll(" 1\u00A0010\u00A0kroner", " <skyldig beløp> kroner")
                .replaceAll(" 1\u00A0000\u00A0kroner", " <skyldig beløp> kroner")
                .replaceAll(" 910\u00A0kroner", " <skyldig beløp uten skatt> kroner")
                .replaceAll(" 900\u00A0kroner", " <skyldig beløp uten skatt> kroner")
                .replaceAll(" 25\u00A0000\u00A0kroner", " <varslet beløp> kroner")
                .replaceAll("15. januar 2020", medVarsel ? "<varseldato>" : "<vedtaksdato>")
                .replaceAll("\\[", "[ ")
                .replaceAll("]", " ]");

        System.out.println(parametrisertTekst);
        System.out.println();
    }

}
