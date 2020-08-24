package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.LocalDateTilLangtNorskFormatSerialiserer;

public class HbBehandling {

    @JsonProperty("er-revurdering")
    private boolean erRevurdering = false;
    @JsonProperty("original-behandling-dato-fagsakvedtak")
    @JsonSerialize(using = LocalDateTilLangtNorskFormatSerialiserer.class)
    private LocalDate originalBehandlingDatoFagsakvedtak;

    private HbBehandling() {
    }

    public boolean erRevurdering() {
        return erRevurdering;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private HbBehandling kladd = new HbBehandling();

        public HbBehandling.Builder medErRevurdering(boolean erRevurdering) {
            kladd.erRevurdering = erRevurdering;
            return this;
        }

        public HbBehandling.Builder medOriginalBehandlingDatoFagsakvedtak(LocalDate datoFagsakvedtak) {
            kladd.originalBehandlingDatoFagsakvedtak = datoFagsakvedtak;
            return this;
        }

        public HbBehandling build() {
            if (kladd.erRevurdering) {
                Objects.requireNonNull(kladd.originalBehandlingDatoFagsakvedtak, "vedtaksdato for original behandling er ikke satt");
            }
            return kladd;
        }
    }
}
