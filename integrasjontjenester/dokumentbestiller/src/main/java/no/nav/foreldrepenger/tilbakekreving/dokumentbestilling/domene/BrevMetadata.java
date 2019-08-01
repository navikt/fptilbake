package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.KodeDto;

public class BrevMetadata {

    private String sakspartId;
    private String sakspartNavn;
    private Adresseinfo mottakerAdresse;

    private String behandlendeEnhetId;
    private String behandlendeEnhetNavn;

    private String ansvarligSaksbehandler;

    private String saksnummer;
    private Språkkode språkkode;
    private KodeDto fagsaktype;
    private String fagsaktypenavnPåSpråk;

    private String tittel;

    public String getTittel() {
        return tittel;
    }

    public String getSakspartId() {
        return sakspartId;
    }

    public String getSakspartNavn() {
        return sakspartNavn;
    }

    public Adresseinfo getMottakerAdresse() {
        return mottakerAdresse;
    }

    public String getBehandlendeEnhetId() {
        return behandlendeEnhetId;
    }

    public String getBehandlendeEnhetNavn() {
        return behandlendeEnhetNavn;
    }

    public String getAnsvarligSaksbehandler() {
        return ansvarligSaksbehandler;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public Språkkode getSpråkkode() {
        return språkkode;
    }

    public KodeDto getFagsaktype() {
        return fagsaktype;
    }

    public String getFagsaktypenavnPåSpråk() {
        return fagsaktypenavnPåSpråk;
    }

    public static class Builder {
        private BrevMetadata metadata = new BrevMetadata();

        public Builder medSakspartId(String sakspartId) {
            this.metadata.sakspartId = sakspartId;
            return this;
        }

        public Builder medSakspartNavn(String sakspartNavn) {
            this.metadata.sakspartNavn = sakspartNavn;
            return this;
        }

        public Builder medBehandlendeEnhetId(String behandlendeEnhetId) {
            this.metadata.behandlendeEnhetId = behandlendeEnhetId;
            return this;
        }

        public Builder medBehandlendeEnhetNavn(String behandlendeEnhetNavn) {
            this.metadata.behandlendeEnhetNavn = behandlendeEnhetNavn;
            return this;
        }

        public Builder medAnsvarligSaksbehandler(String ansvarligSaksbehandler) {
            this.metadata.ansvarligSaksbehandler = ansvarligSaksbehandler;
            return this;
        }

        public Builder medFagsaktypenavnPåSpråk(String fagsaktypenavnPåSpråk) {
            this.metadata.fagsaktypenavnPåSpråk = fagsaktypenavnPåSpråk;
            return this;
        }

        public Builder medTittel(String tittel) {
            this.metadata.tittel = tittel;
            return this;
        }

        public Builder medSaksnummer(String saksnummer) {
            this.metadata.saksnummer = saksnummer;
            return this;
        }
        public Builder medFagsaktype(KodeDto fagsaktype) {
            this.metadata.fagsaktype = fagsaktype;
            return this;
        }

        public Builder medSprakkode(Språkkode sprakkode) {
            this.metadata.språkkode = sprakkode;
            return this;
        }

        public Builder medMottakerAdresse(Adresseinfo mottakerAdresse) {
            this.metadata.mottakerAdresse = mottakerAdresse;
            return this;
        }

        public BrevMetadata build() {
            return metadata;
        }

    }

}
