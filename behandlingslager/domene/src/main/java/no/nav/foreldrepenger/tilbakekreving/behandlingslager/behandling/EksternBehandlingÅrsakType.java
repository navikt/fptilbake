package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "BehandlingÅrsakType")
@DiscriminatorValue(EksternBehandlingÅrsakType.DISCRIMINATOR)
public class EksternBehandlingÅrsakType extends Kodeliste {
    public static final String DISCRIMINATOR = "BEHANDLING_AARSAK"; //$NON-NLS-1$

    public static final EksternBehandlingÅrsakType UDEFINERT = new EksternBehandlingÅrsakType("-"); //$NON-NLS-1$

    EksternBehandlingÅrsakType() {
        //for Hibernate
    }

    private EksternBehandlingÅrsakType(String kode) {
        super(kode, DISCRIMINATOR);
    }


}
