package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum HendelseType implements Kodeverdi {

    MEDLEMSKAP_TYPE("MEDLEMSKAP", "§14-2 Medlemskap", FagsakYtelseType.FORELDREPENGER),
    ØKONOMI_FEIL("OKONOMI_FEIL", "Feil i økonomi", FagsakYtelseType.FORELDREPENGER),
    FP_OPPTJENING_TYPE("OPPTJENING_TYPE", "§14-6 Opptjening", FagsakYtelseType.FORELDREPENGER),
    FP_BEREGNING_TYPE("BEREGNING_TYPE", "§14-7 Beregning", FagsakYtelseType.FORELDREPENGER),
    FP_STONADSPERIODEN_TYPE("STONADSPERIODEN_TYPE", "§14-9 Stønadsperioden", FagsakYtelseType.FORELDREPENGER),
    FP_UTTAK_GENERELT_TYPE("UTTAK_GENERELT_TYPE", "§14-10 Generelt om uttak", FagsakYtelseType.FORELDREPENGER),
    FP_UTTAK_UTSETTELSE_TYPE("UTTAK_UTSETTELSE_TYPE", "§14-11 Utsettelse av uttak", FagsakYtelseType.FORELDREPENGER),
    FP_UTTAK_KVOTENE_TYPE("UTTAK_KVOTENE_TYPE", "§14-12 Uttak av kvotene", FagsakYtelseType.FORELDREPENGER),
    FP_VILKAAR_GENERELLE_TYPE("VILKAAR_GENERELLE_TYPE", "§14-13 Generelle vilkår for fars uttak", FagsakYtelseType.FORELDREPENGER),
    FP_KUN_RETT_TYPE("KUN_RETT_TYPE", "§14-14 Kun far/medmor rett", FagsakYtelseType.FORELDREPENGER),
    FP_UTTAK_ALENEOMSORG_TYPE("UTTAK_ALENEOMSORG_TYPE", "§14-15 Uttak ved aleneomsorg/samlivsbrudd", FagsakYtelseType.FORELDREPENGER),
    FP_UTTAK_GRADERT_TYPE("UTTAK_GRADERT_TYPE", "§14-16 Gradert uttak", FagsakYtelseType.FORELDREPENGER),
    FP_ANNET_HENDELSE_TYPE("FP_ANNET_HENDELSE_TYPE", "Annet", FagsakYtelseType.FORELDREPENGER),

    ES_FODSELSVILKAARET_TYPE("ES_FODSELSVILKAARET_TYPE", "§14-17 1. ledd Fødselsvilkåret", FagsakYtelseType.ENGANGSTØNAD),
    ES_ADOPSJONSVILKAARET_TYPE("ES_ADOPSJONSVILKAARET_TYPE", "§14-17 1. ledd Adopsjonsvilkåret", FagsakYtelseType.ENGANGSTØNAD),
    ES_FORELDREANSVAR_TYPE("ES_FORELDREANSVAR_TYPE", "§14-17 2. ledd Foreldreansvar", FagsakYtelseType.ENGANGSTØNAD),
    ES_OMSORGSVILKAAR_TYPE("ES_OMSORGSVILKAAR_TYPE", "§14-17 3. ledd Omsorgsvilkår ved mors død", FagsakYtelseType.ENGANGSTØNAD),
    ES_FORELDREANSVAR_FAR_TYPE("ES_FORELDREANSVAR_FAR_TYPE", "§14-17 4. ledd Foreldreansvar far", FagsakYtelseType.ENGANGSTØNAD),
    ES_RETT_PAA_FORELDREPENGER_TYPE("ES_RETT_PAA_FORELDREPENGER_TYPE", "Rett på foreldrepenger etter klage", FagsakYtelseType.ENGANGSTØNAD),
    ES_FEIL_UTBETALING_TYPE("ES_FEIL_UTBETALING_TYPE", "Feil i utbetaling", FagsakYtelseType.ENGANGSTØNAD),
    ES_ANNET_TYPE("ES_ANNET_TYPE", "Annet", FagsakYtelseType.ENGANGSTØNAD),

    SVP_FAKTA_TYPE("SVP_FAKTA_TYPE", "§14-4 Fakta om svangerskap", FagsakYtelseType.SVANGERSKAPSPENGER),
    SVP_ARBEIDSGIVERS_FORHOLD_TYPE("SVP_ARBEIDSGIVERS_FORHOLD_TYPE", "§14-4 1. ledd Arbeidsgivers forhold", FagsakYtelseType.SVANGERSKAPSPENGER),
    SVP_OPPTJENING_TYPE("SVP_OPPTJENING_TYPE", "§14-4 3. ledd Opptjening/ inntekt", FagsakYtelseType.SVANGERSKAPSPENGER),
    SVP_BEREGNING_TYPE("SVP_BEREGNING_TYPE", "§14-4 5. ledd Beregning", FagsakYtelseType.SVANGERSKAPSPENGER),
    SVP_UTTAK_TYPE("SVP_UTTAK_TYPE", "§14-4 Uttak", FagsakYtelseType.SVANGERSKAPSPENGER),
    SVP_OPPHØR("OPPHØR", "Opphør", FagsakYtelseType.SVANGERSKAPSPENGER),
    SVP_ANNET_TYPE("SVP_ANNET_TYPE", "Annet", FagsakYtelseType.SVANGERSKAPSPENGER);

    private final String kode;
    private final String navn;
    private FagsakYtelseType fagsakYtelseType;

    public static final String KODEVERK = "HENDELSE_TYPE";
    private static final Map<String, HendelseType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private HendelseType(String kode, String navn, FagsakYtelseType fagsakYtelseType) {
        this.kode = kode;
        this.navn = navn;
        this.fagsakYtelseType = fagsakYtelseType;
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

    public FagsakYtelseType getFagsakYtelseType() {
        return fagsakYtelseType;
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

    public static Set<HendelseType> getHendelseTyperForFagsakYtleseType(FagsakYtelseType fagsakYtelseType) {
        Set<HendelseType> hendelseTyper = new HashSet<>();
        for (var hendelseType : values()) {
            if (hendelseType.getFagsakYtelseType().equals(fagsakYtelseType)) {
                hendelseTyper.add(hendelseType);
            }
        }
        return hendelseTyper;
    }
}
