package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "Opptjening")
@DiscriminatorValue(Opptjening.DISCRIMINATOR)
public class Opptjening extends Kodeliste{

    public static final String DISCRIMINATOR = "OPPTJENING";

    public static final Opptjening INGEN_INTEKT = new Opptjening("INGEN_INTEKT");
    public static final Opptjening INTEKT_UNDER = new Opptjening("INTEKT_UNDER");

    public Opptjening() {
        // For Hibernate
    }

    public Opptjening(String kode) {
        super(kode, DISCRIMINATOR);
    }
}