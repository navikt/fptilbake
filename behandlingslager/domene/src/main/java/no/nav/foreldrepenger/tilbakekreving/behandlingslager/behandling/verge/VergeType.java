package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

public enum VergeType implements Kodeverdi {

    BARN("BARN", "Verge for barn under 18 år"),
    FBARN("FBARN", "Verge for foreldreløst barn under 18 år"),
    VOKSEN("VOKSEN", "Verge for voksen"),
    ADVOKAT("ADVOKAT", "Advokat/advokatfullmektig"),
    ANNEN_F("ANNEN_F", "Annen fullmektig"),
    UDEFINERT("-", "UDefinert");

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

    public static VergeType fraKode(final String kode) {
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

    @JsonValue
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
