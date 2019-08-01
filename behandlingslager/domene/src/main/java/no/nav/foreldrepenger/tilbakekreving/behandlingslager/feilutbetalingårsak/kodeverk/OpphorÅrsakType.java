package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "OpphorÅrsakType")
@DiscriminatorValue(OpphorÅrsakType.DISCRIMINATOR)
public class OpphorÅrsakType extends Kodeliste{

    public static final String DISCRIMINATOR = "OPPHOR_AARSAK_TYPE";

    public static final OpphorÅrsakType OPPHOR_DOD = new OpphorÅrsakType("OPPHOR_DOD");

    public OpphorÅrsakType() {
        // For Hibernate
    }

    public OpphorÅrsakType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}