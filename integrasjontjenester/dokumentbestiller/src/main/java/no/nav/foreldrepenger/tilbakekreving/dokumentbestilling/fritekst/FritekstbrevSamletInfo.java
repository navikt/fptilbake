package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekst;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.BrevMetadata;

public class FritekstbrevSamletInfo {

    private BrevMetadata brevMetadata;
    private String overskrift;
    private String fritekstFraSaksbehandler;

    public BrevMetadata getBrevMetadata() {
        return brevMetadata;
    }

    public void setBrevMetadata(BrevMetadata brevMetadata) {
        this.brevMetadata = brevMetadata;
    }

    public String getOverskrift() {
        return overskrift;
    }

    public void setOverskrift(String overskrift) {
        this.overskrift = overskrift;
    }

    public String getFritekstFraSaksbehandler() {
        return fritekstFraSaksbehandler;
    }

    public void setFritekstFraSaksbehandler(String fritekstFraSaksbehandler) {
        this.fritekstFraSaksbehandler = fritekstFraSaksbehandler;
    }
}
