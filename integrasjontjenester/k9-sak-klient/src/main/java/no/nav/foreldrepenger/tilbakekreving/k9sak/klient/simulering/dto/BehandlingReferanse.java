package no.nav.foreldrepenger.tilbakekreving.k9sak.klient.simulering.dto;

import java.util.Objects;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Referanse for behandling - enten Long (legacy) eller UUID format.
 */
@JsonFormat(shape = Shape.STRING)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class BehandlingReferanse {

    private static final String NUM_REGEXP = "\\d{1,19}"; // Long
    private static final java.util.regex.Pattern UUID_REGEXP_PATTERN = java.util.regex.Pattern.compile(IsUUID.UUID_REGEXP);

    /**
     * Behandling Id - legacy Long, ny UUID.
     */
    @JsonValue
    @NotNull
    @Size(min = 1, max = 100)
    @Pattern(regexp = "^(" + NUM_REGEXP + ")|(" + IsUUID.UUID_REGEXP
        + ")$", message = "Behandling Id ${validatedValue} matcher ikke tillatt pattern '{regexp}'")
    private String id;

    @JsonIgnore
    private Boolean isUuid;

    public BehandlingReferanse(Long behandlingId) {
        this.id = Objects.requireNonNull(behandlingId, "behandlingId").toString();
        this.isUuid = false;
    }

    public BehandlingReferanse(UUID behandlingId) {
        this.id = Objects.requireNonNull(behandlingId, "behandlingId").toString();
        this.isUuid = true;
    }

    @JsonCreator
    public BehandlingReferanse(@Valid @NotNull @Pattern(regexp = "^(" + NUM_REGEXP + ")|("
        + IsUUID.UUID_REGEXP + ")$", message = "Behandling Id ${validatedValue} matcher ikke tillatt pattern") String behandlingId) {
        this.id = behandlingId;
        this.isUuid = UUID_REGEXP_PATTERN.matcher(id).matches();
    }

    public String getId() {
        return id;
    }

    public boolean isUuid() {
        return this.isUuid;
    }

    public boolean isLong() {
        return !isUuid();
    }

    public Long getAsLong() {
        if (isLong()) {
            return Long.valueOf(id);
        } else {
            throw new IllegalStateException("id er UUID : " + id);
        }
    }

    public UUID getAsUuid() {
        if (isUuid()) {
            return UUID.fromString(id);
        } else {
            throw new IllegalStateException("id er UUID : " + id);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(this.getClass())) {
            return false;
        }
        var other = (BehandlingReferanse) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<id=" + (isLong() ? getAsLong() : getAsUuid()) + ">";
    }
}
