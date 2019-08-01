package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.util;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.PeriodeMedBrevtekst;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Avsnitt;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.Underavsnitt;

public class AvsnittUtil {

    private AvsnittUtil() {
        // for static access
    }

    public static Avsnitt lagIntroduksjonsavsnitt(
        Long sumBeløpSomSkalTilbakekreves,
        Long sumFeilutbetaling,
        LocalDate varselbrevSendtUt,
        String fagsaktypenavnPåSpråk,
        String oppsummeringFritekst) {

        Underavsnitt underavsnitt = new Underavsnitt.Builder()
            .medBrødtekst(String.format
                ("Vi sendte deg et varsel %s hvor vi fortalte at du hadde fått utbetalt %d kroner for mye. Du må betale %d kroner tilbake. ",
                    BrevUtil.konverterFraLocaldateTilTekst(varselbrevSendtUt),
                    sumFeilutbetaling,
                    sumBeløpSomSkalTilbakekreves))
            .medErFritekstTillatt(true)
            .medFritekst(oppsummeringFritekst)
            .build();

        return new Avsnitt.Builder()
            .medOverskrift(TittelOverskriftUtil.finnOverskriftVedtaksbrev(fagsaktypenavnPåSpråk))
            .medAvsnittstype(Avsnitt.Avsnittstype.OPPSUMMERING)
            .medUnderavsnittsliste(List.of(underavsnitt))
            .build();
    }

    public static Avsnitt lagEkstrainformasjonsavsnitt(int antallUkerKlagefrist, String kontakttelefonnummer) {
        Underavsnitt hvordanBetaleTilbakeBeløpAvsnitt = lagHvordanBetaleBeløpAvsnitt();
        Underavsnitt angåendeKlageAvsnitt = lagAngåendeKlageAvsnitt(antallUkerKlagefrist);
        Underavsnitt rettTilInnsynAvsnitt = lagRettTilInnsynAvsnitt();
        Underavsnitt harDuSpørsmålAvsnitt = lagHarDuSpørsmålAvsnitt(kontakttelefonnummer);
        Underavsnitt hilsenNavAvsnitt = lagHilsenNavAvsnitt();

        return new Avsnitt.Builder()
            .medOverskrift("Hvordan betale tilbake pengene du skylder")
            .medAvsnittstype(Avsnitt.Avsnittstype.TILLEGGSINFORMASJON)
            .medUnderavsnittsliste(List.of(
                hvordanBetaleTilbakeBeløpAvsnitt,
                angåendeKlageAvsnitt,
                rettTilInnsynAvsnitt,
                harDuSpørsmålAvsnitt,
                hilsenNavAvsnitt))
            .build();
    }

    private static Underavsnitt lagHvordanBetaleBeløpAvsnitt() {
        return new Underavsnitt.Builder()
            .medBrødtekst("Vi har overført beløpet du skylder til NAV Innkreving, som vil sende deg " +
                "et krav om å betale tilbake det feilutbetalte beløpet. Har du spørsmål om dette, kan du kontakte NAV Innkreving. ")
            .build();
    }

    private static Underavsnitt lagAngåendeKlageAvsnitt(int antallUkerKlagefrist) {
        return new Underavsnitt.Builder()
            .medOverskrift("Du har rett til å klage")
            .medBrødtekst(
                String.format(lagBrødTekst(), antallUkerKlagefrist))
            .build();
    }

    private static String lagBrødTekst() {
        StringBuilder builder = new StringBuilder();
        builder.append("Du kan klage innen %d uker fra den datoen du mottok vedtaket. ");
        builder.append("Du finner skjema og informasjon på nav.no/klage. ");
        builder.append("\n");
        builder.append("Du må betale tilbake, selv om du klager på dette vedtaket. Vi vil betale ");
        builder.append("tilbake pengene du har betalt inn, om du får vedtak om at du ikke trengte å betale tilbake hele ");
        builder.append("eller deler av beløpet du skyldte.");

        return builder.toString();
    }

    private static Underavsnitt lagRettTilInnsynAvsnitt() {
        return new Underavsnitt.Builder()
            .medOverskrift("Du har rett til innsyn")
            .medBrødtekst("På nav.no/dittnav kan du se dokumentene i saken din.")
            .build();
    }

    private static Underavsnitt lagHilsenNavAvsnitt() {
        return new Underavsnitt.Builder()
            .medBrødtekst("Med vennlig hilsen \nNAV Familie- og pensjonsytelser")
            .build();
    }

    private static Underavsnitt lagHarDuSpørsmålAvsnitt(String kontakttelefonnummer) {
        return new Underavsnitt.Builder()
            .medOverskrift("Har du spørsmål?")
            .medBrødtekst(String.format("Du finner nyttig informasjon på nav.no/familie. Du kan også kontakte oss på telefon %s",
                kontakttelefonnummer))
            .build();
    }

    public static Avsnitt lagPeriodeAvsnitt(PeriodeMedBrevtekst periodeMedTekst) {
        String overskrift = String.format("Perioden fra og med %s til og med %s",
            BrevUtil.konverterFraLocaldateTilTekst(periodeMedTekst.getFom()),
            BrevUtil.konverterFraLocaldateTilTekst(periodeMedTekst.getTom()));

        Underavsnitt underavsnittOmFakta = new Underavsnitt.Builder()
            .medBrødtekst(periodeMedTekst.getGenerertFaktaAvsnitt())
            .medFritekst(periodeMedTekst.getFritekstFakta())
            .medErFritekstTillatt(true)
            .medUnderavsnittstype(Underavsnitt.Underavsnittstype.FAKTA)
            .build();

        Underavsnitt underavsnittOmVilkår = new Underavsnitt.Builder()
            .medBrødtekst(periodeMedTekst.getGenerertVilkårAvsnitt())
            .medErFritekstTillatt(true)
            .medFritekst(periodeMedTekst.getFritekstVilkår())
            .medUnderavsnittstype(Underavsnitt.Underavsnittstype.VILKÅR)
            .build();


        Underavsnitt underavsnittOmSærligeGrunner = new Underavsnitt.Builder()
            .medBrødtekst(periodeMedTekst.getGenerertSærligeGrunnerAvsnitt())
            .medErFritekstTillatt(true)
            .medFritekst(periodeMedTekst.getFritekstSærligeGrunner())
            .medUnderavsnittstype(Underavsnitt.Underavsnittstype.SÆRLIGEGRUNNER)
            .build();

        return new Avsnitt.Builder()
            .medOverskrift(overskrift)
            .medFom(periodeMedTekst.getFom())
            .medTom(periodeMedTekst.getTom())
            .medAvsnittstype(Avsnitt.Avsnittstype.PERIODE)
            .medUnderavsnittsliste(List.of(underavsnittOmFakta, underavsnittOmVilkår, underavsnittOmSærligeGrunner))
            .build();
    }
}
