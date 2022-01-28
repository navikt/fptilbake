package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.BigDecimalHeltallSerialiserer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.LocalDateTilLangtNorskFormatSerialiserer;

public class HbVarsel {

    @JsonProperty("varslet-dato")
    @JsonSerialize(using = LocalDateTilLangtNorskFormatSerialiserer.class)
    private LocalDate varsletDato;
    @JsonProperty("varslet-beløp")
    @JsonSerialize(using = BigDecimalHeltallSerialiserer.class)
    private BigDecimal varsletBeløp;

    private HbVarsel() {
    }

    public static HbVarsel forDatoOgBeløp(LocalDate varsletDato, Long varsletBeløp) {
        if (varsletDato == null && varsletBeløp == null) {
            return null;
        }
        return HbVarsel.builder()
                .medVarsletDato(varsletDato)
                .medVarsletBeløp(varsletBeløp)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private HbVarsel kladd = new HbVarsel();

        public HbVarsel.Builder medVarsletDato(LocalDate varsletDato) {
            kladd.varsletDato = varsletDato;
            return this;
        }

        public HbVarsel.Builder medVarsletBeløp(BigDecimal varsletBeløp) {
            kladd.varsletBeløp = varsletBeløp;
            return this;
        }

        public HbVarsel.Builder medVarsletBeløp(Long varsletBeløp) {
            kladd.varsletBeløp = varsletBeløp != null ? BigDecimal.valueOf(varsletBeløp) : null;
            return this;
        }

        public HbVarsel build() {
            if (kladd.varsletDato == null && kladd.varsletBeløp != null) {
                throw new IllegalArgumentException("Inkonsistent tilstand: varslet beløp finnes, men varslet dato finnes ikke");
            }
            if (kladd.varsletDato != null && kladd.varsletBeløp == null) {
                throw new IllegalArgumentException("Inkonsistent tilstand: varslet dato finnes, men varslet beløp finnes ikke");
            }
            return kladd;
        }
    }
}
