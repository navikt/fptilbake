package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt;

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
public enum VurderÅrsak implements Kodeverdi {

    FEIL_FAKTA("FEIL_FAKTA"),
    FEIL_LOV("FEIL_LOV"),
    FEIL_REGEL("FEIL_REGEL"),
    ANNET("ANNET"),
    UDEFINERT("-"); //$NON-NLS-1$

    public static final String KODEVERK = "VURDER_AARSAK";
    private static final Map<String, VurderÅrsak> KODER = new LinkedHashMap<>();

    private String kode;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    VurderÅrsak(String kode) {
        this.kode = kode;
    }

    @JsonCreator
    public static VurderÅrsak fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent VurderÅrsak: " + kode);
        }
        return ad;
    }

    public static Map<String, VurderÅrsak> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<VurderÅrsak, String> {
        @Override
        public String convertToDatabaseColumn(VurderÅrsak attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public VurderÅrsak convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
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
        return null;
    }
}
