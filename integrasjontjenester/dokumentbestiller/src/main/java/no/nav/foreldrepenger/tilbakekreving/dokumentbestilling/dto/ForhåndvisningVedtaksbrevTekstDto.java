package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto;

import java.util.List;

public class ForhåndvisningVedtaksbrevTekstDto {

    private List<Avsnitt> avsnittsliste;

    public List<Avsnitt> getAvsnittsliste() {
        return avsnittsliste;
    }

    public static class Builder {
        private ForhåndvisningVedtaksbrevTekstDto dto = new ForhåndvisningVedtaksbrevTekstDto();

        public Builder medAvsnittsliste(List<Avsnitt> avsnittsliste) {
            this.dto.avsnittsliste = avsnittsliste;
            return this;
        }

        public ForhåndvisningVedtaksbrevTekstDto build() {
            return dto;
        }
    }
}
