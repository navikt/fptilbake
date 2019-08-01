package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "Opphor")
@DiscriminatorValue(Opphor.DISCRIMINATOR)
public class Opphor extends Kodeliste{

    public static final String DISCRIMINATOR = "OPPHOR";

    public static final Opphor BARN_DOD = new Opphor("BARN_DOD");
    public static final Opphor STONADSMOTTAKER_DOD = new Opphor("STONADSMOTTAKER_DOD");

    public Opphor() {
        // For Hibernate
    }

    public Opphor(String kode) {
        super(kode, DISCRIMINATOR);
    }
}