package no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

public enum FagOmrådeKode implements Kodeverdi {

    FORELDREPENGER("FP", "Foreldrepenger"),
    FORELDREPENGER_ARBEIDSGIVER("FPREF", "Foreldrepenger refusjon"),
    SYKEPENGER("SP", "Sykepenger"),
    SYKEPENGER_ARBEIDSGIVER("SPREF", "Sykepenger refusjon"),
    PLEIEPENGER_V1("OOP", "Pleiepenger sykt barn"),
    PLEIEPENGER_V1_ARBEIDSGIVER("OOPREF", PLEIEPENGER_V1.navn),
    ENGANGSSTØNAD("REFUTG", "Engangsstønad"),
    SVANGERSKAPSPENGER("SVP", "Svangerskapspenger"),
    SVANGERSKAPSPENGER_ARBEIDSGIVER("SVPREF", "Svangerskapspenger refusjon til arbeidsgiver"),

    //K9
    PLEIEPENGER_SYKT_BARN("PB", PLEIEPENGER_V1.navn),
    PLEIEPENGER_SYKT_BARN_ARBEIDSGIVER("PBREF", PLEIEPENGER_V1.navn),
    PLEIEPENGER_NÆRSTÅENDE("PN", "Pleiepenger nærstående"),
    PLEIEPENGER_NÆRSTÅENDE_ARBEIDSGIVER("PNREF", "Pleiepenger nærstående"),
    OMSORGSPENGER("OM", "Omsorgspenger"),
    OMSORGSPENGER_ARBEIDSGIVER("OMREF", "Omsorgspenger"),
    OPPLÆRINGSPENGER("OPP", "Opplæringspenger"),
    OPPLÆRINGSPENGER_ARBEIDSGIVER("OPPREF", "Opplæringspenger"),
    FRISINN("FRISINN", "FRIlansere og Selstendig næringsdrivendes INNtektskompensasjon"),

    UDEFINERT("-", "udefinert");

    private String kode;
    private String navn;

    public static final String KODEVERK = "FAG_OMRAADE_KODE";
    private static final Map<String, FagOmrådeKode> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private FagOmrådeKode(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static FagOmrådeKode fraKode(final String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent FagOmrådeKode: " + kode);
        }
        return ad;
    }

    public static Map<String, FagOmrådeKode> kodeMap() {
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
    public static class KodeverdiConverter implements AttributeConverter<FagOmrådeKode, String> {
        @Override
        public String convertToDatabaseColumn(FagOmrådeKode attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public FagOmrådeKode convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
