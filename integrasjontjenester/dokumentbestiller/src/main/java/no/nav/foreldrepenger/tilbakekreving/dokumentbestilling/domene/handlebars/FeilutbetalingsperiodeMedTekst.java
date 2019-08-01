package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.domene.handlebars;

import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class FeilutbetalingsperiodeMedTekst {
    private Periode periode;

    private String generertFaktaAvsnitt;
    private String generertVilkaarAvsnitt;
    private String generertSaerligeGrunnerAvsnitt;

    private String fritekstFaktaAvsnitt;
    private String fritekstVilkaarAvsnitt;
    private String fritekstSaerligeGrunnerAvsnitt;

    public Periode getPeriode() {
        return periode;
    }

    public void setPeriode(Periode periode) {
        this.periode = periode;
    }

    public String getFritekstFaktaAvsnitt() {
        return fritekstFaktaAvsnitt;
    }

    public void setFritekstFaktaAvsnitt(String fritekstFaktaAvsnitt) {
        this.fritekstFaktaAvsnitt = fritekstFaktaAvsnitt;
    }

    public String getFritekstVilkaarAvsnitt() {
        return fritekstVilkaarAvsnitt;
    }

    public void setFritekstVilkaarAvsnitt(String fritekstVilkaarAvsnitt) {
        this.fritekstVilkaarAvsnitt = fritekstVilkaarAvsnitt;
    }

    public String getFritekstSaerligeGrunnerAvsnitt() {
        return fritekstSaerligeGrunnerAvsnitt;
    }

    public void setFritekstSaerligeGrunnerAvsnitt(String fritekstSaerligeGrunnerAvsnitt) {
        this.fritekstSaerligeGrunnerAvsnitt = fritekstSaerligeGrunnerAvsnitt;
    }

    public String getGenerertFaktaAvsnitt() {
        return generertFaktaAvsnitt;
    }

    public void setGenerertFaktaAvsnitt(String generertFaktaAvsnitt) {
        this.generertFaktaAvsnitt = generertFaktaAvsnitt;
    }

    public String getGenerertVilkaarAvsnitt() {
        return generertVilkaarAvsnitt;
    }

    public void setGenerertVilkaarAvsnitt(String generertVilkaarAvsnitt) {
        this.generertVilkaarAvsnitt = generertVilkaarAvsnitt;
    }

    public String getGenerertSaerligeGrunnerAvsnitt() {
        return generertSaerligeGrunnerAvsnitt;
    }

    public void setGenerertSaerligeGrunnerAvsnitt(String generertSaerligeGrunnerAvsnitt) {
        this.generertSaerligeGrunnerAvsnitt = generertSaerligeGrunnerAvsnitt;
    }
}
