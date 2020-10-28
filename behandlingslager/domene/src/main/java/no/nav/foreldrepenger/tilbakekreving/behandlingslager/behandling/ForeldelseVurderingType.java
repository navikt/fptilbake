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
public enum ForeldelseVurderingType implements Kodeverdi {

    IKKE_VURDERT("IKKE_VURDERT","Perioden er ikke vurdert"),
    FORELDET("FORELDET","Perioden er foreldet"),
    IKKE_FORELDET("IKKE_FORELDET","Perioden er ikke foreldet"),
    TILLEGGSFRIST("TILLEGGSFRIST","Perioden er ikke foreldet, regel om tilleggsfrist (10 år) benyttes"),
    UDEFINERT("-","Ikke Definert");

    private String kode;
    private String navn;

    public static final String KODEVERK = "FORELDELSE_VURDERING";
    private static final Map<String, ForeldelseVurderingType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    ForeldelseVurderingType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator
    public static ForeldelseVurderingType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent ForeldelseVurderingType: " + kode);
        }
        return ad;
    }

    public static Map<String, ForeldelseVurderingType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @JsonProperty
    @Override
    public java.lang.String getKode() {
        return kode;
    }

    @Override
    public java.lang.String getOffisiellKode() {
        return getKode();
    }

    @JsonProperty
    @Override
    public java.lang.String getKodeverk() {
        return KODEVERK;
    }

    @JsonProperty
    @Override
    public java.lang.String getNavn() {
        return navn;
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<ForeldelseVurderingType, String> {
        @Override
        public String convertToDatabaseColumn(ForeldelseVurderingType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public ForeldelseVurderingType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
