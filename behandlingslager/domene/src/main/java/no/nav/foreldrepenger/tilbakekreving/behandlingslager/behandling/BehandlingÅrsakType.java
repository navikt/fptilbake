package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "BehandlingÅrsakType")
@DiscriminatorValue(BehandlingÅrsakType.DISCRIMINATOR)
public class BehandlingÅrsakType extends Kodeliste {

    public static final String DISCRIMINATOR = "BEHANDLING_AARSAK"; //$NON-NLS-1$

    public static final BehandlingÅrsakType RE_KLAGE_NFP = new BehandlingÅrsakType("RE_KLAGE_NFP"); //$NON-NLS-1$
    public static final BehandlingÅrsakType RE_KLAGE_KA = new BehandlingÅrsakType("RE_KLAGE_KA"); //$NON-NLS-1$
    public static final BehandlingÅrsakType RE_OPPLYSNINGER_OM_VILKÅR = new BehandlingÅrsakType("RE_VILKÅR"); //$NON-NLS-1$
    public static final BehandlingÅrsakType RE_OPPLYSNINGER_OM_FORELDELSE = new BehandlingÅrsakType("RE_FORELDELSE"); //$NON-NLS-1$

    public static final BehandlingÅrsakType UDEFINERT = new BehandlingÅrsakType("-"); //$NON-NLS-1$

    BehandlingÅrsakType() {
        //for Hibernate
    }

    private BehandlingÅrsakType(String kode) {
        super(kode, DISCRIMINATOR);
    }


    public static final Set<BehandlingÅrsakType> KLAGE_ÅRSAKER = Set.of(BehandlingÅrsakType.RE_KLAGE_KA, BehandlingÅrsakType.RE_KLAGE_NFP);

}
