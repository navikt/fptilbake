package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.TempAvledeKode;

public enum IverksettingStatus implements Kodeverdi {

    IKKE_IVERKSATT("IKKE_IVERKSATT"),
    UNDER_IVERKSETTING("UNDER_IVERKSETTING"),
    IVERKSATT("IVERKSATT"),
    UDEFINERT("-");

    public static final String KODEVERK = "IVERKSETTING_STATUS";
    private static final Map<String, IverksettingStatus> KODER = new LinkedHashMap<>();

    private String kode;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    IverksettingStatus(String kode) {
        this.kode = kode;
    }

    public static IverksettingStatus fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(IverksettingStatus.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent IverksettingStatus: " + kode);
        }
        return ad;
    }

    public static Map<String, IverksettingStatus> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

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
    public static class KodeverdiConverter implements AttributeConverter<IverksettingStatus, String> {
        @Override
        public String convertToDatabaseColumn(IverksettingStatus attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public IverksettingStatus convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
