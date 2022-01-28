package no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.TempAvledeKode;

@JsonFormat(shape = JsonFormat.Shape.STRING)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum KravStatusKode implements Kodeverdi {

    ANNULERT("ANNU", "Kravgrunnlag annullert"),
    ANNULLERT_OMG("ANOM", "Kravgrunnlag annullert ved omg"),
    AVSLUTTET("AVSL", "Avsluttet kravgrunnlag"),
    BEHANDLET("BEHA", "Kravgrunnlag ferdigbehandlet"),
    ENDRET("ENDR", "Endret kravgrunnlag"),
    FEIL("FEIL", "Feil på kravgrunnlag"),
    MANUELL("MANU", "Manuell behandling"),
    NYTT("NY", "Nytt kravgrunnlag"),
    SPERRET("SPER", "Kravgrunnlag sperret");

    public static final String KODEVERK = "KRAV_STATUS_KODE";

    private static Map<String, KravStatusKode> KODER = new LinkedHashMap<>();

    @JsonValue
    private String kode;

    @JsonIgnore
    private String navn;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private KravStatusKode(String kode) {
        this.kode = kode;
    }

    private KravStatusKode(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static KravStatusKode fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(KravStatusKode.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent KravStatusKode: " + kode);
        }
        return ad;
    }

    public static Map<String, KravStatusKode> kodeMap() {
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
    public static class KodeverdiConverter implements AttributeConverter<KravStatusKode, String> {
        @Override
        public String convertToDatabaseColumn(KravStatusKode attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public KravStatusKode convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
