package no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk;

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
public enum HistorikkAktør implements Kodeverdi {

    BESLUTTER("BESL","Beslutter"),
    SAKSBEHANDLER("SBH","Saksbehandler"),
    SØKER("SOKER","Søker"),
    ARBEIDSGIVER("ARBEIDSGIVER","Arbeidsgiver"),
    VEDTAKSLØSNINGEN("VL","Vedtaksløsningen"),
    UDEFINERT("-","Ikke definert");

    private String kode;
    private String navn;

    public static final String KODEVERK = "HISTORIKK_AKTOER";
    private static Map<String, HistorikkAktør> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private HistorikkAktør(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static HistorikkAktør fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(HistorikkAktør.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent HistorikkAktør: " + kode);
        }
        return ad;
    }

    public static Map<String, HistorikkAktør> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @JsonProperty
    @Override
    public java.lang.String getKode() {
        return kode;
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
    public static class KodeverdiConverter implements AttributeConverter<HistorikkAktør, String> {
        @Override
        public String convertToDatabaseColumn(HistorikkAktør attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public HistorikkAktør convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
