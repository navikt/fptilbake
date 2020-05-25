package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "VergeType")
@DiscriminatorValue(VergeType.DISCRIMINATOR)
public class VergeType extends Kodeliste {

    public static final String DISCRIMINATOR = "VERGE_TYPE";

    public static final VergeType BARN = new VergeType("BARN");
    public static final VergeType FBARN = new VergeType("FBARN");
    public static final VergeType VOKSEN = new VergeType("VOKSEN");
    public static final VergeType ADVOKAT = new VergeType("ADVOKAT");
    public static final VergeType ANNEN_F = new VergeType("ANNEN_F");
    public static final VergeType UDEFINERT = new VergeType("-");

    VergeType() {
        // For Hibernate
    }

    private VergeType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
