package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "UtsettelseInnleggelse")
@DiscriminatorValue(UtsettelseInnleggelse.DISCRIMINATOR)
public class UtsettelseInnleggelse extends Kodeliste{

    public static final String DISCRIMINATOR = "UTSETTELSE_INNLEGGELSE";

    public static final UtsettelseInnleggelse INNLEGGELSE_BRUKER = new UtsettelseInnleggelse("INNLEGGELSE_BRUKER");
    public static final UtsettelseInnleggelse INNLEGGELSE_BARNET = new UtsettelseInnleggelse("INNLEGGELSE_BARNET");

    public UtsettelseInnleggelse() {
        // For Hibernate
    }

    public UtsettelseInnleggelse(String kode) {
        super(kode, DISCRIMINATOR);
    }
}