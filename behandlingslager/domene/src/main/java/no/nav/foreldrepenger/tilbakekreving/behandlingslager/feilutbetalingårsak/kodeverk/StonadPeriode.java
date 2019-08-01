package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "StonadPeriode")
@DiscriminatorValue(StonadPeriode.DISCRIMINATOR)
public class StonadPeriode extends Kodeliste{

    public static final String DISCRIMINATOR = "STONAD_PERIODE";

    public static final StonadPeriode MOR_STONAD_PERIODE = new StonadPeriode("MOR_STONAD_PERIODE");

    public static final StonadPeriode MANGLENDE_STONAD_PERIODE = new StonadPeriode("MANGLENDE_STONAD_PERIODE");

    public static final StonadPeriode STONAD_PERIODE = new StonadPeriode(DISCRIMINATOR);

    public StonadPeriode() {
        // For Hibernate
    }

    public StonadPeriode(String kode) {
        super(kode, DISCRIMINATOR);
    }
}