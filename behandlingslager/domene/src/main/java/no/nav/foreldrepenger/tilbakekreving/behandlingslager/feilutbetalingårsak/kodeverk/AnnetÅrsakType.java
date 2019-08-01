package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "AnnetÅrsakType")
@DiscriminatorValue(AnnetÅrsakType.DISCRIMINATOR)
public class AnnetÅrsakType extends Kodeliste {

    public static final String DISCRIMINATOR = "ANNET_AARSAK_TYPE";

    public static final AnnetÅrsakType ANNET = new AnnetÅrsakType("ANNET");

    public AnnetÅrsakType() {
        // For Hibernate
    }

    public AnnetÅrsakType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}