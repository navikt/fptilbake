package no.nav.foreldrepenger.tilbakekreving.økonomixml;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "MeldingType")
@DiscriminatorValue(MeldingType.DISCRIMINATOR)
public class MeldingType extends Kodeliste {

    public static final String DISCRIMINATOR = "MELDING_TYPE";

    public static final MeldingType VEDTAK = new MeldingType("VEDTAK");
    public static final MeldingType ANNULERE_GRUNNLAG = new MeldingType("ANNULERE_GRUNNLAG");

    MeldingType() {
        // For hibernate
    }

    private MeldingType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}


