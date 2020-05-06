package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;


@Entity(name = "SkjermlenkeType")
@DiscriminatorValue(SkjermlenkeType.DISCRIMINATOR)
public class SkjermlenkeType extends Kodeliste {

    public static final String DISCRIMINATOR = "SKJERMLENKE_TYPE"; //$NON-NLS-1$

    public static final SkjermlenkeType UDEFINERT = new SkjermlenkeType("-");
    public static final SkjermlenkeType FAKTA_OM_FEILUTBETALING = new SkjermlenkeType("FAKTA_OM_FEILUTBETALING");
    public static final SkjermlenkeType TILBAKEKREVING = new SkjermlenkeType("TILBAKEKREVING");
    public static final SkjermlenkeType FORELDELSE = new SkjermlenkeType("FORELDELSE");
    public static final SkjermlenkeType VEDTAK = new SkjermlenkeType("VEDTAK");
    public static final SkjermlenkeType FAKTA_OM_VERGE = new SkjermlenkeType("FAKTA_OM_VERGE");


    public SkjermlenkeType() {
        //
    }

    public SkjermlenkeType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
