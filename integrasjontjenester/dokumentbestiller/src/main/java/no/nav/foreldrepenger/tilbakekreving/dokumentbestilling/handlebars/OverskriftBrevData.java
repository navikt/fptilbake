package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars;

public class OverskriftBrevData {

    private String fagsakType;
    private boolean engangsstønad = false;

    public String getFagsakType() {
        return fagsakType;
    }

    public void setFagsakType(String fagsakType) {
        this.fagsakType = fagsakType;
    }

    public boolean isEngangsstønad() {
        return engangsstønad;
    }

    public void setEngangsstønad(boolean engangsstønad) {
        this.engangsstønad = engangsstønad;
    }
}
