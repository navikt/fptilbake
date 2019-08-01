package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "UtbetalingSystemFeil")
@DiscriminatorValue(UtbetalingSystemFeil.DISCRIMINATOR)
public class UtbetalingSystemFeil extends Kodeliste{

    public static final String DISCRIMINATOR = "UTBETALING_SYSTEM_FEIL";

    public static final UtbetalingSystemFeil DOBBELT_UTBETALING = new UtbetalingSystemFeil("DOBBELT_UTBETALING");
    public static final UtbetalingSystemFeil UTBETALING_TIL_FEIL_MOTTAKER = new UtbetalingSystemFeil("UTBETALING_TIL_FEIL_MOTTAKER");
    public static final UtbetalingSystemFeil ANNET = new UtbetalingSystemFeil("ANNET");

    public UtbetalingSystemFeil() {
        // For Hibernate
    }

    public UtbetalingSystemFeil(String kode) {
        super(kode, DISCRIMINATOR);
    }
}