package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt;

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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.diff.IndexKey;


/**
 * Tilbakemelding fra beslutter for å be saksbehandler vurdere et aksjonspunkt på nytt.
 */
@Entity(name = "VurderPåNyttÅrsak")
@Table(name = "VURDER_PAA_NYTT_AARSAK")
public class VurderPåNyttÅrsak extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VURDER_PAA_NYTT_AARSAK")
    private Long id;

    @Convert(converter = VurderÅrsak.KodeverdiConverter.class)
    @Column(name = "aarsak_type", nullable = false, updatable = false)
    private VurderÅrsak årsaksType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aksjonspunkt_id", nullable = false, updatable = false)
    private Aksjonspunkt aksjonspunkt;

    VurderPåNyttÅrsak() {
        // for Hibernate
    }

    public VurderPåNyttÅrsak(VurderÅrsak type, Aksjonspunkt aksjonspunkt) {
        this.aksjonspunkt = aksjonspunkt;
        this.årsaksType = type;
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(årsaksType);
    }

    public VurderÅrsak getÅrsaksType() {
        return årsaksType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VurderPåNyttÅrsak)) {
            return false;
        }
        VurderPåNyttÅrsak that = (VurderPåNyttÅrsak) o;
        return Objects.equals(getÅrsaksType(), that.getÅrsaksType()) &&
                Objects.equals(aksjonspunkt, that.aksjonspunkt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getÅrsaksType(), aksjonspunkt);
    }
}
