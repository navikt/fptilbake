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

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum HistorikkinnslagFeltType implements Kodeverdi {

    AARSAK("AARSAK"),
    BEGRUNNELSE("BEGRUNNELSE"),
    HENDELSE("HENDELSE"),
    RESULTAT("RESULTAT"),
    OPPLYSNINGER("OPPLYSNINGER"),
    ENDRET_FELT("ENDRET_FELT"),
    SKJERMLENKE("SKJERMLENKE"),
    GJELDENDE_FRA("GJELDENDE_FRA"),
    AKSJONSPUNKT_BEGRUNNELSE("AKSJONSPUNKT_BEGRUNNELSE"),
    AKSJONSPUNKT_GODKJENT("AKSJONSPUNKT_GODKJENT"),
    AKSJONSPUNKT_KODE("AKSJONSPUNKT_KODE"),
    AVKLART_SOEKNADSPERIODE("AVKLART_SOEKNADSPERIODE"),
    ANGÅR_TEMA("ANGÅR_TEMA"),

    UDEFINIERT("-");

    public static final String KODEVERK = "HISTORIKKINNSLAG_FELT_TYPE"; //$NON-NLS-1$

    private static final Map<String, HistorikkinnslagFeltType> KODER = new LinkedHashMap<>();

    private String kode;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }


    private HistorikkinnslagFeltType(String kode) {
        this.kode = kode;
    }

    @JsonCreator
    public static HistorikkinnslagFeltType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent HistorikkinnslagFeltType: " + kode);
        }
        return ad;
    }

    public static Map<String, HistorikkinnslagFeltType> kodeMap() {
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
        return null;
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<HistorikkinnslagFeltType, String> {
        @Override
        public String convertToDatabaseColumn(HistorikkinnslagFeltType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public HistorikkinnslagFeltType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
