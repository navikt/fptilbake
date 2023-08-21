package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;

@Entity(name = "BehandlingÅrsak")
@Table(name = "BEHANDLING_ARSAK")
public class BehandlingÅrsak extends BaseEntitet {

    @Id
    @SequenceGenerator(name = "behandling_aarsak_sekvens", sequenceName = "SEQ_BEHANDLING_ARSAK")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "behandling_aarsak_sekvens")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @ManyToOne(optional = false)
    @JoinColumn(name = "behandling_id", nullable = false, updatable = false)
    private Behandling behandling;

    @Convert(converter = BehandlingÅrsakType.KodeverdiConverter.class)
    @Column(name = "behandling_arsak_type", nullable = false)
    private BehandlingÅrsakType behandlingÅrsakType = BehandlingÅrsakType.UDEFINERT;

    @OneToOne
    @JoinColumn(name = "original_behandling_id", updatable = false)
    private Behandling originalBehandling;

    BehandlingÅrsak() {
        // for hibernate
    }

    public Long getId() {
        return id;
    }

    public Long getBehandlingId() {
        return behandling.getId();
    }

    public BehandlingÅrsakType getBehandlingÅrsakType() {
        return behandlingÅrsakType;
    }

    public Optional<Behandling> getOriginalBehandling() {
        return Optional.ofNullable(originalBehandling);
    }

    public static Builder builder(BehandlingÅrsakType behandlingÅrsakType) {
        return new Builder(behandlingÅrsakType);
    }


    void setBehandling(Behandling behandling) {
        this.behandling = behandling;
    }

    public static class Builder {

        private BehandlingÅrsakType behandlingÅrsakType;
        private Behandling originalBehandling;

        public Builder(BehandlingÅrsakType behandlingÅrsakType) {
            Objects.requireNonNull(behandlingÅrsakType, "behandlingÅrsakTyper");
            this.behandlingÅrsakType = behandlingÅrsakType;
        }

        public Builder medOriginalBehandling(Behandling originalBehandling) {
            this.originalBehandling = originalBehandling;
            return this;
        }

        public List<BehandlingÅrsak> buildFor(Behandling behandling) {
            Objects.requireNonNull(behandling, "behandling");
            BehandlingÅrsak årsak = new BehandlingÅrsak();
            årsak.behandling = behandling;
            årsak.behandlingÅrsakType = behandlingÅrsakType;
            årsak.originalBehandling = this.originalBehandling;
            behandling.leggTilBehandlingÅrsaker(årsak);
            return behandling.getBehandlingÅrsaker();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BehandlingÅrsak that = (BehandlingÅrsak) o;

        return Objects.equals(behandlingÅrsakType, that.behandlingÅrsakType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(behandlingÅrsakType);
    }
}
