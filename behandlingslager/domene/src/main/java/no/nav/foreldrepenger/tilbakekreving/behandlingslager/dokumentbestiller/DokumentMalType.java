package no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

public enum DokumentMalType implements Kodeverdi {

    INNHENT_DOK("INNHEN", "Innhent dokumentasjon"),
    FRITEKST_DOK("FRITKS", "Fritekstbrev"),
    VARSEL_DOK("VARS", "Varsel om tilbakekreving"),
    KORRIGERT_VARSEL_DOK("KORRIGVARS", "Korrigert varsel om tilbakebetaling");

    public static final String KODEVERK = "DOKUMENT_MAL_TYPE";
    private static final Map<String, DokumentMalType> KODER = new LinkedHashMap<>();

    private String kode;
    private String navn;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    DokumentMalType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static DokumentMalType fraKode(final String kode) {
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
