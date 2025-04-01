package no.nav.foreldrepenger.tilbakekreving.økonomixml;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

public enum MeldingType implements Kodeverdi {

    VEDTAK("VEDTAK"),
    ANNULERE_GRUNNLAG("ANNULERE_GRUNNLAG");

    public static final String KODEVERK = "MELDING_TYPE";
    private static final Map<String, MeldingType> KODER = new LinkedHashMap<>();

    private String kode;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    MeldingType(String kode) {
        this.kode = kode;
    }

    public static MeldingType fraKode(final String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent MeldingType: " + kode);
        }
        return ad;
    }

    public static Map<String, MeldingType> kodeMap() {
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
        return null;
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<MeldingType, String> {
        @Override
        public String convertToDatabaseColumn(MeldingType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public MeldingType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}


