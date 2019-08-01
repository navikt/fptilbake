package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "MedlemskapVilkåretType")
@DiscriminatorValue(MedlemskapVilkåretType.DISCRIMINATOR)
public class MedlemskapVilkåretType extends Kodeliste{

    public static final String DISCRIMINATOR = "MEDLEMSKAP_VILKAARET_TYPE";

    public static final MedlemskapVilkåretType MEDLEMSKAP_VILKAARET_TYPE = new MedlemskapVilkåretType(DISCRIMINATOR);

    public MedlemskapVilkåretType() {
        // For Hibernate
    }

    public MedlemskapVilkåretType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}