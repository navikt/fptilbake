package no.nav.foreldrepenger.tilbakekreving.kontrakter.aktørbytte;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


/**
 * Input request for å bytte en utgått aktørid med en aktiv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class ByttAktørRequest {

    @JsonProperty(value = "utgatt", required = true)
    @NotNull
    @Size(max = 20)
    @Pattern(regexp = "^\\d+$", message = "AktørId [${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
    private String utgåttAktør;

    @JsonProperty(value = "gyldig", required = true)
    @NotNull
    @Size(max = 20)
    @Pattern(regexp = "^\\d+$", message = "AktørId [${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
    private String gyldigAktør;

    public ByttAktørRequest() {
        // Jackson
    }

    public ByttAktørRequest(String utgåttAktør, String gyldigAktør) {
        this.utgåttAktør = utgåttAktør;
        this.gyldigAktør = gyldigAktør;
    }

    public String getUtgåttAktør() {
        return utgåttAktør;
    }

    public String getGyldigAktør() {
        return gyldigAktør;
    }
}
