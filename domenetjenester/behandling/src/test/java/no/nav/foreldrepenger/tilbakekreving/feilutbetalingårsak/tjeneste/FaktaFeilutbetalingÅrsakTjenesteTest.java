package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.FellesUndertyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.FpHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.MedlemskapHendelseUndertyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.SvpHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.ØkonomiUndertyper;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertyperDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTyperPrYtelseTypeDto;

public class FaktaFeilutbetalingÅrsakTjenesteTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private FeilutbetalingÅrsakTjeneste feilutbetalingÅrsakTjeneste = new FeilutbetalingÅrsakTjeneste(repositoryProvider.getKodeverkRepository());

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
            HendelseType.ØKONOMI_FEIL,
            HendelseType.FP_ANNET_HENDELSE_TYPE
        );

        assertThat(mapAvResultat.get(HendelseType.ØKONOMI_FEIL)).containsExactly(
            ØkonomiUndertyper.DOBBELTUTBETALING,
            ØkonomiUndertyper.FOR_MYE_UTBETALT,
            ØkonomiUndertyper.FEIL_TREKK,
            ØkonomiUndertyper.FEIL_FERIEPENGER
        );

        assertThat(mapAvResultat.get(HendelseType.MEDLEMSKAP_TYPE)).containsExactly(
            MedlemskapHendelseUndertyper.UTVANDRET,
            MedlemskapHendelseUndertyper.IKKE_BOSATT,
            MedlemskapHendelseUndertyper.IKKE_OPPHOLDSRETT_EØS,
            MedlemskapHendelseUndertyper.IKKE_LOVLIG_OPPHOLD,
            MedlemskapHendelseUndertyper.MEDLEM_I_ANNET_LAND
        );

        assertThat(mapAvResultat.get(HendelseType.FP_OPPTJENING_TYPE)).containsExactly(
            FpHendelseUnderTyper.IKKE_INNTEKT,
            FpHendelseUnderTyper.IKKE_YRKESAKTIV
        );

        assertThat(mapAvResultat.get(HendelseType.FP_BEREGNING_TYPE)).containsExactly(
            FpHendelseUnderTyper.ENDRING_GRUNNLAG,
            FpHendelseUnderTyper.INNTEKT_UNDER
        );

        assertThat(mapAvResultat.get(HendelseType.FP_STONADSPERIODEN_TYPE)).containsExactly(
            FpHendelseUnderTyper.ENDRET_DEKNINGSGRAD,
            FpHendelseUnderTyper.FEIL_FLERBARNSDAGER,
            FpHendelseUnderTyper.OPPHOR_BARN_DOD,
            FpHendelseUnderTyper.OPPHOR_MOTTAKER_DOD
        );

        assertThat(mapAvResultat.get(HendelseType.FP_UTTAK_GENERELT_TYPE)).containsExactly(
            FpHendelseUnderTyper.STONADSPERIODE_OVER_3,
            FpHendelseUnderTyper.NY_STONADSPERIODE,
            FpHendelseUnderTyper.IKKE_OMSORG,
            FpHendelseUnderTyper.MOTTAKER_I_ARBEID,
            FpHendelseUnderTyper.FORELDRES_UTTAK,
            FpHendelseUnderTyper.STONADSPERIODE_MANGEL
        );

        assertThat(mapAvResultat.get(HendelseType.FP_UTTAK_UTSETTELSE_TYPE)).containsExactly(
            FpHendelseUnderTyper.LOVBESTEMT_FERIE,
            FpHendelseUnderTyper.ARBEID_HELTID,
            FpHendelseUnderTyper.MOTTAKER_HELT_AVHENGIG,
            FpHendelseUnderTyper.MOTTAKER_INNLAGT,
            FpHendelseUnderTyper.BARN_INNLAGT
        );

        assertThat(mapAvResultat.get(HendelseType.FP_UTTAK_KVOTENE_TYPE)).containsExactly(
            FpHendelseUnderTyper.KVO_MOTTAKER_HELT_AVHENGIG,
            FpHendelseUnderTyper.KVO_MOTTAKER_INNLAGT
        );

        assertThat(mapAvResultat.get(HendelseType.FP_VILKAAR_GENERELLE_TYPE)).containsExactly(
            FpHendelseUnderTyper.MOR_IKKE_ARBEID,
            FpHendelseUnderTyper.MOR_IKKE_STUDERT,
            FpHendelseUnderTyper.MOR_IKKE_ARBEID_OG_STUDER,
            FpHendelseUnderTyper.MOR_IKKE_HELT_AVHENGIG,
            FpHendelseUnderTyper.MOR_IKKE_INNLAGT,
            FpHendelseUnderTyper.MOR_IKKE_I_IP,
            FpHendelseUnderTyper.MOR_IKKE_I_KP
        );

        assertThat(mapAvResultat.get(HendelseType.FP_ANNET_HENDELSE_TYPE)).containsExactly(
            FellesUndertyper.REFUSJON_ARBEIDSGIVER,
            FellesUndertyper.ANNET_FRITEKST
        );

        assertThat(mapAvResultat.get(HendelseType.FP_KUN_RETT_TYPE)).containsOnly(
            FpHendelseUnderTyper.FEIL_I_ANTALL_DAGER
        );

        assertThat(mapAvResultat.get(HendelseType.FP_UTTAK_ALENEOMSORG_TYPE)).containsOnly(
            FpHendelseUnderTyper.IKKE_ALENEOMSORG
        );

        assertThat(mapAvResultat.get(HendelseType.FP_UTTAK_GRADERT_TYPE)).containsOnly(
            FpHendelseUnderTyper.GRADERT_UTTAK
        );

    }

    @Test
    public void skal_ha_riktige_årsaker_og_underårsaker_for_svangerskapspenger() {
        Map<HendelseType, List<HendelseUnderType>> mapAvResultat = hentÅrsakerForYtelseType(FagsakYtelseType.SVANGERSKAPSPENGER);

        assertThat(mapAvResultat.keySet()).containsExactly(
            HendelseType.MEDLEMSKAP_TYPE,
            HendelseType.SVP_FAKTA_TYPE,
            HendelseType.SVP_ARBEIDSGIVERS_FORHOLD_TYPE,
            HendelseType.SVP_ARBEIDSFORHOLD_TYPE,
            HendelseType.SVP_OPPTJENING_TYPE,
            HendelseType.SVP_BEREGNING_TYPE,
            HendelseType.SVP_UTTAK_TYPE,
            HendelseType.SVP_OPPHØR,
            HendelseType.ØKONOMI_FEIL,
            HendelseType.SVP_ANNET_TYPE
        );

        assertThat(mapAvResultat.get(HendelseType.MEDLEMSKAP_TYPE)).containsExactly(
            MedlemskapHendelseUndertyper.UTVANDRET,
            MedlemskapHendelseUndertyper.IKKE_BOSATT,
            MedlemskapHendelseUndertyper.IKKE_OPPHOLDSRETT_EØS,
            MedlemskapHendelseUndertyper.IKKE_LOVLIG_OPPHOLD,
            MedlemskapHendelseUndertyper.MEDLEM_I_ANNET_LAND
        );

        assertThat(mapAvResultat.get(HendelseType.ØKONOMI_FEIL)).containsExactly(
            ØkonomiUndertyper.DOBBELTUTBETALING,
            ØkonomiUndertyper.FOR_MYE_UTBETALT,
            ØkonomiUndertyper.FEIL_TREKK,
            ØkonomiUndertyper.FEIL_FERIEPENGER
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_OPPHØR)).containsExactly(
            SvpHendelseUnderTyper.MOTTAKER_DØD,
            SvpHendelseUnderTyper.MOTTAKER_IKKE_GRAVID
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_FAKTA_TYPE)).containsExactly(
            SvpHendelseUnderTyper.SVP_ENDRING_TERMINDATO,
            SvpHendelseUnderTyper.SVP_TIDLIG_FODSEL,
            SvpHendelseUnderTyper.SVP_IKKE_HELSEFARLIG
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_ARBEIDSGIVERS_FORHOLD_TYPE)).containsExactly(
            SvpHendelseUnderTyper.SVP_TILRETTELEGGING_FULLT_MULIG,
            SvpHendelseUnderTyper.SVP_TILRETTELEGGING_DELVIS_MULIG
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_ARBEIDSFORHOLD_TYPE)).containsExactly(
            SvpHendelseUnderTyper.SVP_MANGLER_ARBEIDSFORHOLD
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_OPPTJENING_TYPE)).containsExactly(
            SvpHendelseUnderTyper.SVP_IKKE_ARBEID,
            SvpHendelseUnderTyper.SVP_INNTEKT_IKKE_TAP,
            SvpHendelseUnderTyper.SVP_INNTEKT_UNDER
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_BEREGNING_TYPE)).containsExactly(
            SvpHendelseUnderTyper.SVP_ENDRING_GRUNNLAG
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_UTTAK_TYPE)).containsExactly(
            SvpHendelseUnderTyper.SVP_ENDRING_PROSENT,
            SvpHendelseUnderTyper.SVP_ENDRING_PERIODE
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_ANNET_TYPE)).containsOnly(
            FellesUndertyper.REFUSJON_ARBEIDSGIVER,
            FellesUndertyper.ANNET_FRITEKST
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
