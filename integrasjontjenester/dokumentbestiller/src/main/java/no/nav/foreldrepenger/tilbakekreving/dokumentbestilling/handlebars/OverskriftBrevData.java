package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.Lokale;

public class OverskriftBrevData {

    private String fagsakType;
    private Lokale lokale;
    private boolean engangsstønad = false;

    public String getFagsakType() {
        return fagsakType;
    }

    public void setFagsakType(String fagsakType) {
        this.fagsakType = fagsakType;
    }

    public String getLokale() {
        return lokale.getTekst();
    }

    public void setLokale(Lokale lokale) {
        this.lokale = lokale;
    }

    public boolean isEngangsstønad() {
        return engangsstønad;
    }

    public void setEngangsstønad(boolean engangsstønad) {
        this.engangsstønad = engangsstønad;
    }
}
