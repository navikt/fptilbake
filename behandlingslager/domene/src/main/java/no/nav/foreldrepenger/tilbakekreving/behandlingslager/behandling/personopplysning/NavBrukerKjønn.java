package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "NavBrukerKjønn")
@DiscriminatorValue(NavBrukerKjønn.DISCRIMINATOR)
public class NavBrukerKjønn extends Kodeliste {

    public static final String DISCRIMINATOR = "BRUKER_KJOENN";
    public static final NavBrukerKjønn KVINNE = new NavBrukerKjønn("K");
    public static final NavBrukerKjønn MANN = new NavBrukerKjønn("M");
    
    public static final NavBrukerKjønn UDEFINERT = new NavBrukerKjønn("-");

    NavBrukerKjønn() {
        // For Hibernate
    }

    public NavBrukerKjønn(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
