package no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@Entity(name = "VidereBehandling")
@DiscriminatorValue(VidereBehandling.DISCRIMINATOR)
public class VidereBehandling extends Kodeliste {

    public static final String DISCRIMINATOR = "TILBAKEKR_VIDERE_BEH";

    public static final VidereBehandling TILBAKEKREV_I_INFOTRYGD = new VidereBehandling("TILBAKEKR_INFOTRYGD");
    public static final VidereBehandling IGNORER_TILBAKEKREVING = new VidereBehandling("TILBAKEKR_IGNORER");
    public static final VidereBehandling INNTREKK = new VidereBehandling("TILBAKEKR_INNTREKK");
    public static final VidereBehandling TILBAKEKR_OPPDATER = new VidereBehandling("TILBAKEKR_OPPDATER");

    public static final VidereBehandling UDEFINERT = new VidereBehandling("-");

    private static final Map<String, VidereBehandling> KODE_MAP = new HashMap<>();

    static {
        KODE_MAP.put(TILBAKEKREV_I_INFOTRYGD.getKode(), TILBAKEKREV_I_INFOTRYGD);
        KODE_MAP.put(IGNORER_TILBAKEKREVING.getKode(), IGNORER_TILBAKEKREVING);
        KODE_MAP.put(INNTREKK.getKode(), INNTREKK);
        KODE_MAP.put(TILBAKEKR_OPPDATER.getKode(), TILBAKEKR_OPPDATER);
    }

    VidereBehandling() {
        // for hibernate
    }

    private VidereBehandling(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static VidereBehandling fraKode(String kode) {
        if (KODE_MAP.containsKey(kode)) {
            return KODE_MAP.get(kode);
        }
        throw VidereBehandlingFeil.FEILFACTORY.ugyldigVidereBehandling(kode).toException();
    }

    interface VidereBehandlingFeil extends DeklarerteFeil {

        VidereBehandlingFeil FEILFACTORY = FeilFactory.create(VidereBehandlingFeil.class);

        @TekniskFeil(feilkode = "FPT-312927", feilmelding = "VidereBehandling '%s' er ugyldig", logLevel = LogLevel.WARN)
        Feil ugyldigVidereBehandling(String videreBehandling);
    }

}
