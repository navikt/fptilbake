package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.TempAvledeKode;

public enum NavOppfulgt implements Kodeverdi {

    NAV_KAN_IKKE_LASTES("NAV_ULASTBAR"),
    HAR_IKKE_FULGT_OPP("HAR_IKKE_FULGT_OPP"),
    HAR_BENYTTET_FEIL("HAR_BENYTTET_FEIL"),
    HAR_IKKE_SJEKKET("HAR_IKKE_SJEKKET"),
    BEREGNINGS_FEIL("BEREGNINGS_FEIL"),
    HAR_UTFØRT_FEIL("HAR_UTFOERT_FEIL"),
    HAR_SENDT_TIL_FEIL_MOTTAKER("HAR_SENDT_TIL_FEIL_MOTTAKER"),

    UDEFINERT("-");

    public static final String KODEVERK = "NAV_OPPFULGT";
    private static final Map<String, NavOppfulgt> KODER = new LinkedHashMap<>();

    private String kode;

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    NavOppfulgt(String kode) {
        this.kode = kode;
    }

    public static NavOppfulgt fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(NavOppfulgt.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent NavOppfulgt: " + kode);
        }
        return ad;
    }

    public static Map<String, NavOppfulgt> kodeMap() {
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
    public static class KodeverdiConverter implements AttributeConverter<NavOppfulgt, String> {
        @Override
        public String convertToDatabaseColumn(NavOppfulgt attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public NavOppfulgt convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
