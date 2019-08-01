package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "ÅrsakUdefinert")
@DiscriminatorValue(ÅrsakUdefinert.DISCRIMINATOR)
public class ÅrsakUdefinert extends Kodeliste{

    public static final String DISCRIMINATOR = "AARSAK_UDEFINERT";

    public static final ÅrsakUdefinert UDEFINERT = new ÅrsakUdefinert("-");

    public ÅrsakUdefinert() {
        // For Hibernate
    }

    public ÅrsakUdefinert(String kode) {
        super(kode, DISCRIMINATOR);
    }
}