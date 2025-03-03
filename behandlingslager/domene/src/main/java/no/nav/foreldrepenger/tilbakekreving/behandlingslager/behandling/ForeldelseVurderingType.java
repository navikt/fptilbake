package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

public enum ForeldelseVurderingType implements Kodeverdi {

    IKKE_VURDERT("IKKE_VURDERT", "Perioden er ikke vurdert"),
    FORELDET("FORELDET", "Perioden er foreldet"),
    IKKE_FORELDET("IKKE_FORELDET", "Perioden er ikke foreldet"),
    TILLEGGSFRIST("TILLEGGSFRIST", "Perioden er ikke foreldet, regel om tilleggsfrist (10 år) benyttes"),
    UDEFINERT("-", "Ikke Definert");

    private String kode;
    private String navn;

    public static final String KODEVERK = "FORELDELSE_VURDERING";
    private static final Map<String, ForeldelseVurderingType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    ForeldelseVurderingType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static ForeldelseVurderingType fraKode(final String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent ForeldelseVurderingType: " + kode);
        }
        return ad;
    }

    public static Map<String, ForeldelseVurderingType> kodeMap() {
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
    public static class KodeverdiConverter implements AttributeConverter<ForeldelseVurderingType, String> {
        @Override
        public String convertToDatabaseColumn(ForeldelseVurderingType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public ForeldelseVurderingType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
