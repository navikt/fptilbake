package no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;


public enum Språkkode implements Kodeverdi {
    NB("NB"),
    NN("NN"),
    EN("EN"),
    UDEFINERT("-");

    public static final Språkkode DEFAULT = Språkkode.NB;
    public static final String KODEVERK = "SPRAAK_KODE";
    private static final Map<String, Språkkode> KODER = new LinkedHashMap<>();

    private String kode;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    Språkkode(String kode) {
        this.kode = kode;
    }

    public static Språkkode fraKode(final String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            return Språkkode.NB;
        }
        return ad;
    }

    public static Map<String, Språkkode> kodeMap() {
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
    public static class KodeverdiConverter implements AttributeConverter<Språkkode, String> {
        @Override
        public String convertToDatabaseColumn(Språkkode attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public Språkkode convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
