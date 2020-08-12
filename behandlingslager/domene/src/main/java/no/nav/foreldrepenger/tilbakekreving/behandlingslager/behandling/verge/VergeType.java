package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge;

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

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum VergeType implements Kodeverdi {

    BARN("BARN","Verge for barn under 18 år"),
    FBARN("FBARN","Verge for foreldreløst barn under 18 år"),
    VOKSEN("VOKSEN","Verge for voksen"),
    ADVOKAT("ADVOKAT","Advokat/advokatfullmektig"),
    ANNEN_F("ANNEN_F","Annen fullmektig"),
    UDEFINERT("-","UDefinert");

    private String kode;
    private String navn;

    public static final String KODEVERK = "VERGE_TYPE";
    private static final Map<String, VergeType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private VergeType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator
    public static VergeType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent VergeType: " + kode);
        }
        return ad;
    }

    public static Map<String, VergeType> kodeMap() {
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

    @JsonProperty
    @Override
    public String getNavn() {
        return navn;
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<VergeType, String> {
        @Override
        public String convertToDatabaseColumn(VergeType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public VergeType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
