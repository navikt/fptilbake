package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "VedtaksbrevFritekstType")
@DiscriminatorValue(VedtaksbrevFritekstType.DISCRIMINATOR)
public class VedtaksbrevFritekstType extends Kodeliste {

    public static final String DISCRIMINATOR = "FRITEKST_TYPE"; //??

    public static final VedtaksbrevFritekstType FAKTA_AVSNITT = new VedtaksbrevFritekstType("FAKTA_AVSNITT"); //$NON-NLS-1$
    public static final VedtaksbrevFritekstType VILKAAR_AVSNITT = new VedtaksbrevFritekstType("VILKAAR_AVSNITT"); //$NON-NLS-1$
    public static final VedtaksbrevFritekstType SAERLIGE_GRUNNER_AVSNITT = new VedtaksbrevFritekstType("SAERLIGE_GRUNNER_AVSNITT"); //$NON-NLS-1$
    public static final VedtaksbrevFritekstType SAERLIGE_GRUNNER_ANNET_AVSNITT = new VedtaksbrevFritekstType("SAERLIGE_GRUNNER_ANNET_AVSNITT"); //$NON-NLS-1$

    VedtaksbrevFritekstType(){
        // hibernate
    }


    public VedtaksbrevFritekstType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
