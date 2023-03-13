package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

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
public enum AksjonspunktType implements Kodeverdi {

    MANUELL("MANU", "Manuell"),
    AUTOPUNKT("AUTO", "Autopunkt"),
    OVERSTYRING("OVST", "Overstyring"),
    SAKSBEHANDLEROVERSTYRING("SAOV", "Saksbehandleroverstyring"),
    UDEFINERT("-", "Ikke Definert");

    public static final String KODEVERK = "AKSJONSPUNKT_TYPE";
    private static final Map<String, AksjonspunktType> KODER = new LinkedHashMap<>();

    private String kode;
    private String navn;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    AksjonspunktType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AksjonspunktType fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        var kode = TempAvledeKode.getVerdi(AksjonspunktType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent AksjonspunktType: " + kode);
        }
        return ad;
    }

    public static Map<String, AksjonspunktType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public boolean erAutopunkt() {
        return Objects.equals(this, AUTOPUNKT);
    }

    public boolean erOverstyringpunkt() {
        return Objects.equals(this, OVERSTYRING) || Objects.equals(this, SAKSBEHANDLEROVERSTYRING);
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
    public static class KodeverdiConverter implements AttributeConverter<AksjonspunktType, String> {
        @Override
        public String convertToDatabaseColumn(AksjonspunktType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public AksjonspunktType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
