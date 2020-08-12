package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

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

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum HendelseType implements Kodeverdi {

    MEDLEMSKAP_TYPE("MEDLEMSKAP", "§14-2 Medlemskap", 10),
    ØKONOMI_FEIL("OKONOMI_FEIL", "Feil i økonomi", 500),
    FP_OPPTJENING_TYPE("OPPTJENING_TYPE", "§14-6 Opptjening", 20),
    FP_BEREGNING_TYPE("BEREGNING_TYPE", "§14-7 Beregning", 30),
    FP_STONADSPERIODEN_TYPE("STONADSPERIODEN_TYPE", "§14-9 Stønadsperioden", 40),
    FP_UTTAK_GENERELT_TYPE("UTTAK_GENERELT_TYPE", "§14-10 Generelt om uttak", 50),
    FP_UTTAK_UTSETTELSE_TYPE("UTTAK_UTSETTELSE_TYPE", "§14-11 Utsettelse av uttak", 60),
    FP_UTTAK_KVOTENE_TYPE("UTTAK_KVOTENE_TYPE", "§14-12 Uttak av kvotene", 70),
    FP_VILKAAR_GENERELLE_TYPE("VILKAAR_GENERELLE_TYPE", "§14-13 Generelle vilkår for fars uttak", 80),
    FP_KUN_RETT_TYPE("KUN_RETT_TYPE", "§14-14 Kun far/medmor rett", 90),
    FP_UTTAK_ALENEOMSORG_TYPE("UTTAK_ALENEOMSORG_TYPE", "§14-15 Uttak ved aleneomsorg/samlivsbrudd", 100),
    FP_UTTAK_GRADERT_TYPE("UTTAK_GRADERT_TYPE", "§14-16 Gradert uttak", 110),
    FP_ANNET_HENDELSE_TYPE("FP_ANNET_HENDELSE_TYPE", "Annet", 999),

    ES_FODSELSVILKAARET_TYPE("ES_FODSELSVILKAARET_TYPE", "§14-17 1. ledd Fødselsvilkåret", 30),
    ES_ADOPSJONSVILKAARET_TYPE("ES_ADOPSJONSVILKAARET_TYPE", "§14-17 1. ledd Adopsjonsvilkåret", 20),
    ES_FORELDREANSVAR_TYPE("ES_FORELDREANSVAR_TYPE", "§14-17 2. ledd Foreldreansvar ", 40),
    ES_OMSORGSVILKAAR_TYPE("ES_OMSORGSVILKAAR_TYPE", "§14-17 3. ledd Omsorgsvilkår ved mors død", 50),
    ES_FORELDREANSVAR_FAR_TYPE("ES_FORELDREANSVAR_FAR_TYPE", "§14-17 4. ledd Foreldreansvar far", 60),
    ES_RETT_PAA_FORELDREPENGER_TYPE("ES_RETT_PAA_FORELDREPENGER_TYPE", "Rett på foreldrepenger etter klage", 70),
    ES_FEIL_UTBETALING_TYPE("ES_FEIL_UTBETALING_TYPE", "Feil i utbetaling", 500),
    ES_ANNET_TYPE("ES_ANNET_TYPE", "Annet", 999),

    SVP_FAKTA_TYPE("SVP_FAKTA_TYPE", "§14-4 Fakta om svangerskap", 20),
    SVP_ARBEIDSGIVERS_FORHOLD_TYPE("SVP_ARBEIDSGIVERS_FORHOLD_TYPE", "§14-4 1. ledd Arbeidsgivers forhold", 30),
    SVP_OPPTJENING_TYPE("SVP_OPPTJENING_TYPE", "§14-4 3. ledd Opptjening/ inntekt", 50),
    SVP_BEREGNING_TYPE("SVP_BEREGNING_TYPE", "§14-4 5. ledd Beregning ", 60),
    SVP_UTTAK_TYPE("SVP_UTTAK_TYPE", "§14-4 Uttak", 70),
    SVP_OPPHØR("OPPHØR", "Opphør", 80),
    SVP_ANNET_TYPE("SVP_ANNET_TYPE", "Annet", 999);

    public static final String KODEVERK = "HENDELSE_TYPE";

    private static final Map<String, HendelseType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private String kode;
    private String navn;
    private int sortering;

    HendelseType(String kode, String navn, int sortering) {
        this.kode = kode;
        this.navn = navn;
        this.sortering = sortering;
    }

    @JsonCreator
    public static HendelseType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent HendelseType: " + kode);
        }
        return ad;
    }

    public static Map<String, HendelseType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getOffisiellKode() {
        return null;
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

    public int getSortering() {
        return sortering;
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<HendelseType, String> {
        @Override
        public String convertToDatabaseColumn(HendelseType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public HendelseType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }
}
