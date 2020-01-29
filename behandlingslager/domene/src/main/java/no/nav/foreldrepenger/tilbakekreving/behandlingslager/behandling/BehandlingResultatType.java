package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "BehandlingResultatType")
@DiscriminatorValue(BehandlingResultatType.DISCRIMINATOR)
public class BehandlingResultatType extends Kodeliste {

    public static final String DISCRIMINATOR = "BEHANDLING_RESULTAT_TYPE";

    public static final BehandlingResultatType IKKE_FASTSATT = new BehandlingResultatType("IKKE_FASTSATT"); //$NON-NLS-1$
    public static final BehandlingResultatType FASTSATT = new BehandlingResultatType("FASTSATT"); //$NON-NLS-1$
    public static final BehandlingResultatType HENLAGT_FEILOPPRETTET = new BehandlingResultatType("HENLAGT_FEILOPPRETTET"); //$NON-NLS-1$
    public static final BehandlingResultatType HENLAGT_KRAVGRUNNLAG_NULLSTILT = new BehandlingResultatType("HENLAGT_KRAVGRUNNLAG_NULLSTILT"); //$NON-NLS-1$
    public static final BehandlingResultatType HENLAGT_TEKNISK_VEDLIKEHOLD = new BehandlingResultatType("HENLAGT_TEKNISK_VEDLIKEHOLD"); //$NON-NLS-1$
    public static final BehandlingResultatType ENDRET = new BehandlingResultatType("ENDRET"); //$NON-NLS-1$
    public static final BehandlingResultatType INGEN_ENDRING = new BehandlingResultatType("INGEN_ENDRING"); //$NON-NLS-1$

    private static final Set<BehandlingResultatType> ALLE_HENLEGGELSESKODER = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(HENLAGT_KRAVGRUNNLAG_NULLSTILT, HENLAGT_FEILOPPRETTET, HENLAGT_TEKNISK_VEDLIKEHOLD)));

    protected BehandlingResultatType() {
        // Hibernate trenger denne
    }

    protected BehandlingResultatType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static Set<BehandlingResultatType> getAlleHenleggelseskoder() {
        return ALLE_HENLEGGELSESKODER;
    }

    public boolean erHenlagt() {
        return ALLE_HENLEGGELSESKODER.contains(this);
    }

}
