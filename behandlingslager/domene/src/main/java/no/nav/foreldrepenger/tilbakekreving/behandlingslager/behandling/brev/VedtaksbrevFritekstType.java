package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.TempAvledeKode;

public enum VedtaksbrevFritekstType implements Kodeverdi {

    FAKTA_AVSNITT("FAKTA_AVSNITT"),
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

    public static VedtaksbrevFritekstType fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(VedtaksbrevFritekstType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent VedtaksbrevFritekstType: " + kode);
        }
        return ad;
    }

    public static Map<String, VedtaksbrevFritekstType> kodeMap() {
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
