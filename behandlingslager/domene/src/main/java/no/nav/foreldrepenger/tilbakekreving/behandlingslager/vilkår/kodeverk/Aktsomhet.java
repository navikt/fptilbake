package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@Entity(name = "Aksomhet")
@DiscriminatorValue(Aktsomhet.DISCRIMINATOR)
public class Aktsomhet extends Vurdering {

    public static final String DISCRIMINATOR = "AKTSOMHET";

    public static final Aktsomhet FORSETT = new Aktsomhet("FORSETT");
    public static final Aktsomhet GROVT_UAKTSOM = new Aktsomhet("GROVT_UAKTSOM");
    public static final Aktsomhet SIMPEL_UAKTSOM = new Aktsomhet("SIMPEL_UAKTSOM");

    private static final Map<String, Aktsomhet> aktsomhetMap = new HashMap<>();

    static {
        aktsomhetMap.put(FORSETT.getKode(), FORSETT);
        aktsomhetMap.put(GROVT_UAKTSOM.getKode(), GROVT_UAKTSOM);
        aktsomhetMap.put(SIMPEL_UAKTSOM.getKode(), SIMPEL_UAKTSOM);
    }

    Aktsomhet(String kode) {
        super(kode, DISCRIMINATOR);
    }

    Aktsomhet() {
        // For hibernate
    }

    public static Aktsomhet fraKode(String aktsomhet) {
        if (aktsomhetMap.containsKey(aktsomhet)) {
            return aktsomhetMap.get(aktsomhet);
        }
        throw AktsomhetFeil.FEILFACTORY.ugyldigAktsomhet(aktsomhet).toException();
    }

    interface AktsomhetFeil extends DeklarerteFeil {

        AktsomhetFeil FEILFACTORY = FeilFactory.create(AktsomhetFeil.class);

        @TekniskFeil(feilkode = "FPT-312924", feilmelding = "Aktsomhet '%s' er ugyldig", logLevel = LogLevel.WARN)
        Feil ugyldigAktsomhet(String aktsomhet);
    }
}
