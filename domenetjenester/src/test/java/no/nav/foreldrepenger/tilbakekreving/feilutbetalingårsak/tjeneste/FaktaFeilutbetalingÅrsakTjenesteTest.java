package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertyperDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTyperPrYtelseTypeDto;

public class FaktaFeilutbetalingÅrsakTjenesteTest {

    private FeilutbetalingÅrsakTjeneste feilutbetalingÅrsakTjeneste = new FeilutbetalingÅrsakTjeneste();

    @Test
    public void skal_ha_riktige_årsaker_og_underårsaker_for_foreldrepenger() {
        Map<HendelseType, List<HendelseUnderType>> mapAvResultat = hentÅrsakerForYtelseType(FagsakYtelseType.FORELDREPENGER);

        assertThat(mapAvResultat.keySet()).containsExactly(
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
            HendelseType.FP_ANNET_HENDELSE_TYPE
        );

        assertThat(mapAvResultat.get(HendelseType.MEDLEMSKAP_TYPE)).containsExactly(
            HendelseUnderType.UTVANDRET,
            HendelseUnderType.IKKE_BOSATT,
            HendelseUnderType.IKKE_LOVLIG_OPPHOLD,
            HendelseUnderType.MEDLEM_I_ANNET_LAND
        );

        assertThat(mapAvResultat.get(HendelseType.FP_OPPTJENING_TYPE)).containsExactly(
            HendelseUnderType.IKKE_INNTEKT,
            HendelseUnderType.IKKE_YRKESAKTIV
        );

        assertThat(mapAvResultat.get(HendelseType.FP_BEREGNING_TYPE)).containsExactly(
            HendelseUnderType.ENDRING_GRUNNLAG,
            HendelseUnderType.INNTEKT_UNDER
        );

        assertThat(mapAvResultat.get(HendelseType.FP_STONADSPERIODEN_TYPE)).containsExactly(
            HendelseUnderType.ENDRET_DEKNINGSGRAD,
            HendelseUnderType.FEIL_FLERBARNSDAGER,
            HendelseUnderType.OPPHOR_BARN_DOD,
            HendelseUnderType.OPPHOR_MOTTAKER_DOD
        );

        assertThat(mapAvResultat.get(HendelseType.FP_UTTAK_GENERELT_TYPE)).containsExactly(
            HendelseUnderType.STONADSPERIODE_OVER_3,
            HendelseUnderType.NY_STONADSPERIODE,
            HendelseUnderType.IKKE_OMSORG,
            HendelseUnderType.MOTTAKER_I_ARBEID,
            HendelseUnderType.FORELDRES_UTTAK,
            HendelseUnderType.STONADSPERIODE_MANGEL
        );

        assertThat(mapAvResultat.get(HendelseType.FP_UTTAK_UTSETTELSE_TYPE)).containsExactly(
            HendelseUnderType.LOVBESTEMT_FERIE,
            HendelseUnderType.ARBEID_HELTID,
            HendelseUnderType.MOTTAKER_HELT_AVHENGIG,
            HendelseUnderType.MOTTAKER_INNLAGT,
            HendelseUnderType.BARN_INNLAGT
        );

        assertThat(mapAvResultat.get(HendelseType.FP_UTTAK_KVOTENE_TYPE)).containsExactly(
            HendelseUnderType.KVO_MOTTAKER_HELT_AVHENGIG,
            HendelseUnderType.KVO_MOTTAKER_INNLAGT,
            HendelseUnderType.KVO_SAMTIDIG_UTTAK
        );

        assertThat(mapAvResultat.get(HendelseType.FP_VILKAAR_GENERELLE_TYPE)).containsExactly(
            HendelseUnderType.MOR_IKKE_ARBEID,
            HendelseUnderType.MOR_IKKE_STUDERT,
            HendelseUnderType.MOR_IKKE_ARBEID_OG_STUDER,
            HendelseUnderType.MOR_IKKE_HELT_AVHENGIG,
            HendelseUnderType.MOR_IKKE_INNLAGT,
            HendelseUnderType.MOR_IKKE_I_IP,
            HendelseUnderType.MOR_IKKE_I_KP
        );

        assertThat(mapAvResultat.get(HendelseType.FP_ANNET_HENDELSE_TYPE)).containsExactly(
            HendelseUnderType.REFUSJON_ARBEIDSGIVER,
            HendelseUnderType.ANNET_FRITEKST,
            HendelseUnderType.FEIL_FERIEPENGER_4G
        );

        assertThat(mapAvResultat.get(HendelseType.FP_KUN_RETT_TYPE)).containsOnly(
            HendelseUnderType.FEIL_I_ANTALL_DAGER
        );

        assertThat(mapAvResultat.get(HendelseType.FP_UTTAK_ALENEOMSORG_TYPE)).containsOnly(
            HendelseUnderType.IKKE_ALENEOMSORG
        );

        assertThat(mapAvResultat.get(HendelseType.FP_UTTAK_GRADERT_TYPE)).containsOnly(
            HendelseUnderType.GRADERT_UTTAK
        );

    }

