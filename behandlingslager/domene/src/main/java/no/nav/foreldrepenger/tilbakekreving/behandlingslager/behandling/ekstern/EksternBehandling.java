package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern;

import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.NaturalId;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "EksternBehandling")
@Table(name = "EKSTERN_BEHANDLING")
public class EksternBehandling extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_EKSTERN_BEHANDLING")
    private Long id;

    @Column(name = "intern_id", nullable = false)
    private Long internId;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private Boolean aktiv = true;

    @NaturalId
    @Column(name = "ekstern_uuid")
    private UUID eksternUuid;

    private Henvisning henvisning;

    EksternBehandling() {
        // Hibernate
    }

    public EksternBehandling(Behandling behandling, Henvisning henvisning, UUID eksternUuid) {
        Objects.requireNonNull(behandling, "behandlingId");
        Objects.requireNonNull(henvisning, "henvisning");
        Objects.requireNonNull(eksternUuid, "eksternUuid");

        this.internId = behandling.getId();
        this.henvisning = henvisning;
        this.eksternUuid = eksternUuid;
    }

    public Long getId() {
        return id;
    }

    public Long getInternId() {
        return internId;
    }

    public Boolean getAktiv() {
        return aktiv;
    }

    public Henvisning getHenvisning() {
        return henvisning;
    }

    public void deaktiver() {
        aktiv = false;
    }

    public void reaktivate() {
        aktiv = true;
    }

    public UUID getEksternUuid() {
        return eksternUuid;
    }

    public void setEksternUuid(UUID eksternUuid) {
        this.eksternUuid = eksternUuid;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof EksternBehandling)) {
            return false;
        }
        EksternBehandling that = (EksternBehandling) object;
        return Objects.equals(internId, that.internId) &&
            Objects.equals(getHenvisning(), that.getHenvisning());
    }

    @Override
    public int hashCode() {
        return Objects.hash(internId, getHenvisning());
    }
}
