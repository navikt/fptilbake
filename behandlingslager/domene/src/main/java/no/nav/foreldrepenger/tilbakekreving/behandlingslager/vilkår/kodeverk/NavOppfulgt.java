package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "NavOppfulgt")
@DiscriminatorValue(NavOppfulgt.DISCRIMINATOR)
public class NavOppfulgt extends Kodeliste {

    public static final String DISCRIMINATOR = "NAV_OPPFULGT";

    public static final NavOppfulgt NAV_KAN_IKKE_LASTES = new NavOppfulgt("NAV_ULASTBAR");
    public static final NavOppfulgt HAR_IKKE_FULGT_OPP = new NavOppfulgt("HAR_IKKE_FULGT_OPP");
    public static final NavOppfulgt HAR_BENYTTET_FEIL = new NavOppfulgt("HAR_BENYTTET_FEIL");
    public static final NavOppfulgt HAR_IKKE_SJEKKET = new NavOppfulgt("HAR_IKKE_SJEKKET");
    public static final NavOppfulgt BEREGNINGS_FEIL = new NavOppfulgt("BEREGNINGS_FEIL");
    public static final NavOppfulgt HAR_UTFØRT_FEIL = new NavOppfulgt("HAR_UTFOERT_FEIL");
    public static final NavOppfulgt HAR_SENDT_TIL_FEIL_MOTTAKER = new NavOppfulgt("HAR_SENDT_TIL_FEIL_MOTTAKER");

    public static final NavOppfulgt UDEFINERT = new NavOppfulgt("-");

    NavOppfulgt(String kode) {
        super(kode, DISCRIMINATOR);
    }

    NavOppfulgt() {
        // For hibernate
    }
}
