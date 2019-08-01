package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "EndringUttakKvote")
@DiscriminatorValue(EndringUttakKvote.DISCRIMINATOR)
public class EndringUttakKvote extends Kodeliste{

    public static final String DISCRIMINATOR = "ENDRING_UTTAK_KVOTE_TYPE";

    public static final EndringUttakKvote SYKDOM = new EndringUttakKvote("SYKDOM");
    public static final EndringUttakKvote INNLEGGELSE = new EndringUttakKvote("INNLEGGELSE");

    public EndringUttakKvote() {
        // For Hibernate
    }

    public EndringUttakKvote(String kode) {
        super(kode, DISCRIMINATOR);
    }
}