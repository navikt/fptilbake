package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "HendelseUnderType")
@DiscriminatorValue(HendelseUnderType.DISCRIMINATOR)
public class HendelseUnderType extends Kodeliste {

    public static final String DISCRIMINATOR = "HENDELSE_UNDERTYPE";

    public HendelseUnderType() {
        // For Hibernate
    }

    public HendelseUnderType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
