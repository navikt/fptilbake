package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

public enum VedtaksbrevFritekstType implements Kodeverdi {

    FAKTA_AVSNITT("FAKTA_AVSNITT"),
    FORELDELSE_AVSNITT("FORELDELSE_AVSNITT"),
    VILKAAR_AVSNITT("VILKAAR_AVSNITT"),
    SAERLIGE_GRUNNER_AVSNITT("SAERLIGE_GRUNNER_AVSNITT"),
    SAERLIGE_GRUNNER_ANNET_AVSNITT("SAERLIGE_GRUNNER_ANNET_AVSNITT");

    public static final String KODEVERK = "FRITEKST_TYPE";
    private static final Map<String, VedtaksbrevFritekstType> KODER = new LinkedHashMap<>();

    private String kode;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    VedtaksbrevFritekstType(String kode) {
        this.kode = kode;
    }

    public static VedtaksbrevFritekstType fraKode(final String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent VedtaksbrevFritekstType: " + kode);
        }
        return ad;
    }

    public static Map<String, VedtaksbrevFritekstType> kodeMap() {
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
    public static class KodeverdiConverter implements AttributeConverter<VedtaksbrevFritekstType, String> {
        @Override
        public String convertToDatabaseColumn(VedtaksbrevFritekstType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public VedtaksbrevFritekstType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
