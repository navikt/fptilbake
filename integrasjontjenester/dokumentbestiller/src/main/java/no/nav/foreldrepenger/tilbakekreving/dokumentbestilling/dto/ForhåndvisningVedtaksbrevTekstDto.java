package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto;

import java.util.List;

public class ForhåndvisningVedtaksbrevTekstDto {

    private List<Avsnitt> avsnittsliste;
    private String oppsummeringFritekst;

    public ForhåndvisningVedtaksbrevTekstDto(List<Avsnitt> avsnittsliste, String oppsummeringFritekst) {
        this.avsnittsliste = avsnittsliste;
        this.oppsummeringFritekst = oppsummeringFritekst;
    }

    public List<Avsnitt> getAvsnittsliste() {
        return avsnittsliste;
    }

    public String getOppsummeringFritekst() {
        return oppsummeringFritekst;
    }

}
