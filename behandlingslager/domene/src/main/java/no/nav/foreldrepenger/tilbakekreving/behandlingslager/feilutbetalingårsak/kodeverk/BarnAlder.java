package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "BarnAlder")
@DiscriminatorValue(BarnAlder.DISCRIMINATOR)
public class BarnAlder extends Kodeliste{

    public static final String DISCRIMINATOR = "BARN_ALDER";

    public static final BarnAlder BARN_ALDER_OVER_TRE = new BarnAlder("BARN_ALDER_OVER_TRE");

    public BarnAlder() {
        // For Hibernate
    }

    public BarnAlder(String kode) {
        super(kode, DISCRIMINATOR);
    }
}