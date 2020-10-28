package no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

public enum DokumentMalType implements Kodeverdi {

    INNHENT_DOK("INNHEN"),
    FRITEKST_DOK("FRITKS"),
    VARSEL_DOK("VARS"),
    KORRIGERT_VARSEL_DOK("KORRIGVARS");

    public static final String KODEVERK = "DOKUMENT_MAL_TYPE";
    private static final Map<String, DokumentMalType> KODER = new LinkedHashMap<>();

    private String kode;
    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    DokumentMalType(String kode) {
        this.kode = kode;
    }

    public static DokumentMalType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent DokumentMalType: " + kode);
        }
        return ad;
    }

    public static Map<String, DokumentMalType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getOffisiellKode() {
        return getKode();
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
