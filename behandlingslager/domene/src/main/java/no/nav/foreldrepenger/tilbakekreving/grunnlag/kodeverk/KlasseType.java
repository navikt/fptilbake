package no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

@JsonFormat(shape = JsonFormat.Shape.STRING)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public enum KlasseType implements Kodeverdi {

    FEIL("FEIL"),
    JUST("JUST"),
    SKAT("SKAT"),
    TREK("TREK"),
    YTEL("YTEL");

    public static final String KODEVERK = "KLASSE_TYPE";

    private static final Map<String, KlasseType> KODER = new LinkedHashMap<>();

    @JsonValue
    private String kode;

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

    @JsonCreator
    public static KlasseType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent KlasseKode: " + kode);
        }
        return ad;
    }

    public static Map<String, KlasseType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @Override
    public java.lang.String getKode() {
        return kode;
    }

    @Override
    public java.lang.String getOffisiellKode() {
        return getKode();
    }

    @Override
    public java.lang.String getKodeverk() {
        return KODEVERK;
    }

    @Override
    public java.lang.String getNavn() {
        return null;
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
