package no.nav.journalpostapi.dto.dokument;

import no.nav.journalpostapi.Kode;

/**
 * https://confluence.adeo.no/display/BOA/Variantformat
 */
public enum Variantformat implements Kode {

    /**
     * Den "offisielle" versjonen av et dokument, som er beregnet på visning og langtidsbevaring. I de fleste tilfeller er arkivvarianten lik dokumentet brukeren sendte inn eller mottok (digitalt eller på papir). Arkivvarianten er alltid i menneskelesbart format, som PDF, PDF/A eller PNG.
     * Alle dokumenter har en arkivvariant, med mindre bruker har fått innvilget vedtak om sletting eller skjerming av opplysninger i arkivet.
     */
    Arkiv("ARKIV"),

    /**
     * Dette er en sladdet versjon av arkivvarianten. Dersom det finnes en SLADDET variant, vil de fleste NAV-ansatte kun ha tilgang til denne varianten og ikke arkivvariant. Enkelte saksbehandlere vil imidlertid ha tilgang til både SLADDET og ARKIV.
     */
    Sladdet("SLADDET"),

    /**
     * Variant av dokument som inneholder spørsmålstekster, hjelpetekster og ubesvarte spørsmål fra søknadsdialogen. Fullversjon genereres for enkelte søknadsskjema fra nav.no, og brukes ved klagebehandling.
     */
    Fullversjon("FULLVERSJON"),

    /**
     * Variant av dokumentet i strukturert format, f.eks. XML eller JSON.
     * <p>
     * Originalvarianten er beregnet på maskinell lesning og behandling.
     */
    Original("ORIGINAL"),

    /**
     * Fil som er produsert under skanning av papirdokumentasjon, og inneholder metadata om forsendelsen og selve skanningen.
     */
    Skanningsdata("SKANNING_META"),
    Brevbestillingsdata("BREVBESTILLING"),

    /**
     * Produksjonsvariant ,i eget proprietært format. Varianten finnes for dokumenter som er produsert i Metaforce eller Brevklient.
     */
    Produksjon("PRODUKSJON"),

    /**
     * Produksjonsvariant i eget proprietært format. Varianten finnes kun for dokumenter som er produsert i Exstream Live Editor.
     */
    Produksjon_DLF("PRODUKSJON_DLF");

    private String kode;

    Variantformat(String kode) {
        this.kode = kode;
    }

    @Override
    public String getKode() {
        return kode;
    }
}
