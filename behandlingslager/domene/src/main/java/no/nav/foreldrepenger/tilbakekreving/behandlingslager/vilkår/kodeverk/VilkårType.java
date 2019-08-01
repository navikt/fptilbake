package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "VilkarType")
@DiscriminatorValue(VilkårType.DISCRIMINATOR)
public class VilkårType extends Kodeliste {

    public static final String DISCRIMINATOR = "VILKAR_TYPE"; //$NON-NLS-1$

    /**
     * Brukes i stedet for null der det er optional.
     */
    public static final VilkårType UDEFINERT = new VilkårType("-"); //$NON-NLS-1$

    VilkårType() {
        // Hibernate trenger den
    }

    public VilkårType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
