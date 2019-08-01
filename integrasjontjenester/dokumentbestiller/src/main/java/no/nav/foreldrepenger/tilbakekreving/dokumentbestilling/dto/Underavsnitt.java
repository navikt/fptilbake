package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto;

public class Underavsnitt {

    private String overskrift;
    private String brødtekst;
    private String fritekst;
    private boolean fritekstTillatt;
    private Underavsnittstype underavsnittstype;

    public enum Underavsnittstype {
        FAKTA,
        VILKÅR,
        SÆRLIGEGRUNNER
    }

    public String getOverskrift() {
        return overskrift;
    }

    public String getBrødtekst() {
        return brødtekst;
    }

    public String getFritekst() {
        return fritekst;
    }

    public boolean isFritekstTillatt() {
        return fritekstTillatt;
    }

    public Underavsnittstype getUnderavsnittstype() {
        return underavsnittstype;
    }

    public static class Builder {
        private Underavsnitt underavsnitt = new Underavsnitt();

        public Builder medOverskrift(String overskrift) {
            this.underavsnitt.overskrift= overskrift;
            return this;
        }

        public Builder medBrødtekst(String brødtekst) {
            this.underavsnitt.brødtekst= brødtekst;
            return this;
        }

        public Builder medFritekst(String fritekst) {
            this.underavsnitt.fritekst= fritekst;
            return this;
        }

        public Builder medErFritekstTillatt(boolean fritekstTillatt) {
            this.underavsnitt.fritekstTillatt= fritekstTillatt;
            return this;
        }

        public Builder medUnderavsnittstype(Underavsnittstype underavsnittstype) {
            this.underavsnitt.underavsnittstype= underavsnittstype;
            return this;
        }

        public Underavsnitt build() {
            return underavsnitt;
        }
    }
}
