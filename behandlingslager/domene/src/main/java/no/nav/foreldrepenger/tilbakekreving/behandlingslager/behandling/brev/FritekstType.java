package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "FritekstType")
@DiscriminatorValue(FritekstType.DISCRIMINATOR)
public class FritekstType extends Kodeliste {

    public static final String DISCRIMINATOR = "FRITEKST_TYPE"; //??

    public static final FritekstType FAKTA_AVSNITT = new FritekstType("FAKTA_AVSNITT"); //$NON-NLS-1$
    public static final FritekstType VILKAAR_AVSNITT = new FritekstType("VILKAAR_AVSNITT"); //$NON-NLS-1$
    public static final FritekstType SAERLIGE_GRUNNER_AVSNITT = new FritekstType("SAERLIGE_GRUNNER_AVSNITT"); //$NON-NLS-1$

    FritekstType(){
        // hibernate
    }


    public FritekstType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
