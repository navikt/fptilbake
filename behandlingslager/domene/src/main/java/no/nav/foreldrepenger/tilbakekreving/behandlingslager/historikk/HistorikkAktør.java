package no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

public enum HistorikkAktør implements Kodeverdi {

    BESLUTTER("BESL", "Beslutter"),
    SAKSBEHANDLER("SBH", "Saksbehandler"),
    SØKER("SOKER", "Søker"),
    ARBEIDSGIVER("ARBEIDSGIVER", "Arbeidsgiver"),
    VEDTAKSLØSNINGEN("VL", "Vedtaksløsningen"),
    UDEFINERT("-", "Ikke definert");

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

    public static HistorikkAktør fraKode(final String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent HistorikkAktør: " + kode);
        }
        return ad;
    }

    public static Map<String, HistorikkAktør> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @JsonValue
    @Override
    public java.lang.String getKode() {
        return kode;
    }

    @Override
    public java.lang.String getKodeverk() {
        return KODEVERK;
    }

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
