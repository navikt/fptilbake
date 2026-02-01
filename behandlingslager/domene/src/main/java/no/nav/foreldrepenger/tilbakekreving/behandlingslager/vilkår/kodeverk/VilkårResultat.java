package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

public enum VilkårResultat implements Kodeverdi {

    FORSTO_BURDE_FORSTÅTT("FORSTO_BURDE_FORSTAATT", "Ja, mottaker forsto eller burde forstått at utbetalingen skyldtes en feil (første ledd, første punkt)"),
    MANGELFULLE_OPPLYSNINGER_FRA_BRUKER("MANGELFULL_OPPLYSNING", "Ja, mottaker har forårsaket feilutbetalingen ved forsett eller uaktsomt gitt mangelfulle opplysninger (første ledd, andre punkt)"),
    FEIL_OPPLYSNINGER_FRA_BRUKER("FEIL_OPPLYSNINGER", "Ja, mottaker har forårsaket feilutbetalingen ved forsett eller uaktsomt gitt feilaktige opplysninger (første ledd, andre punkt)"),
    GOD_TRO("GOD_TRO", "Nei, mottaker har mottatt beløpet i god tro (første ledd)"),
    UDEFINERT(STANDARDKODE_UDEFINERT, "Ikke Definert");

    @JsonValue
    private final String kode;
    private final String navn;

    public static final String KODEVERK = "VILKAAR_RESULTAT";
    private static Map<String, VilkårResultat> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    VilkårResultat(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static VilkårResultat fraKode(final String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent VilkårResultat: " + kode);
        }
        return ad;
    }

    public static Map<String, VilkårResultat> kodeMap() {
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
        return navn;
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<VilkårResultat, String> {
        @Override
        public String convertToDatabaseColumn(VilkårResultat attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public VilkårResultat convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }

}
