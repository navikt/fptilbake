package no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør;

import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.MappedSuperclass;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;

@MappedSuperclass
public abstract class Aktør extends BaseEntitet {

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "aktoer_id", unique = true, nullable = false, updatable = false)))
    private AktørId aktørId;

    @SuppressWarnings("unused")
    private Aktør() {
        // For Hibernate
    }

    protected Aktør(AktørId aktørId) {
        this.aktørId = aktørId;
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Aktør)
                || !(getClass().isAssignableFrom(object.getClass()) || object.getClass().isAssignableFrom(getClass()))) {
            return false;
        }
        Aktør other = (Aktør) object;
        return Objects.equals(other.aktørId, this.aktørId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId);
    }

}
