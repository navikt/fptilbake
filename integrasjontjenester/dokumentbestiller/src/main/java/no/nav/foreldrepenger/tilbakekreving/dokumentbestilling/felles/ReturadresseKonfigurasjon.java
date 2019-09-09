package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles;

public class ReturadresseKonfigurasjon {

    private static final String BREV_RETURADRESSE_ENHET_NAVN = "NAV Familie- og pensjonsytelser";
    private static final String BREV_RETURADRESSE_ADRESSELINJE_1 = "Postboks 6600 Etterstad";
    private static final String BREV_RETURADRESSE_POSTNUMMER = "0607";
    private static final String BREV_RETURADRESSE_POSTSTED = "OSLO";
    private static final String BREV_RETURADRESSE_KLAGE_ENHET = "NAV Klageinstans";
    private static final String BREV_TELEFONNUMMER_KLAGE_ENHET = "55 55 33 33";

    private ReturadresseKonfigurasjon() {
        // gjem implisitt konstruktør
    }

    public static String getBrevTelefonnummerKlageEnhet() {
        return BREV_TELEFONNUMMER_KLAGE_ENHET;
    }

    public static String getBrevReturadresseEnhetNavn() {
        return BREV_RETURADRESSE_ENHET_NAVN;
    }

    public static String getBrevReturadresseAdresselinje1() {
        return BREV_RETURADRESSE_ADRESSELINJE_1;
    }

    public static String getBrevReturadressePostnummer() {
        return BREV_RETURADRESSE_POSTNUMMER;
    }

    public static String getBrevReturadressePoststed() {
        return BREV_RETURADRESSE_POSTSTED;
    }

    public static String getBrevReturadresseKlageEnhet() {
        return BREV_RETURADRESSE_KLAGE_ENHET;
    }
}
