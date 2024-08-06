package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import no.nav.abakus.vedtak.ytelse.Aktør;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;


/**
 * Input request for å bytte en utgått aktørid med en aktiv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class ByttAktørRequest {

    @JsonProperty(value = "utgatt", required = true)
    @NotNull
    @Valid
    private AktørId utgåttAktør;

    @JsonProperty(value = "gyldig", required = true)
    @NotNull
    @Valid
    private AktørId gyldigAktør;

    public ByttAktørRequest() {
        // Jackson
    }

    public AktørId getUtgåttAktør() {
        return utgåttAktør;
    }

    public AktørId getGyldigAktør() {
        return gyldigAktør;
    }
}
