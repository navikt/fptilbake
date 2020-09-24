package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.header;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;

public class Adresse {
    private String mottakerNavn;
    private String adresselinje1;
    private String adresselinje2;
    private String adresselinje3;
    private String adresselinje4;
    private String postNr;
    private String poststed;
    private String land;

    public Adresse(Adresseinfo adresseinfo) {
        this.mottakerNavn = adresseinfo.getMottakerNavn();
        this.adresselinje1 = adresseinfo.getAdresselinje1();
        this.adresselinje2 = adresseinfo.getAdresselinje2();
        this.adresselinje3 = adresseinfo.getAdresselinje3();
        this.adresselinje4 = adresseinfo.getAdresselinje4();
        this.postNr = adresseinfo.getPostNr();
        this.poststed = adresseinfo.getPoststed();
        this.land = adresseinfo.getLand();
    }

}
