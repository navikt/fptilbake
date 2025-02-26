package no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;


public enum Fagsystem implements Kodeverdi {


    FPSAK("FPSAK", "FS36"),
    K9SAK("K9", "K9"),
    TPS("TPS", "FS03"),
    JOARK("JOARK", "AS36"),
    INFOTRYGD("INFOTRYGD", "IT01"),
    ARENA("ARENA", "AO01"),
    INNTEKT("INNTEKT", "FS28"),
    MEDL("MEDL", "FS18"),
    GOSYS("GOSYS", "FS22"),
    ENHETSREGISTERET("ENHETSREGISTERET", "ER01"),
    AAREGISTERET("AAREGISTERET", "AR01"),
    FPTILBAKE("FPTILBAKE", ""),
    K9TILBAKE("K9TILBAKE", "");

    public static final String KODEVERK = "FAGSYSTEM";

    private static final Map<String, Fagsystem> KODER = new LinkedHashMap<>();

    private String kode;

    private String offisiellKode;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    Fagsystem(String kode, String offisiellKode) {
        this.kode = kode;
        this.offisiellKode = offisiellKode;
    }

    public static Fagsystem fraKode(final String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Fagsystem: " + kode);
        }
        return ad;
    }

    public static Map<String, Fagsystem> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @JsonValue
    @Override
    public String getKode() {
        return kode;
    }

    public String getOffisiellKode() {
        return offisiellKode;
    }

    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @Override
    public String getNavn() {
        return null;
    }
}
