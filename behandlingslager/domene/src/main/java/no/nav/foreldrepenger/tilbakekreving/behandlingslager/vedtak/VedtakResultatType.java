package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

public enum VedtakResultatType implements Kodeverdi { //Kun brukes for å sende data til frontend

    FULL_TILBAKEBETALING("FULL_TILBAKEBETALING", "Tilbakebetaling"),
    DELVIS_TILBAKEBETALING("DELVIS_TILBAKEBETALING", "Delvis tilbakebetaling"),
    INGEN_TILBAKEBETALING("INGEN_TILBAKEBETALING", "Ingen tilbakebetaling");

    private String kode;
    private String navn;

    public static final String KODEVERK = "VEDTAK_RESULTAT_TYPE";
    private static final Map<String, VedtakResultatType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    VedtakResultatType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static VedtakResultatType fraKode(final String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent VedtakResultatType: " + kode);
        }
        return ad;
    }

    public static Map<String, VedtakResultatType> kodeMap() {
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
    public static class KodeverdiConverter implements AttributeConverter<VedtakResultatType, String> {
        @Override
        public String convertToDatabaseColumn(VedtakResultatType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public VedtakResultatType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
