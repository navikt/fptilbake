package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;

@Entity(name = "Behandlingresultat")
@Table(name = "BEHANDLING_RESULTAT")
public class Behandlingsresultat extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BEHANDLING_RESULTAT")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @ManyToOne(optional = false)
    @JoinColumn(name = "behandling_id", nullable = false, updatable = false)
    private Behandling behandling;

    @Convert(converter = BehandlingResultatType.KodeverdiConverter.class)
    @Column(name = "behandling_resultat_type", nullable = false)
    private BehandlingResultatType behandlingResultatType = BehandlingResultatType.IKKE_FASTSATT;

    Behandlingsresultat() {
        // Hibernate
    }

    public Long getId() {
        return id;
    }

    public long getVersjon() {
        return versjon;
    }

    public Behandling getBehandling() {
        return behandling;
    }

    public BehandlingResultatType getBehandlingResultatType() {
        return behandlingResultatType;
    }

    public boolean erBehandlingHenlagt() {
        return BehandlingResultatType.getAlleHenleggelseskoder().contains(behandlingResultatType);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builderEndreEksisterende(Behandlingsresultat behandlingsresultat) {
        return new Builder(behandlingsresultat, true);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "<behandling: " + behandling.getId()
                + " resultatType: " + behandlingResultatType.getKode() + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Behandlingsresultat)) {
            return false;
        }
        Behandlingsresultat that = (Behandlingsresultat) o;
        // Behandlingsresultat skal p.t. kun eksisterere dersom parent Behandling allerede er persistert.
        // Det syntaktisk korrekte vil derfor være at subaggregat Behandlingsresultat med 1:1-forhold til parent
        // Behandling har også sin id knyttet opp mot Behandling alene.
        return getBehandling().getId().equals(that.getBehandling().getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBehandling());
    }

    public static class Builder {

        private Behandlingsresultat behandlingsresultat = new Behandlingsresultat();

        public Builder() {
            // tom constructor
        }

        Builder(Behandlingsresultat gammeltResultat, boolean endreEksisterende) {
            if (endreEksisterende) {
                behandlingsresultat = gammeltResultat;
            }
        }

        public Builder medBehandling(Behandling behandling) {
            behandlingsresultat.behandling = behandling;
            return this;
        }

        public Builder medBehandlingResultatType(BehandlingResultatType behandlingResultatType) {
            behandlingsresultat.behandlingResultatType = behandlingResultatType;
            return this;
        }

        public Behandlingsresultat build() {
            Objects.requireNonNull(behandlingsresultat.behandling, "Behandling");
            return behandlingsresultat;
        }

    }
}
