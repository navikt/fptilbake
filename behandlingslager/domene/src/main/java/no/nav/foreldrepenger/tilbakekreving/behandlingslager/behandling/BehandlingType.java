package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

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
public enum BehandlingType implements Kodeverdi {

    TILBAKEKREVING("BT-007", "Tilbakekreving"),
    REVURDERING_TILBAKEKREVING("BT-009", "Tilbakekreving revurdering"),
    UDEFINERT("-", "Ikke definert");

    public static final String KODEVERK = "BEHANDLING_TYPE";
    private static final Map<String, BehandlingType> TILGJENGELIGE = Map.of(
        REVURDERING_TILBAKEKREVING.getKode(), REVURDERING_TILBAKEKREVING,
        TILBAKEKREVING.getKode(), TILBAKEKREVING
    );
    private static final Map<String, BehandlingType> KODER = new LinkedHashMap<>();

    private String kode;
    private String navn;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    BehandlingType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }


    @JsonCreator
    public static BehandlingType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent BehandlingType: " + kode);
        }
        return ad;
    }

    public static Map<String, BehandlingType> kodeMap() {
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
    public static class KodeverdiConverter implements AttributeConverter<BehandlingType, String> {
        @Override
        public String convertToDatabaseColumn(BehandlingType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public BehandlingType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
