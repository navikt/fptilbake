package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

public enum BrevType implements Kodeverdi {

     VARSEL_BREV("VARSEL"),
     VEDTAK_BREV("VEDTAK"),
     HENLEGGELSE_BREV("HENLEGGELSE"),
     INNHENT_DOKUMENTASJONBREV("INNHENT_DOKUMENTASJON"),
     UDEFINERT("-");

    public static final String KODEVERK = "BREV_TYPE";
    private static final Map<String, BrevType> KODER = new LinkedHashMap<>();

    private String kode;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    BrevType(String kode){
        this.kode = kode;
    }

    public static BrevType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent BrevType: " + kode);
        }
        return ad;
    }

    public static Map<String, BrevType> kodeMap() {
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
    public static class KodeverdiConverter implements AttributeConverter<BrevType, String> {
        @Override
        public String convertToDatabaseColumn(BrevType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public BrevType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}