    @Test
    public void skal_ha_riktige_årsaker_og_underårsaker_for_svangerskapspenger() {
        Map<HendelseType, List<HendelseUnderType>> mapAvResultat = hentÅrsakerForYtelseType(FagsakYtelseType.SVANGERSKAPSPENGER);

        assertThat(mapAvResultat.keySet()).containsExactly(
            HendelseType.MEDLEMSKAP_TYPE,
            HendelseType.SVP_FAKTA_TYPE,
            HendelseType.SVP_ARBEIDSGIVERS_FORHOLD_TYPE,
            HendelseType.SVP_OPPTJENING_TYPE,
            HendelseType.SVP_BEREGNING_TYPE,
            HendelseType.SVP_UTTAK_TYPE,
            HendelseType.SVP_OPPHØR,
            HendelseType.SVP_ANNET_TYPE
        );

        assertThat(mapAvResultat.get(HendelseType.MEDLEMSKAP_TYPE)).containsExactly(
            HendelseUnderType.UTVANDRET,
            HendelseUnderType.IKKE_BOSATT,
            HendelseUnderType.IKKE_LOVLIG_OPPHOLD,
            HendelseUnderType.MEDLEM_I_ANNET_LAND
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_OPPHØR)).containsExactly(
            HendelseUnderType.MOTTAKER_DØD,
            HendelseUnderType.MOTTAKER_IKKE_GRAVID
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_FAKTA_TYPE)).containsExactly(
            HendelseUnderType.SVP_ENDRING_TERMINDATO,
            HendelseUnderType.SVP_TIDLIG_FODSEL,
            HendelseUnderType.SVP_IKKE_HELSEFARLIG
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_ARBEIDSGIVERS_FORHOLD_TYPE)).containsExactly(
            HendelseUnderType.SVP_TILRETTELEGGING_FULLT_MULIG,
            HendelseUnderType.SVP_TILRETTELEGGING_DELVIS_MULIG
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_OPPTJENING_TYPE)).containsExactly(
            HendelseUnderType.SVP_IKKE_ARBEID,
            HendelseUnderType.SVP_INNTEKT_IKKE_TAP,
            HendelseUnderType.SVP_INNTEKT_UNDER
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_BEREGNING_TYPE)).containsExactly(
            HendelseUnderType.SVP_ENDRING_GRUNNLAG
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_UTTAK_TYPE)).containsExactly(
            HendelseUnderType.SVP_ENDRING_PROSENT,
            HendelseUnderType.SVP_ENDRING_PERIODE
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_ANNET_TYPE)).containsExactly(
            HendelseUnderType.REFUSJON_ARBEIDSGIVER,
            HendelseUnderType.ANNET_FRITEKST,
            HendelseUnderType.FEIL_FERIEPENGER_4G
        );
    }

    @Test
    public void skal_ha_riktige_årsaker_og_underårsaker_for_engangstønad() {
        Map<HendelseType, List<HendelseUnderType>> mapAvResultat = hentÅrsakerForYtelseType(FagsakYtelseType.ENGANGSTØNAD);

        assertThat(mapAvResultat.keySet()).containsExactly(
            HendelseType.ES_MEDLEMSKAP_TYPE,
            HendelseType.ES_ADOPSJONSVILKAARET_TYPE,
            HendelseType.ES_FODSELSVILKAARET_TYPE,
            HendelseType.ES_FORELDREANSVAR_TYPE,
            HendelseType.ES_OMSORGSVILKAAR_TYPE,
            HendelseType.ES_FORELDREANSVAR_FAR_TYPE,
            HendelseType.ES_RETT_PAA_FORELDREPENGER_TYPE,
            HendelseType.ES_FEIL_UTBETALING_TYPE,
            HendelseType.ES_ANNET_TYPE
        );

        assertThat(mapAvResultat.get(HendelseType.ES_MEDLEMSKAP_TYPE)).containsExactly(
            HendelseUnderType.UTVANDRET,
            HendelseUnderType.IKKE_BOSATT,
            HendelseUnderType.IKKE_OPPHOLDSRETT_EØS,
            HendelseUnderType.IKKE_LOVLIG_OPPHOLD,
            HendelseUnderType.MEDLEM_I_ANNET_LAND
        );

        assertThat(mapAvResultat.get(HendelseType.ES_ADOPSJONSVILKAARET_TYPE)).containsExactly(
            HendelseUnderType.ES_IKKE_OPPFYLT,
            HendelseUnderType.ES_BARN_OVER_15,
            HendelseUnderType.ES_MANN_IKKE_ALENE,
            HendelseUnderType.ES_STEBARN
        );

        assertThat(mapAvResultat.get(HendelseType.ES_FODSELSVILKAARET_TYPE)).containsExactly(
            HendelseUnderType.ES_BARN_IKKE_REGISTRERT,
            HendelseUnderType.ES_MOTTAKER_FAR_MEDMOR
        );

        assertThat(mapAvResultat.get(HendelseType.ES_FORELDREANSVAR_TYPE)).containsExactly(
            HendelseUnderType.ES_ANDRE_FORELDRE_DODD,
            HendelseUnderType.ES_IKKE_TILDELT,
            HendelseUnderType.ES_IKKE_MINDRE_SAMVAER,
            HendelseUnderType.ES_FORELDREANSVAR_BARN_OVER_15
        );

        assertThat(mapAvResultat.get(HendelseType.ES_OMSORGSVILKAAR_TYPE)).containsExactly(
            HendelseUnderType.ES_FAR_IKKE_OMSORG,
            HendelseUnderType.ES_STONADEN_ALLEREDE_UTBETALT
        );

        assertThat(mapAvResultat.get(HendelseType.ES_FORELDREANSVAR_FAR_TYPE)).containsExactly(
            HendelseUnderType.ES_FAR_IKKE_ALENE,
            HendelseUnderType.ES_FAR_IKKE_INNEN_STONADSPERIODE
        );

        assertThat(mapAvResultat.get(HendelseType.ES_RETT_PAA_FORELDREPENGER_TYPE)).containsExactly(
            HendelseUnderType.ES_BRUKER_RETT_FORELDREPENGER
        );

        assertThat(mapAvResultat.get(HendelseType.ES_FEIL_UTBETALING_TYPE)).containsExactly(
            HendelseUnderType.ES_STONAD_FLERE_GANGER,
            HendelseUnderType.LEGACY_ØKONOMI_UTBETALT_FOR_MYE
        );

        assertThat(mapAvResultat.get(HendelseType.ES_ANNET_TYPE)).containsExactly(
            HendelseUnderType.ANNET_FRITEKST
        );
    }

    private Map<HendelseType, List<HendelseUnderType>> hentÅrsakerForYtelseType(FagsakYtelseType fagsakYtelseType) {
        List<HendelseTypeMedUndertyperDto> feilutbetalingÅrsaker = feilutbetalingÅrsakTjeneste.hentFeilutbetalingårsaker()
            .stream()
            .filter(v -> v.getYtelseType().equals(fagsakYtelseType))
            .map(HendelseTyperPrYtelseTypeDto::getHendelseTyper)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Ukjent ytelseType:" + fagsakYtelseType));

        LinkedHashMap<HendelseType, List<HendelseUnderType>> mapAvResultat = new LinkedHashMap<>();
        feilutbetalingÅrsaker.forEach(
            m -> mapAvResultat.put(m.getHendelseType(), m.getHendelseUndertyper())
        );

        return mapAvResultat;
    }
}
