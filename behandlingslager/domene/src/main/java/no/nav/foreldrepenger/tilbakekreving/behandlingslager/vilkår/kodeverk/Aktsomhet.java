package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum Aktsomhet implements Vurdering {

    FORSETT("FORSETT","Forsett"),
    GROVT_UAKTSOM("GROVT_UAKTSOM","Grov uaktsomhet"),
    SIMPEL_UAKTSOM("SIMPEL_UAKTSOM","Simpel uaktsomhet");

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

    @JsonCreator
    public static Aktsomhet fraKode(@JsonProperty("kode") String kode) {
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

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getOffisiellKode() {
        return getKode();
    }

    @JsonProperty
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
