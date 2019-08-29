package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto;

import java.util.List;

public class ForhåndvisningVedtaksbrevTekstDto {

    private List<Avsnitt> avsnittsliste;

    public List<Avsnitt> getAvsnittsliste() {
        return avsnittsliste;
    }

    public ForhåndvisningVedtaksbrevTekstDto(List<Avsnitt> avsnittsliste) {
        this.avsnittsliste = avsnittsliste;
    }
}
