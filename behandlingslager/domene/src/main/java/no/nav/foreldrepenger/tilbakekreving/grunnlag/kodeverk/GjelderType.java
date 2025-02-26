package no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

public enum GjelderType implements Kodeverdi {


    PERSON("PERSON", "Person"),
    ORGANISASJON("ORGANISASJON", "Organisasjon"),
    SAMHANDLER("SAMHANDLER", "Samhandler"),
    APPBRUKER("APPBRUKER", "App bruker");

    public static final String KODEVERK = "GJELDER_TYPE";

    private static final Map<String, GjelderType> KODER = new LinkedHashMap<>();

    @JsonValue
    private String kode;

    private String navn;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private GjelderType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static GjelderType fraKode(final String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent GjelderType: " + kode);
        }
        return ad;
    }

    public static Map<String, GjelderType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

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
    public static class KodeverdiConverter implements AttributeConverter<GjelderType, String> {
        @Override
        public String convertToDatabaseColumn(GjelderType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public GjelderType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
