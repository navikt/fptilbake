package no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.TempAvledeKode;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum FagOmrådeKode implements Kodeverdi {

    FORELDREPENGER("FP","Foreldrepenger"),
    FORELDREPENGER_ARBEIDSGIVER("FPREF","Foreldrepenger refusjon"),
    SYKEPENGER("SP","Sykepenger"),
    SYKEPENGER_ARBEIDSGIVER("SPREF","Sykepenger refusjon"),
    PLEIEPENGER_V1("OOP","Pleiepenger sykt barn"),
    PLEIEPENGER_V1_ARBEIDSGIVER("OOPREF","Pleiepenger sykt barn"),
    ENGANGSSTØNAD("REFUTG","Engangsstønad"),
    SVANGERSKAPSPENGER("SVP","Svangerskapspenger"),
    SVANGERSKAPSPENGER_ARBEIDSGIVER("SVPREF","Svangerskapspenger refusjon til arbeidsgiver"),

    //K9
    PLEIEPENGER_SYKT_BARN("PB","Pleiepenger sykt barn"),
    PLEIEPENGER_SYKT_BARN_ARBEIDSGIVER("PBREF","Pleiepenger sykt barn"),
    PLEIEPENGER_NÆRSTÅENDE("PN","Pleiepenger nærstående"),
    PLEIEPENGER_NÆRSTÅENDE_ARBEIDSGIVER("PNREF","Pleiepenger nærstående"),
    OMSORGSPENGER("OM","Omsorgspenger"),
    OMSORGSPENGER_ARBEIDSGIVER("OMREF","Omsorgspenger"),
    OPPLÆRINGSPENGER("OPP","Opplæringspenger"),
    OPPLÆRINGSPENGER_ARBEIDSGIVER("OPPREF","Opplæringspenger"),
    FRISINN("FRISINN","FRIlansere og Selstendig næringsdrivendes INNtektskompensasjon"),

    UDEFINERT("-","udefinert");

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

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static FagOmrådeKode fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(FagOmrådeKode.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent FagOmrådeKode: " + kode);
        }
        return ad;
    }

    public static Map<String, FagOmrådeKode> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getOffisiellKode() {
        return getKode();
    }

    @JsonProperty
    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @JsonProperty
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
