package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Aktsomhet implements Vurdering {

    FORSETT("FORSETT", "Forsett"),
    GROVT_UAKTSOM("GROVT_UAKTSOM", "Grov uaktsomhet"),
    SIMPEL_UAKTSOM("SIMPEL_UAKTSOM", "Simpel uaktsomhet");

    /* TODO/FIXME burde egentlig ha egne koder her for bruke sammen med VilkårResultat.FORSTO_BURDE_FORSTÅTT
     enum-verdi FORSTOD
     enum-verdi MÅ_HA_FORSTÅTT
     enum-verdi BURDE_HA_FORSTÅTT
     */

    private String kode;
    private String navn;

    public static final String KODEVERK = "AKTSOMHET";
    private static final Map<String, Aktsomhet> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private Aktsomhet(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static Aktsomhet fraKode(final String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Aktsomhet: " + kode);
        }
        return ad;
    }

    public static Map<String, Aktsomhet> kodeMap() {
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

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<Aktsomhet, String> {
        @Override
        public String convertToDatabaseColumn(Aktsomhet attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public Aktsomhet convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
