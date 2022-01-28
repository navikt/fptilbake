package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import java.util.Map;
import java.util.Set;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;

public class HendelseTypePrYtelseType {

    private HendelseTypePrYtelseType() {
        // sonar
    }

    private static final Map<FagsakYtelseType, Set<HendelseType>> HIERARKI = Map.of(
            FagsakYtelseType.FORELDREPENGER, Set.of(
                    HendelseType.MEDLEMSKAP_TYPE,
                    HendelseType.FP_OPPTJENING_TYPE,
                    HendelseType.FP_BEREGNING_TYPE,
                    HendelseType.FP_STONADSPERIODEN_TYPE,
                    HendelseType.FP_UTTAK_GENERELT_TYPE,
                    HendelseType.FP_UTTAK_UTSETTELSE_TYPE,
                    HendelseType.FP_UTTAK_KVOTENE_TYPE,
                    HendelseType.FP_VILKAAR_GENERELLE_TYPE,
                    HendelseType.FP_KUN_RETT_TYPE,
                    HendelseType.FP_UTTAK_ALENEOMSORG_TYPE,
                    HendelseType.FP_UTTAK_GRADERT_TYPE,
                    HendelseType.FP_ANNET_HENDELSE_TYPE),
            FagsakYtelseType.SVANGERSKAPSPENGER, Set.of(
                    HendelseType.SVP_FAKTA_TYPE,
                    HendelseType.SVP_ARBEIDSGIVERS_FORHOLD_TYPE,
                    HendelseType.SVP_OPPTJENING_TYPE,
                    HendelseType.SVP_BEREGNING_TYPE,
                    HendelseType.SVP_UTTAK_TYPE,
                    HendelseType.SVP_ANNET_TYPE,
                    HendelseType.MEDLEMSKAP_TYPE,
                    HendelseType.SVP_OPPHØR),
            FagsakYtelseType.ENGANGSTØNAD, Set.of(
                    HendelseType.ES_FODSELSVILKAARET_TYPE,
                    HendelseType.ES_ADOPSJONSVILKAARET_TYPE,
                    HendelseType.ES_FORELDREANSVAR_TYPE,
                    HendelseType.ES_OMSORGSVILKAAR_TYPE,
                    HendelseType.ES_FORELDREANSVAR_FAR_TYPE,
                    HendelseType.ES_RETT_PAA_FORELDREPENGER_TYPE,
                    HendelseType.ES_FEIL_UTBETALING_TYPE,
                    HendelseType.ES_ANNET_TYPE,
                    HendelseType.ES_MEDLEMSKAP_TYPE),
            FagsakYtelseType.FRISINN, Set.of(
                    HendelseType.FRISINN_ANNET_TYPE),
            FagsakYtelseType.PLEIEPENGER_SYKT_BARN, Set.of(
                    HendelseType.PSB_ANNET_TYPE),
            FagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE, Set.of(
                    HendelseType.PPN_ANNET_TYPE),
            FagsakYtelseType.OMSORGSPENGER, Set.of(
                    HendelseType.OMP_ANNET_TYPE),
            FagsakYtelseType.OPPLÆRINGSPENGER, Set.of(
                    HendelseType.OLP_ANNET_TYPE)
    );

    public static Map<FagsakYtelseType, Set<HendelseType>> getHendelsetypeHierarki() {
        return HIERARKI;
    }

    public static Set<HendelseType> getHendelsetyper(FagsakYtelseType fagsakYtelseType) {
        if (!HIERARKI.containsKey(fagsakYtelseType)) {
            throw new IllegalArgumentException("Ikke-støttet fagsakYtelseType: " + fagsakYtelseType);
        }
        return HIERARKI.get(fagsakYtelseType);
    }


}
