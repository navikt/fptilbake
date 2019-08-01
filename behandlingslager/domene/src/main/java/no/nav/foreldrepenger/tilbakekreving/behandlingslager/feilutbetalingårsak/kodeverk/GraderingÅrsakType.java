package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "GraderingÅrsakType")
@DiscriminatorValue(GraderingÅrsakType.DISCRIMINATOR)
public class GraderingÅrsakType extends Kodeliste{

    public static final String DISCRIMINATOR = "GRADERING_AARSAK_TYPE";

    public static final GraderingÅrsakType GRADERING = new GraderingÅrsakType("GRADERING");

    public GraderingÅrsakType() {
        // For Hibernate
    }

    public GraderingÅrsakType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}