package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene;

import java.time.LocalDate;

public class PeriodeMedBrevtekst {

    private LocalDate fom;
    private LocalDate tom;

    private String generertFaktaAvsnitt;
    private String generertVilkårAvsnitt;
    private String generertSærligeGrunnerAvsnitt;

    private String fritekstFakta;
    private String fritekstVilkår;
    private String fritekstSærligeGrunner;

    public PeriodeMedBrevtekst() {
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public String getGenerertFaktaAvsnitt() {
        return generertFaktaAvsnitt;
    }

    public String getGenerertVilkårAvsnitt() {
        return generertVilkårAvsnitt;
    }

    public String getGenerertSærligeGrunnerAvsnitt() {
        return generertSærligeGrunnerAvsnitt;
    }

    public String getFritekstFakta() {
        return fritekstFakta;
    }

    public String getFritekstVilkår() {
        return fritekstVilkår;
    }

    public String getFritekstSærligeGrunner() {
        return fritekstSærligeGrunner;
    }



    public static class Builder {
        PeriodeMedBrevtekst periode = new PeriodeMedBrevtekst();

        public Builder medFom(LocalDate fom) {
            this.periode.fom = fom;
            return this;
        }

        public Builder medTom(LocalDate tom) {
            this.periode.tom = tom;
            return this;
        }

        public Builder medGenerertFaktaAvsnitt(String generertFaktaAvsnitt) {
            this.periode.generertFaktaAvsnitt = generertFaktaAvsnitt;
            return this;
        }

        public Builder medGenerertVilkårAvsnitt(String generertVilkårAvsnitt) {
            this.periode.generertVilkårAvsnitt = generertVilkårAvsnitt;
            return this;
        }

        public Builder medGenerertSærligeGrunnerAvsnitt(String generertSærligeGrunnerAvsnitt) {
            this.periode.generertSærligeGrunnerAvsnitt = generertSærligeGrunnerAvsnitt;
            return this;
        }

        public Builder medFritekstFakta(String fritekstFakta) {
            this.periode.fritekstFakta = fritekstFakta;
            return this;
        }

        public Builder medFritekstVilkår(String fritekstVilkår) {
            this.periode.fritekstVilkår = fritekstVilkår;
            return this;
        }

        public Builder medFritekstSærligeGrunner(String fritekstSærligeGrunner) {
            this.periode.fritekstSærligeGrunner = fritekstSærligeGrunner;
            return this;
        }

        public PeriodeMedBrevtekst build() {
            return periode;
        }
    }
}
