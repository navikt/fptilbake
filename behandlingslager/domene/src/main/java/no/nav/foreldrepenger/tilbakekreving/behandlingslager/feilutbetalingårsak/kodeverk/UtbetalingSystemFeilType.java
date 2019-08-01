package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "UtbetalingSystemFeilType")
@DiscriminatorValue(UtbetalingSystemFeilType.DISCRIMINATOR)
public class UtbetalingSystemFeilType extends Kodeliste{

    public static final String DISCRIMINATOR = "UTBETALING_SYSTEM_FEIL_TYPE";

    public static final UtbetalingSystemFeilType UTBETALING_SYSTEM_FEIL_TYPE = new UtbetalingSystemFeilType(DISCRIMINATOR);

    public UtbetalingSystemFeilType() {
        // For Hibernate
    }

    public UtbetalingSystemFeilType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}