package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9;

import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public class K9PipDto {

    @JsonProperty(value = "aktørIder", required = true)
    @Size(max = 50)
    @Valid
    private Set<K9AktørId> aktørIder;

    /**
     * @deprecated Ikke helt godt kodeverk - her er tittel i stedet.
     */
    @Deprecated(forRemoval = true)
    @JsonProperty(value = "behandlingStatus")
    @Size(max = 50)
    @Pattern(regexp = "^[\\p{Alnum}\\p{Space}]+$", message = "[${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
    private String behandlingStatus;

    /**
     * @deprecated Ikke helt godt kodeverk - her er tittel i stedet.
     */
    @Deprecated(forRemoval = true)
    @JsonProperty(value = "fagsakStatus")
    @Size(max = 50)
    @Pattern(regexp = "^[\\p{Alnum}\\p{Space}]+$", message = "[${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
    private String fagsakStatus;

    public Set<K9AktørId> getAktørIder() {
        return aktørIder;
    }

    public String getBehandlingStatus() {
        return behandlingStatus;
    }

    public String getFagsakStatus() {
        return fagsakStatus;
    }

    public void setAktørIder(Set<K9AktørId> aktørIder) {
        this.aktørIder = aktørIder;
    }

    public void setBehandlingStatus(String behandlingStatus) {
        this.behandlingStatus = behandlingStatus;
    }

    public void setFagsakStatus(String fagsakStatus) {
        this.fagsakStatus = fagsakStatus;
    }
}
