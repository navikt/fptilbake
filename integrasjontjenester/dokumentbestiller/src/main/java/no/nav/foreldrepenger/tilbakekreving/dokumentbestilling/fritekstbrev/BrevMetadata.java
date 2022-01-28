package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;

public class BrevMetadata {

    private String sakspartId;
    private String sakspartNavn;
    private boolean finnesVerge;
    private String vergeNavn;
    private Adresseinfo mottakerAdresse;

    private String behandlendeEnhetId;
    private String behandlendeEnhetNavn;

    private String ansvarligSaksbehandler;

    private String saksnummer;
    private Språkkode språkkode;
    private FagsakYtelseType fagsaktype;
    private String fagsaktypenavnPåSpråk;
    private BehandlingType behandlingType;

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

    public boolean isFinnesVerge() {
        return finnesVerge;
    }

    public String getVergeNavn() {
        return vergeNavn;
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

    public FagsakYtelseType getFagsaktype() {
        return fagsaktype;
    }

    public String getFagsaktypenavnPåSpråk() {
        return fagsaktypenavnPåSpråk;
    }

    public BehandlingType getBehandlingType() {
        return behandlingType;
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

        public Builder medFinnesVerge(boolean finnesVerge) {
            this.metadata.finnesVerge = finnesVerge;
            return this;
        }

        public Builder medVergeNavn(String vergeNavn) {
            this.metadata.vergeNavn = vergeNavn;
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

        public Builder medFagsaktype(FagsakYtelseType fagsaktype) {
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

        public Builder medBehandlingtype(BehandlingType behandlingType) {
            this.metadata.behandlingType = behandlingType;
            return this;
        }

        public BrevMetadata build() {
            return metadata;
        }

    }

}
