package no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn;

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

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.diff.IndexKey;


/**
 * Tilbakemelding fra beslutter for å be saksbehandler vurdere et aksjonspunkt på nytt.
 */
@Entity(name = "VurderÅrsakTotrinnsvurdering")
@Table(name = "VURDER_AARSAK_TTVURDERING")
public class VurderÅrsakTotrinnsvurdering extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VURDER_AARSAK_TTVURDERING")
    private Long id;

    @Convert(converter = VurderÅrsak.KodeverdiConverter.class)
    @Column(name = "aarsak_type", nullable = false, updatable = false)
    private VurderÅrsak årsaksType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "totrinnsvurdering_id", nullable = false, updatable = false)
    private Totrinnsvurdering totrinnsvurdering;

    VurderÅrsakTotrinnsvurdering() {
        // for Hibernate
    }

    public VurderÅrsakTotrinnsvurdering(VurderÅrsak type, Totrinnsvurdering totrinnsvurdering) {
        this.totrinnsvurdering = totrinnsvurdering;
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
        if (!(o instanceof VurderÅrsakTotrinnsvurdering)) {
            return false;
        }
        VurderÅrsakTotrinnsvurdering that = (VurderÅrsakTotrinnsvurdering) o;
        return Objects.equals(getÅrsaksType(), that.getÅrsaksType()) &&
                Objects.equals(totrinnsvurdering, that.totrinnsvurdering);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getÅrsaksType(), totrinnsvurdering);
    }
}
