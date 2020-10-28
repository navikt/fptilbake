package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;


@Entity(name = "BehandlingTypeStegSekvens")
@Table(name = "BEHANDLING_TYPE_STEG_SEKV")
public class BehandlingTypeStegSekvens extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BEHANDLING_TYPE_STEG_SEKV")
    private Long id;

    @Convert(converter = BehandlingType.KodeverdiConverter.class)
    @Column(name = "behandling_type", nullable = false)
    private BehandlingType behandlingType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "behandling_steg_type", nullable = false, updatable = false, insertable = false)
    private BehandlingStegType stegType;

    @Column(name = "sekvens_nr", nullable = false, updatable = false, insertable = false)
    private Integer sekvensNr = 1;

    BehandlingTypeStegSekvens() {
        // Hibernate
    }

    public BehandlingStegType getStegType() {
        return stegType;
    }

    public BehandlingType getBehandlingType() {
        return behandlingType;
    }

    public Integer getSekvensNr() {
        return sekvensNr;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BehandlingTypeStegSekvens)) {
            return false;
        }
        BehandlingTypeStegSekvens other = (BehandlingTypeStegSekvens) obj;
        return Objects.equals(behandlingType, other.behandlingType)
                && Objects.equals(stegType, other.stegType)
                && Objects.equals(sekvensNr, other.sekvensNr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(behandlingType, stegType, sekvensNr);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<behandlingType=" + behandlingType //$NON-NLS-1$
                + ", stegType=" + stegType //$NON-NLS-1$
                + ", sekvensNr=" + sekvensNr //$NON-NLS-1$
                + ">"; //$NON-NLS-1$

    }
}
