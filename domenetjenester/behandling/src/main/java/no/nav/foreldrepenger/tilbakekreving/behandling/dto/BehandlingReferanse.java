package no.nav.foreldrepenger.tilbakekreving.behandling.dto;


import java.util.Objects;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

/**
 * Referanse til en behandling.
 * Enten {@link #id} eller {@link #behandlingUuid} vil være satt.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY, fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(Include.NON_NULL)
public class BehandlingReferanse implements AbacDto {

    private static final String NUM_REGEXP = "\\d{1,19}"; // Long

    public static final String NAME = "behandlingId";

    /**
     * Behandling Id - legacy Long, ny UUID.
     */
    @JsonProperty(value = NAME, required = true)
    @NotNull
    @Size(min = 1, max = 100)
    @Pattern(regexp = "^(" + NUM_REGEXP + ")|(" + IsUUID.UUID_REGEXP
        + ")$", message = "Behandling Id ${validatedValue} matcher ikke tillatt pattern '{regexp}'")
    private String id;

    public BehandlingReferanse(Integer id) {
        this.id = Objects.requireNonNull(id, "id").toString();
    }

    public BehandlingReferanse(Long id) {
        this.id = Objects.requireNonNull(id, "id").toString();
    }

    public BehandlingReferanse(String id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    public BehandlingReferanse(UUID id) {
        this.id = Objects.requireNonNull(id, "id").toString();
    }

    protected BehandlingReferanse() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var other = (BehandlingReferanse) obj;
        return Objects.equals(this.id, other.id);
    }

    /**
     * Denne er kun intern nøkkel, bør ikke eksponeres ut men foreløpig støttes både Long id og UUID id for behandling på grensesnittene.
     */
    public Long getBehandlingId() {
        return isLong() ? Long.parseLong(id) : null;
    }

    public UUID getBehandlingUuid() {
        return !isLong() ? UUID.fromString(id) : null;
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @JsonSetter(NAME)
    public void setBehandlingId(String behandlingId) {
        this.id = Objects.requireNonNull(behandlingId, NAME);
        validerLongEllerUuid();
    }

    @Override
    public String toString() {
        return id;
    }

    private boolean isLong() {
        return id.matches("^\\d+$");
    }

    private void validerLongEllerUuid() {
        // valider
        if (isLong()) {
            getBehandlingId();
        } else {
            getBehandlingUuid();
        }
    }

    @JsonIgnore
    public boolean erInternBehandlingId() {
        return isLong();
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        if (isLong()) {
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_ID, getBehandlingId());
        } else {
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_UUID, getBehandlingUuid());
        }
    }
}
