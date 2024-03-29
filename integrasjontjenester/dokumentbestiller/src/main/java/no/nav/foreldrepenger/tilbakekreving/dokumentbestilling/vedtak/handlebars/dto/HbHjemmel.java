package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HbHjemmel {

    @JsonProperty("lovhjemmel-vedtak")
    private String lovhjemmelVedtak;
    @JsonProperty("lovhjemmel-flertall")
    private boolean lovhjemmelFlertall;

    private HbHjemmel() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private HbHjemmel kladd = new HbHjemmel();

        public HbHjemmel.Builder medLovhjemmelVedtak(String lovhjemmelVedtak) {
            return medLovhjemmelVedtak(lovhjemmelVedtak, " og ");
        }

        public HbHjemmel.Builder medLovhjemmelVedtak(String lovhjemmelVedtak, String skilletegnMellomHjemler) {
            kladd.lovhjemmelVedtak = lovhjemmelVedtak;
            kladd.lovhjemmelFlertall = lovhjemmelVedtak.contains(skilletegnMellomHjemler);
            return this;
        }

        public HbHjemmel build() {
            Objects.requireNonNull(kladd.lovhjemmelVedtak, "lovhjemmelVedtak er ikke satt");
            return kladd;
        }
    }
}
