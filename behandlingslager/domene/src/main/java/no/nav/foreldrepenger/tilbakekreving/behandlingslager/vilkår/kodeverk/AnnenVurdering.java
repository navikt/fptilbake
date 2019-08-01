package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "AnnenVurdering")
@DiscriminatorValue(AnnenVurdering.DISCRIMINATOR)
public class AnnenVurdering extends Vurdering {

    public static final String DISCRIMINATOR = "VURDERING";

    public static final AnnenVurdering GOD_TRO = new AnnenVurdering("GOD_TRO");
    public static final AnnenVurdering FORELDET = new AnnenVurdering("FORELDET");

    AnnenVurdering(String kode) {
        super(kode, DISCRIMINATOR);
    }

    AnnenVurdering() {
        // For hibernate
    }

}
