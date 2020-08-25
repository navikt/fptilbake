package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbPerson;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbSak;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbTotalresultat;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVarsel;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.HbVedtaksbrevFelles;

@Ignore("Kjøres ved behov for å regenerere dokumentasjon")
public class DokumentasjonGeneratorVedtakOppsummering {

    private final static LocalDate JANUAR_15 = LocalDate.of(2020, 1, 15);

    private static List<VedtakResultatType> tilbakekrevingsResultat = new ArrayList<>();
    static {
        tilbakekrevingsResultat.add(VedtakResultatType.FULL_TILBAKEBETALING);
        tilbakekrevingsResultat.add(VedtakResultatType.DELVIS_TILBAKEBETALING);
        tilbakekrevingsResultat.add(VedtakResultatType.INGEN_TILBAKEBETALING);
    }
    private static boolean[] trueFalse = new boolean[] { true, false};

    @Test
    public void list_ut_vedtak_start_for_fp() {
        FagsakYtelseType ytelseType = FagsakYtelseType.FORELDREPENGER;
        Språkkode nb = Språkkode.nb;
        for (VedtakResultatType resultatType : tilbakekrevingsResultat) {
            for (boolean medVarsel : trueFalse){
                genererVedtakStart(ytelseType, nb, resultatType, medVarsel, 1000, 10, 100);
                genererVedtakStart(ytelseType, nb, resultatType, medVarsel, 1000, 0, 100);
                genererVedtakStart(ytelseType, nb, resultatType, medVarsel, 1000, 10, 0);
                genererVedtakStart(ytelseType, nb, resultatType, medVarsel, 1000, 0, 0);
            }
        }
    }

    @Test
    public void list_ut_vedtak_start_for_fp_nynorsk() {
        FagsakYtelseType ytelseType = FagsakYtelseType.FORELDREPENGER;
        Språkkode språkkode = Språkkode.nn;
        for (VedtakResultatType resultatType : tilbakekrevingsResultat) {
            for (boolean medVarsel : trueFalse){
                genererVedtakStart(ytelseType, språkkode, resultatType, medVarsel, 1000, 10, 100);
                genererVedtakStart(ytelseType, språkkode, resultatType, medVarsel, 1000, 0, 100);
                genererVedtakStart(ytelseType, språkkode, resultatType, medVarsel, 1000, 10, 0);
                genererVedtakStart(ytelseType, språkkode, resultatType, medVarsel, 1000, 0, 0);
            }
        }
    }

    @Test
    public void list_ut_vedtak_start_for_es() {
        FagsakYtelseType ytelseType = FagsakYtelseType.ENGANGSTØNAD;
        Språkkode nb = Språkkode.nb;
        for (VedtakResultatType resultatType : tilbakekrevingsResultat) {
            for (boolean medVarsel : trueFalse){
                genererVedtakStart(ytelseType, nb, resultatType, medVarsel, 1000, 10, 0);
                genererVedtakStart(ytelseType, nb, resultatType, medVarsel, 1000, 0, 0);
            }
        }
    }

    @Test
    public void list_ut_vedtak_start_for_es_nynorsk() {
        FagsakYtelseType ytelseType = FagsakYtelseType.ENGANGSTØNAD;
        Språkkode språkkode = Språkkode.nn;
        for (VedtakResultatType resultatType : tilbakekrevingsResultat) {
            for (boolean medVarsel : trueFalse){
                genererVedtakStart(ytelseType, språkkode, resultatType, medVarsel, 1000, 10, 0);
                genererVedtakStart(ytelseType, språkkode, resultatType, medVarsel, 1000, 0, 0);
            }
        }
    }

    @Test
    public void list_ut_vedtak_start_for_svp() {
        FagsakYtelseType ytelseType = FagsakYtelseType.SVANGERSKAPSPENGER;
        Språkkode nb = Språkkode.nb;
        for (VedtakResultatType resultatType : tilbakekrevingsResultat) {
            for (boolean medVarsel : trueFalse){
                genererVedtakStart(ytelseType, nb, resultatType, medVarsel, 1000, 10, 100);
                genererVedtakStart(ytelseType, nb, resultatType, medVarsel, 1000, 0, 100);
                genererVedtakStart(ytelseType, nb, resultatType, medVarsel, 1000, 10, 0);
                genererVedtakStart(ytelseType, nb, resultatType, medVarsel, 1000, 0, 0);
            }
        }
    }

