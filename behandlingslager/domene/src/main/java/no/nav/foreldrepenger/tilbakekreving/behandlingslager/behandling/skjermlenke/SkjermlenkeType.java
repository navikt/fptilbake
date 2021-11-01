package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke;

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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.TempAvledeKode;


@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum SkjermlenkeType implements Kodeverdi {

    FAKTA_OM_FEILUTBETALING("FAKTA_OM_FEILUTBETALING", "Fakta om feilutbetaling"),
    TILBAKEKREVING("TILBAKEKREVING", "Tilbakekreving"),
    FORELDELSE("FORELDELSE", "Foreldelse"),
    VEDTAK("VEDTAK", "Vedtak"),
    FAKTA_OM_VERGE("FAKTA_OM_VERGE", "Fakta om verge/fullmektig"),
    UDEFINERT("-", "Ikke Definert");

    private String kode;
    private String navn;

    public static final String KODEVERK = "SKJERMLENKE_TYPE";
    private static Map<String, SkjermlenkeType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private SkjermlenkeType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SkjermlenkeType fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(SkjermlenkeType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent HistorikkinnslagType: " + kode);
        }
        return ad;
    }


    public static Map<String, SkjermlenkeType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
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
    public static class KodeverdiConverter implements AttributeConverter<SkjermlenkeType, String> {
        @Override
        public String convertToDatabaseColumn(SkjermlenkeType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public SkjermlenkeType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
