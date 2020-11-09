package no.nav.foreldrepenger.tilbakekreving.økonomixml;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.TempAvledeKode;

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

    public static MeldingType fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(MeldingType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent MeldingType: " + kode);
        }
        return ad;
    }

    public static Map<String, MeldingType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getOffisiellKode() {
        return getKode();
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


