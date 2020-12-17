package no.nav.foreldrepenger.tilbakekreving.organisasjon;

import java.util.Objects;


public class Virksomhet {

    private String orgnr;
    private String navn;

    public Virksomhet() {
        // internal forbruk
    }


    public String getOrgnr() {
        return orgnr;
    }

    public String getNavn() {
        return navn;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Virksomhet)) {
            return false;
        }
        Virksomhet other = (Virksomhet) obj;
        return Objects.equals(this.getOrgnr(), other.getOrgnr());
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgnr);
    }

    @Override
    public String toString() {
        return "Virksomhet{" +
                "navn=" + navn +
                '}';
    }

    public static class Builder {
        private Virksomhet mal;

        /**
         * For oppretting av
         */
        public Builder() {
            this.mal = new Virksomhet();
        }

        public Builder medOrgnr(String orgnr) {

            this.mal.orgnr = orgnr;
            return this;
        }

        public Builder medNavn(String navn) {
            this.mal.navn = navn;
            return this;
        }

        public Virksomhet build() {
            return mal;
        }
    }
}
