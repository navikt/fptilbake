package no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

public enum VidereBehandling implements Kodeverdi {

    TILBAKEKR_OPPRETT("TILBAKEKR_OPPRETT", "Feilutbetaling med tilbakekreving"),
    IGNORER_TILBAKEKREVING("TILBAKEKR_IGNORER", "Feilutbetaling, avvent samordning"),
    INNTREKK("TILBAKEKR_INNTREKK", "Feilutbetaling hvor inntrekk dekker hele beløpet"),
    TILBAKEKR_OPPDATER("TILBAKEKR_OPPDATER", "Endringer vil oppdatere eksisterende feilutbetalte perioder og beløp."),

    UDEFINERT("-", "UDefinert");

    private String kode;
    private String navn;

    public static final String KODEVERK = "TILBAKEKR_VIDERE_BEH";
    private static final Map<String, VidereBehandling> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private VidereBehandling(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static VidereBehandling fraKode(final String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent VidereBehandling: " + kode);
        }
        return ad;
    }

    public static Map<String, VidereBehandling> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @JsonValue
    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @Override
    public String getNavn() {
        return navn;
    }
}
