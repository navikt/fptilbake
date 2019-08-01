package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "UttakKvote")
@DiscriminatorValue(UttakKvote.DISCRIMINATOR)
public class UttakKvote extends Kodeliste{

    public static final String DISCRIMINATOR = "UTTAK_KVOTE";

    public static final UttakKvote ENDRING_UTTAK_KVOTE = new UttakKvote("ENDRING_UTTAK_KVOTE");

    public UttakKvote() {
        // For Hibernate
    }

    public UttakKvote(String kode) {
        super(kode, DISCRIMINATOR);
    }
}