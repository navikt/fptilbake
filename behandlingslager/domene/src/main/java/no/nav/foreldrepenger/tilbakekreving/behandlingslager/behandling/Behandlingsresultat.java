package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.vedtak.felles.jpa.BaseEntitet;

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

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "behandling_resultat_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + BehandlingResultatType.DISCRIMINATOR + "'"))
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

    public boolean erBehandlingresultatAvslåttEllerOpphørt() {
        return erBehandlingresultatAvslått() || erBehandlingresultatOpphørt();
    }

    public boolean erBehandlingresultatAvslått() {
        return BehandlingResultatType.AVSLÅTT.equals(behandlingResultatType);
    }

    public boolean erBehandlingresultatOpphørt() {
        return BehandlingResultatType.OPPHØR.equals(behandlingResultatType);
    }

    public boolean erBehandlingresultatInnvilget() {
        return BehandlingResultatType.INNVILGET.equals(behandlingResultatType);
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
