package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class HendelseUndertypePrHendelseType {

    private static final Map<HendelseType, Set<HendelseUnderType>> HIERARKI = Collections.unmodifiableMap(lagMapping());

    private static Map<HendelseType, Set<HendelseUnderType>> lagMapping() {
        Map<HendelseType, Set<HendelseUnderType>> h = new EnumMap<>(HendelseType.class);
        h.put(HendelseType.FP_BEREGNING_TYPE, Set.of(
            HendelseUnderType.ENDRING_GRUNNLAG,
            HendelseUnderType.INNTEKT_UNDER));
        h.put(HendelseType.ES_ADOPSJONSVILKAARET_TYPE, Set.of(
            HendelseUnderType.ES_BARN_OVER_15,
            HendelseUnderType.ES_IKKE_OPPFYLT,
            HendelseUnderType.ES_MANN_IKKE_ALENE,
            HendelseUnderType.ES_STEBARN));
        h.put(HendelseType.ES_ANNET_TYPE, Set.of(
            HendelseUnderType.ANNET_FRITEKST));
        h.put(HendelseType.ES_FEIL_UTBETALING_TYPE, Set.of(
            HendelseUnderType.ES_STONAD_FLERE_GANGER,
            HendelseUnderType.LEGACY_ØKONOMI_UTBETALT_FOR_MYE));
        h.put(HendelseType.ES_FODSELSVILKAARET_TYPE, Set.of(
            HendelseUnderType.ES_BARN_IKKE_REGISTRERT,
            HendelseUnderType.ES_MOTTAKER_FAR_MEDMOR));
        h.put(HendelseType.ES_FORELDREANSVAR_FAR_TYPE, Set.of(
            HendelseUnderType.ES_FAR_IKKE_ALENE,
            HendelseUnderType.ES_FAR_IKKE_INNEN_STONADSPERIODE));
        h.put(HendelseType.ES_FORELDREANSVAR_TYPE, Set.of(
            HendelseUnderType.ES_ANDRE_FORELDRE_DODD,
            HendelseUnderType.ES_FORELDREANSVAR_BARN_OVER_15,
            HendelseUnderType.ES_IKKE_MINDRE_SAMVAER,
            HendelseUnderType.ES_IKKE_TILDELT));
        h.put(HendelseType.ES_OMSORGSVILKAAR_TYPE, Set.of(
            HendelseUnderType.ES_FAR_IKKE_OMSORG,
            HendelseUnderType.ES_STONADEN_ALLEREDE_UTBETALT));
        h.put(HendelseType.ES_RETT_PAA_FORELDREPENGER_TYPE, Set.of(
            HendelseUnderType.ES_BRUKER_RETT_FORELDREPENGER));
        h.put(HendelseType.FP_ANNET_HENDELSE_TYPE, Set.of(
            HendelseUnderType.ANNET_FRITEKST,
            HendelseUnderType.REFUSJON_ARBEIDSGIVER));
        h.put(HendelseType.FP_KUN_RETT_TYPE, Set.of(
            HendelseUnderType.FEIL_I_ANTALL_DAGER));
        h.put(HendelseType.MEDLEMSKAP_TYPE, Set.of(
            HendelseUnderType.IKKE_BOSATT,
            HendelseUnderType.IKKE_LOVLIG_OPPHOLD,
            HendelseUnderType.IKKE_OPPHOLDSRETT_EØS,
            HendelseUnderType.MEDLEM_I_ANNET_LAND,
            HendelseUnderType.UTVANDRET));
        h.put(HendelseType.ØKONOMI_FEIL, Set.of(
            HendelseUnderType.DOBBELTUTBETALING,
            HendelseUnderType.FEIL_FERIEPENGER,
            HendelseUnderType.FEIL_TREKK,
            HendelseUnderType.FOR_MYE_UTBETALT));
        h.put(HendelseType.SVP_OPPHØR, Set.of(
            HendelseUnderType.MOTTAKER_DØD,
            HendelseUnderType.MOTTAKER_IKKE_GRAVID));
        h.put(HendelseType.FP_OPPTJENING_TYPE, Set.of(
            HendelseUnderType.IKKE_INNTEKT,
            HendelseUnderType.IKKE_YRKESAKTIV));
        h.put(HendelseType.FP_STONADSPERIODEN_TYPE, Set.of(
            HendelseUnderType.ENDRET_DEKNINGSGRAD,
            HendelseUnderType.FEIL_FLERBARNSDAGER,
            HendelseUnderType.OPPHOR_BARN_DOD,
            HendelseUnderType.OPPHOR_MOTTAKER_DOD));
        h.put(HendelseType.SVP_ANNET_TYPE, Set.of(
            HendelseUnderType.ANNET_FRITEKST,
            HendelseUnderType.REFUSJON_ARBEIDSGIVER));
        h.put(HendelseType.SVP_ARBEIDSGIVERS_FORHOLD_TYPE, Set.of(
            HendelseUnderType.SVP_TILRETTELEGGING_DELVIS_MULIG,
            HendelseUnderType.SVP_TILRETTELEGGING_FULLT_MULIG));
        h.put(HendelseType.SVP_BEREGNING_TYPE, Set.of(
            HendelseUnderType.SVP_ENDRING_GRUNNLAG));
        h.put(HendelseType.SVP_FAKTA_TYPE, Set.of(
            HendelseUnderType.SVP_ENDRING_TERMINDATO,
            HendelseUnderType.SVP_IKKE_HELSEFARLIG,
            HendelseUnderType.SVP_TIDLIG_FODSEL));
        h.put(HendelseType.SVP_OPPTJENING_TYPE, Set.of(
            HendelseUnderType.SVP_IKKE_ARBEID,
            HendelseUnderType.SVP_INNTEKT_IKKE_TAP,
            HendelseUnderType.SVP_INNTEKT_UNDER));
        h.put(HendelseType.SVP_UTTAK_TYPE, Set.of(
            HendelseUnderType.SVP_ENDRING_PERIODE,
            HendelseUnderType.SVP_ENDRING_PROSENT));
        h.put(HendelseType.FP_UTTAK_ALENEOMSORG_TYPE, Set.of(
            HendelseUnderType.IKKE_ALENEOMSORG));
        h.put(HendelseType.FP_UTTAK_GENERELT_TYPE, Set.of(
            HendelseUnderType.FORELDRES_UTTAK,
            HendelseUnderType.IKKE_OMSORG,
            HendelseUnderType.MOTTAKER_I_ARBEID,
            HendelseUnderType.NY_STONADSPERIODE,
            HendelseUnderType.STONADSPERIODE_MANGEL,
            HendelseUnderType.STONADSPERIODE_OVER_3));
        h.put(HendelseType.FP_UTTAK_GRADERT_TYPE, Set.of(
            HendelseUnderType.GRADERT_UTTAK));
        h.put(HendelseType.FP_UTTAK_KVOTENE_TYPE, Set.of(
            HendelseUnderType.KVO_MOTTAKER_HELT_AVHENGIG,
            HendelseUnderType.KVO_MOTTAKER_INNLAGT,
            HendelseUnderType.KVO_SAMTIDIG_UTTAK));
        h.put(HendelseType.FP_UTTAK_UTSETTELSE_TYPE, Set.of(
            HendelseUnderType.ARBEID_HELTID,
            HendelseUnderType.BARN_INNLAGT,
            HendelseUnderType.LOVBESTEMT_FERIE,
            HendelseUnderType.MOTTAKER_HELT_AVHENGIG,
            HendelseUnderType.MOTTAKER_INNLAGT));
        h.put(HendelseType.FP_VILKAAR_GENERELLE_TYPE, Set.of(
            HendelseUnderType.MOR_IKKE_ARBEID,
            HendelseUnderType.MOR_IKKE_ARBEID_OG_STUDER,
            HendelseUnderType.MOR_IKKE_HELT_AVHENGIG,
            HendelseUnderType.MOR_IKKE_INNLAGT,
            HendelseUnderType.MOR_IKKE_I_IP,
            HendelseUnderType.MOR_IKKE_I_KP,
            HendelseUnderType.MOR_IKKE_STUDERT));
        return h;
    }

    public static Map<HendelseType, Set<HendelseUnderType>> getHendelsetypeHierarki() {
        return HIERARKI;
    }

    public static Set<HendelseUnderType> getHendelsetyper(HendelseType hendelseType) {
        if (!HIERARKI.containsKey(hendelseType)) {
            throw new IllegalArgumentException("Ikke-støttet hendelseType: " + hendelseType);
        }
        return HIERARKI.get(hendelseType);
    }


}
