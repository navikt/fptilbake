package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "OptjeningType")
@DiscriminatorValue(OpptjeningType.DISCRIMINATOR)
public class OpptjeningType extends Kodeliste{

    public static final String DISCRIMINATOR = "OPPTJENING_TYPE";

    public static final OpptjeningType OPPTJENING_TYPE = new OpptjeningType(DISCRIMINATOR);

    public OpptjeningType() {
        // For Hibernate
    }

    public OpptjeningType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}