    @Test
    public void list_ut_vedtak_start_for_svp_nynorsk() {
        FagsakYtelseType ytelseType = FagsakYtelseType.SVANGERSKAPSPENGER;
        Språkkode språkkode = Språkkode.nn;
        for (VedtakResultatType resultatType : tilbakekrevingsResultat) {
            for (boolean medVarsel : trueFalse){
                genererVedtakStart(ytelseType, språkkode, resultatType, medVarsel, 1000, 10, 100);
                genererVedtakStart(ytelseType, språkkode, resultatType, medVarsel, 1000, 0, 100);
                genererVedtakStart(ytelseType, språkkode, resultatType, medVarsel, 1000, 10, 0);
                genererVedtakStart(ytelseType, språkkode, resultatType, medVarsel, 1000, 0, 0);
            }
        }
    }

    @Test
    public void list_ut_vedtak_start_for_frisinn() {
        FagsakYtelseType ytelseType = FagsakYtelseType.FRISINN;
        Språkkode nb = Språkkode.nb;
        for (VedtakResultatType resultatType : tilbakekrevingsResultat) {
            for (boolean medVarsel : trueFalse){
                genererVedtakStart(ytelseType, nb, resultatType, medVarsel, 1000, 0, 100);
                genererVedtakStart(ytelseType, nb, resultatType, medVarsel, 1000, 0, 0);
            }
        }
    }

    @Test
    public void list_ut_vedtak_start_for_frisinn_nynorsk() {
        FagsakYtelseType ytelseType = FagsakYtelseType.FRISINN;
        Språkkode språkkode = Språkkode.nn;
        for (VedtakResultatType resultatType : tilbakekrevingsResultat) {
            for (boolean medVarsel : trueFalse){
                genererVedtakStart(ytelseType, språkkode, resultatType, medVarsel, 1000, 0, 100);
                genererVedtakStart(ytelseType, språkkode, resultatType, medVarsel, 1000, 0, 0);
            }
        }
    }

    private void genererVedtakStart(FagsakYtelseType ytelseType,
                                    Språkkode språkkode,
                                    VedtakResultatType tilbakebetaling,
                                    boolean medVarsel,
                                    int totalt,
                                    int renter,
                                    int skatt) {
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
            .medLovhjemmelVedtak("");
        if (medVarsel) {
            fellesBuilder.medVarsel(HbVarsel.builder()
                .medVarsletBeløp(1000l)
                .medVarsletDato(JANUAR_15)
                .build());
        }
        HbVedtaksbrevFelles felles = fellesBuilder
            .medSpråkkode(språkkode).build();
        String vedtakStart = TekstformatererVedtaksbrev.lagVedtakStart(felles);

        prettyPrint(tilbakebetaling, medVarsel, renter, skatt, vedtakStart);
    }

    private void prettyPrint(VedtakResultatType tilbakebetaling,
                             boolean medVarsel,
                             int renter,
                             int skatt,
                             String generertTekst) {
        System.out.println("*[ " + tilbakebetaling.getNavn() + " - "
            + (medVarsel ? "med varsel" : "uten varsel") + " - "
            + (skatt != 0 ? "med skatt" : "uten skatt") + " - "
            + (renter != 0 ? "med renter" : "uten renter") + " ]*");
        String parametrisertTekst = generertTekst
            .replaceAll(" 1\u00A0010\u00A0kroner", " <skyldig beløp> kroner")
            .replaceAll(" 1\u00A0000\u00A0kroner", " <skyldig beløp> kroner")
            .replaceAll(" 910\u00A0kroner", " <skyldig beløp uten skatt> kroner")
            .replaceAll(" 900\u00A0kroner", " <skyldig beløp uten skatt> kroner")
            .replaceAll("15. januar 2020", medVarsel ? "<varseldato>" : "<vedtaksdato>")
            .replaceAll("\\[", "[ ")
            .replaceAll("]", " ]");

        System.out.println(parametrisertTekst);
        System.out.println();
    }

}
