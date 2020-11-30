package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto;

public class Underavsnitt {

    private String overskrift;
    private String brødtekst;
    private String fritekst;
    private boolean fritekstTillatt;
    private boolean fritekstPåkrevet;
    private Underavsnittstype underavsnittstype;

    public enum Underavsnittstype {
        FAKTA,
        FORELDELSE,
        VILKÅR,
        SÆRLIGEGRUNNER,
        SÆRLIGEGRUNNER_ANNET
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

    public boolean isFritekstPåkrevet() {
        return fritekstPåkrevet;
    }

    public Underavsnittstype getUnderavsnittstype() {
        return underavsnittstype;
    }

    public static class Builder {
        private Underavsnitt underavsnitt = new Underavsnitt();

        public Builder medOverskrift(String overskrift) {
            this.underavsnitt.overskrift = overskrift;
            return this;
        }

        public Builder medBrødtekst(String brødtekst) {
            this.underavsnitt.brødtekst = brødtekst;
            return this;
        }

        public Builder medFritekst(String fritekst) {
            this.underavsnitt.fritekst = fritekst;
            return this;
        }

        public Builder medErFritekstTillatt(boolean fritekstTillatt) {
            this.underavsnitt.fritekstTillatt = fritekstTillatt;
            return this;
        }

        public Builder medErFritekstPåkrevet(boolean fritekstPåkrevet) {
            this.underavsnitt.fritekstPåkrevet = fritekstPåkrevet;
            return this;
        }

        public Builder medUnderavsnittstype(Underavsnittstype underavsnittstype) {
            this.underavsnitt.underavsnittstype = underavsnittstype;
            return this;
        }

        public Underavsnitt build() {
            if (!underavsnitt.fritekstTillatt && underavsnitt.fritekstPåkrevet) {
                throw new IllegalArgumentException("Det gir ikke mening at fritekst er påkrevet når fritekst ikke er tillatt");
            }
            return underavsnitt;
        }
    }
}
