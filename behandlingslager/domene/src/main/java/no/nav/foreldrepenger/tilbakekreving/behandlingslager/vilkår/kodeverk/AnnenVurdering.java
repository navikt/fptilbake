package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AnnenVurdering implements Vurdering {

    GOD_TRO("GOD_TRO", "Handlet i god tro"),
    FORELDET("FORELDET", "Foreldet");

    private String kode;
    private String navn;

    public static final String KODEVERK = "VURDERING";
    private static final Map<String, AnnenVurdering> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private AnnenVurdering(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static AnnenVurdering fraKode(final String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent AnnenVurdering: " + kode);
        }
        return ad;
    }

    public static Map<String, AnnenVurdering> kodeMap() {
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
