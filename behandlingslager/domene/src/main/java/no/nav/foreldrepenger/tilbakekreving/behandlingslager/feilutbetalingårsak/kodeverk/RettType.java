package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "RettType")
@DiscriminatorValue(RettType.DISCRIMINATOR)
public class RettType extends Kodeliste{

    public static final String DISCRIMINATOR = "RETT_TYPE";

    public static final RettType FAR_RETT = new RettType("FAR_RETT");

    public RettType() {
        // For Hibernate
    }

    public RettType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}