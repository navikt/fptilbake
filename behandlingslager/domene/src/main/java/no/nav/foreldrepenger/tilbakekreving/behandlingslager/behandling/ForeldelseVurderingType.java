package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "ForeldelseVurderingType")
@DiscriminatorValue(ForeldelseVurderingType.DISCRIMINATOR)
public class ForeldelseVurderingType extends Kodeliste {

    public static final String DISCRIMINATOR = "FORELDELSE_VURDERING";

    public static final ForeldelseVurderingType IKKE_VURDERT = new ForeldelseVurderingType("IKKE_VURDERT"); //$NON-NLS-1$
    public static final ForeldelseVurderingType FORELDET = new ForeldelseVurderingType("FORELDET"); //$NON-NLS-1$
    public static final ForeldelseVurderingType IKKE_FORELDET = new ForeldelseVurderingType("IKKE_FORELDET"); //$NON-NLS-1$
    public static final ForeldelseVurderingType TILLEGGSFRIST = new ForeldelseVurderingType("TILLEGGSFRIST"); //$NON-NLS-1$
    public static final ForeldelseVurderingType UDEFINERT = new ForeldelseVurderingType("-"); //$NON-NLS-1$

    ForeldelseVurderingType(){
        // hibernate
    }


    public ForeldelseVurderingType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
