package no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

public enum KlasseType implements Kodeverdi {

    FEIL("FEIL", "Feilkonto"),
    JUST("JUST", "Justeringskonto"),
    SKAT("SKAT", "Skatt"),
    TREK("TREK", "Trekk"),
    YTEL("YTEL", "Ytelseskonto");

    public static final String KODEVERK = "KLASSE_TYPE";

    private static final Map<String, KlasseType> KODER = new LinkedHashMap<>();

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

    private KlasseType(String kode) {
        this.kode = kode;
    }

    private KlasseType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static KlasseType fraKode(final String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent KlasseType: " + kode);
        }
        return ad;
    }

    public static Map<String, KlasseType> kodeMap() {
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
    public static class KodeverdiConverter implements AttributeConverter<KlasseType, String> {
        @Override
        public String convertToDatabaseColumn(KlasseType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public KlasseType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
