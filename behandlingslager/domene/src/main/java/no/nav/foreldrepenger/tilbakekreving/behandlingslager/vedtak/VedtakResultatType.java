package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "VedtakResultatType")
@DiscriminatorValue(VedtakResultatType.DISCRIMINATOR)
public class VedtakResultatType extends Kodeliste {

    public static final String DISCRIMINATOR = "VEDTAK_RESULTAT_TYPE";

    public static final VedtakResultatType FULL_TILBAKEBETALING = new VedtakResultatType("FULL_TILBAKEBETALING");
    public static final VedtakResultatType DELVIS_TILBAKEBETALING = new VedtakResultatType("DELVIS_TILBAKEBETALING");
    public static final VedtakResultatType INGEN_TILBAKEBETALING = new VedtakResultatType("INGEN_TILBAKEBETALING");
    public static final VedtakResultatType UDEFINERT = new VedtakResultatType("-");

    VedtakResultatType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    VedtakResultatType() {
        // For hibernate
    }

}
