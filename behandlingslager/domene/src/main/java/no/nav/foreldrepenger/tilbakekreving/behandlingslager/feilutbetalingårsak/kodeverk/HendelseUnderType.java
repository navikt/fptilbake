package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

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
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public enum HendelseUnderType implements Kodeverdi {

    //FP
    IKKE_INNTEKT("IKKE_INNTEKT", "Ikke inntekt 6 av siste 10 måneder", 0),
    IKKE_YRKESAKTIV("IKKE_YRKESAKTIV", "Ikke yrkesaktiv med pensjonsgivende inntekt", 1),
    INNTEKT_UNDER("INNTEKT_UNDER", "Inntekt under 1/2 G", 1),
    ENDRING_GRUNNLAG("ENDRING_GRUNNLAG", "Endring i selve grunnlaget", 0),
    ENDRET_DEKNINGSGRAD("ENDRET_DEKNINGSGRAD", "Endret dekningsgrad", 0),
    FEIL_FLERBARNSDAGER("FEIL_FLERBARNSDAGER", "Feil i flerbarnsdager", 1),
    OPPHOR_BARN_DOD("OPPHOR_BARN_DOD", "Opphør barn død", 2),
    OPPHOR_MOTTAKER_DOD("OPPHOR_MOTTAKER_DOD", "Opphør mottaker død", 3),
    STONADSPERIODE_OVER_3("STONADSPERIODE_OVER_3", "Stønadsperiode over 3 år", 0),
    NY_STONADSPERIODE("NY_STONADSPERIODE", "Ny stønadsperiode for nytt barn", 1),
    IKKE_OMSORG("IKKE_OMSORG", "Ikke omsorg for barnet", 2),
    MOTTAKER_I_ARBEID("MOTTAKER_I_ARBEID", "Mottaker i arbeid heltid", 3),
    FORELDRES_UTTAK("FORELDRES_UTTAK", "Ikke rett til samtidig uttak", 4),
    STONADSPERIODE_MANGEL("STONADSPERIODE_MANGEL", "Manglende stønadsperiode", 5),
    LOVBESTEMT_FERIE("LOVBESTEMT_FERIE", "Lovbestemt ferie", 0),
    ARBEID_HELTID("ARBEID_HELTID", "Arbeid heltid", 1),
    MOTTAKER_HELT_AVHENGIG("MOTTAKER_HELT_AVHENGIG", "Mottaker helt avhenig av hjelp til å ta seg av barnet", 2),
    MOTTAKER_INNLAGT("MOTTAKER_INNLAGT", "Mottaker innlagt i helseinstitusjon", 3),
    BARN_INNLAGT("BARN_INNLAGT", "Barn innlagt i helseinstitusjon", 4),
    KVO_MOTTAKER_HELT_AVHENGIG("KVO_MOTTAKER_HELT_AVHENGIG", "Mottaker helt avhenig av hjelp til å ta seg av barnet", 0),
    KVO_MOTTAKER_INNLAGT("KVO_MOTTAKER_INNLAGT", "Mottaker innlagt i helseinstitusjon", 1),
    KVO_SAMTIDIG_UTTAK("KVO_SAMTIDIG_UTTAK", "Samtidig uttak", 3),
    MOR_IKKE_ARBEID("MOR_IKKE_ARBEID", "Mor ikke arbeidet heltid", 0),
    MOR_IKKE_STUDERT("MOR_IKKE_STUDERT", "Mor ikke studert heltid", 1),
    MOR_IKKE_ARBEID_OG_STUDER("MOR_IKKE_ARBEID_OG_STUDER", "Mor ikke arbeid og studier - heltid", 2),
    MOR_IKKE_HELT_AVHENGIG("MOR_IKKE_HELT_AVHENGIG", "Mor ikke helt avhengig av hjelp til å ta seg av barnet", 3),
    MOR_IKKE_INNLAGT("MOR_IKKE_INNLAGT", "Mor ikke innlagt helseinstitusjon", 4),
    MOR_IKKE_I_IP("MOR_IKKE_I_IP", "Mor ikke i introduksjonsprogram", 5),
    MOR_IKKE_I_KP("MOR_IKKE_I_KP", "Mor ikke i kvalifiseringsprogram", 6),
    FEIL_I_ANTALL_DAGER("FEIL_I_ANTALL_DAGER", "Feil i antall dager", 0),
    IKKE_ALENEOMSORG("IKKE_ALENEOMSORG", "Ikke aleneomsorg", 0),
    GRADERT_UTTAK("GRADERT_UTTAK", "Gradert uttak", 0),
    //SVP
    SVP_ENDRING_TERMINDATO("SVP_ENDRING_TERMINDATO", "Endring i termindato", 0),
    SVP_TIDLIG_FODSEL("SVP_TIDLIG_FODSEL", "Tidlig fødsel", 1),
    SVP_IKKE_HELSEFARLIG("SVP_IKKE_HELSEFARLIG", "Ikke helsefarlig for ventende barn", 2),
    SVP_TILRETTELEGGING_FULLT_MULIG("SVP_TILRETTELEGGING_FULLT_MULIG", "Tilrettelegging fullt mulig", 0),
    SVP_TILRETTELEGGING_DELVIS_MULIG("SVP_TILRETTELEGGING_DELVIS_MULIG", "Tilrettelegging delvis mulig", 1),
    SVP_IKKE_ARBEID("SVP_IKKE_ARBEID", "Ikke i arbeid siste 4 uker", 10),
    SVP_INNTEKT_UNDER("SVP_INNTEKT_UNDER", "Inntekt under 1/2 G", 30),
    SVP_ENDRING_GRUNNLAG("SVP_ENDRING_GRUNNLAG", "Endring i selve grunnlaget", 0),
    SVP_ENDRING_PROSENT("SVP_ENDRING_PROSENT", "Endring i uttaksprosent/gradering", 0),
    SVP_ENDRING_PERIODE("SVP_ENDRING_PERIODE", "Endring av periode", 1),
    MOTTAKER_DØD("MOTTAKER_DØD", "Mottaker død", 1),
    MOTTAKER_IKKE_GRAVID("MOTTAKER_IKKE_GRAVID", "Mottaker ikke lenger gravid", 2),
    SVP_INNTEKT_IKKE_TAP("SVP_INNTEKT_IKKE_TAP", "Ikke tap av pensjonsgivende inntekt", 20),
    //ES
    ES_BARN_IKKE_REGISTRERT("ES_BARN_IKKE_REGISTRERT", "Barn ikke registrert", 0),
    ES_MOTTAKER_FAR_MEDMOR("ES_MOTTAKER_FAR_MEDMOR", "Mottaker er far eller medmor", 1),
    ES_IKKE_OPPFYLT("ES_IKKE_OPPFYLT", "Adopsjonsvilkår ikke oppfylt", 0),
    ES_BARN_OVER_15("ES_BARN_OVER_15", "Barn over 15 år", 1),
    ES_MANN_IKKE_ALENE("ES_MANN_IKKE_ALENE", "Mann adopterer ikke alene", 2),
    ES_STEBARN("ES_STEBARN", "Stebarnsadopsjon", 3),
    ES_ANDRE_FORELDRE_DODD("ES_ANDRE_FORELDRE_DODD", "Har ikke foreldreansvar ved andre foreldres død", 0),
    ES_IKKE_TILDELT("ES_IKKE_TILDELT", "Ikke tildelt foreldreansvar etter barneloven", 1),
    ES_IKKE_MINDRE_SAMVAER("ES_IKKE_MINDRE_SAMVAER", "Ikke hatt mindre samvær enn barneloven §43", 2),
    ES_FORELDREANSVAR_BARN_OVER_15("ES_FORELDREANSVAR_BARN_OVER_15", "Barn over 15 år", 3),
    ES_FAR_IKKE_OMSORG("ES_FAR_IKKE_OMSORG", "Far ikke omsorg for barnet", 0),
    ES_STONADEN_ALLEREDE_UTBETALT("ES_STONADEN_ALLEREDE_UTBETALT", "Stønaden allerede utbetalt til mor", 1),
    ES_FAR_IKKE_ALENE("ES_FAR_IKKE_ALENE", "Far overtar ikke foreldreansvar alene", 0),
    ES_FAR_IKKE_INNEN_STONADSPERIODE("ES_FAR_IKKE_INNEN_STONADSPERIODE", "Far overtar ikke innen stønadsperioden", 1),
    ES_BRUKER_RETT_FORELDREPENGER("ES_BRUKER_RETT_FORELDREPENGER", "Bruker har likevel rett på foreldrepenger", 0),
    ES_STONAD_FLERE_GANGER("ES_STONAD_FLERE_GANGER", "Stønad gitt for samme barn flere ganger", 0),
    //medlemskap
    UTVANDRET("UTVANDRET", "Utvandret", 0),
    IKKE_LOVLIG_OPPHOLD("IKKE_LOVLIG_OPPHOLD", "Ikke lovlig opphold", 3),
    MEDLEM_I_ANNET_LAND("MEDLEM_I_ANNET_LAND", "Unntak medlemskap/medlem annet land", 4),
    IKKE_OPPHOLDSRETT_EØS("IKKE_OPPHOLDSRETT_EØS", "Ikke oppholdsrett EØS", 2),
    IKKE_BOSATT("IKKE_BOSATT", "Ikke bosatt", 1),
    //økonomi
    DOBBELTUTBETALING("OKONOMI_DOBBELUTBETALING", "Dobbeltutbetaling", 0),
    FOR_MYE_UTBETALT("OKONOMI_UTBETALT", "Utbetalt for mye", 1),
    FEIL_TREKK("OKONOMI_FEIL_TREKK", "Feil trekk", 2),
    FEIL_FERIEPENGER("OKONOMI_FEIL_FERIEPENGER", "Feil feriepengeutbetaling", 5),
    //legacy (kan ikke lenger velges, men finnes i prod-data. Må bevares for å støtte visning av disse data)
    LEGACY_ØKONOMI_UTBETALT_FOR_MYE("ØKONOMI_UTBETALT_FOR_MYE", "Feil i økonomi - utbetalt for mye", 1),
    //FELLES
    REFUSJON_ARBEIDSGIVER("REFUSJON_ARBGIVER", "Refusjon til arbeidsgiver", 1),
    ANNET_FRITEKST("ANNET_FRITEKST", "Annet - fritekst", 3),
    IKKE_SATT("-", null, 0),
    ;

    private static final Map<String, HendelseUnderType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    public static Map<String, HendelseUnderType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    private String kode;
    private String navn;
    private int sortering;

    HendelseUnderType(String kode, String navn, int sortering) {
        this.kode = kode;
        this.navn = navn;
        this.sortering = sortering;
    }

    @JsonCreator
    public static HendelseUnderType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent HendelseType: " + kode);
        }
        return ad;
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
        return "HENDELSE_UNDERTYPE";
    }

    @JsonProperty
    @Override
    public String getNavn() {
        return navn;
    }

    public int getSortering() {
        return sortering;
    }

    @Override
    public String toString() {
        return kode;
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<HendelseUnderType, String> {
        @Override
        public String convertToDatabaseColumn(HendelseUnderType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public HendelseUnderType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }

}


