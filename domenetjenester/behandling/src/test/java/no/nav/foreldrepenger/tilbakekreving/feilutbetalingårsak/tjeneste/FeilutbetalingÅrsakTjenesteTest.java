package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.FellesUndertyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.FpHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.MedlemskapHendelseUndertyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.SvpHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.ØkonomiUndertyper;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.FeiltubetalingÅrsakerYtelseTypeDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.FeilutbetalingÅrsakDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.UnderÅrsakDto;

public class FeilutbetalingÅrsakTjenesteTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private FeilutbetalingÅrsakTjeneste feilutbetalingÅrsakTjeneste = new FeilutbetalingÅrsakTjeneste(repositoryProvider.getKodeverkRepository());

    @Test
    public void skal_ha_riktige_årsaker_og_underårsaker_for_foreldrepenger() {
        List<FeilutbetalingÅrsakDto> feilutbetalingÅrsaker = hentÅrsakerForYtelseType(FagsakYtelseType.FORELDREPENGER);

        Map<String, List<String>> mapAvResultat = feilutbetalingÅrsaker.stream().collect(Collectors.toMap(
            FeilutbetalingÅrsakDto::getÅrsakKode,
            dto -> dto.getUnderÅrsaker().stream().map(UnderÅrsakDto::getUnderÅrsakKode).collect(Collectors.toList())
        ));

        assertThat(mapAvResultat.keySet()).containsOnly(
            HendelseType.MEDLEMSKAP_TYPE.getKode(),
            HendelseType.ØKONOMI_FEIL.getKode(),
            HendelseType.FP_OPPTJENING_TYPE.getKode(),
            HendelseType.FP_BEREGNING_TYPE.getKode(),
            HendelseType.FP_STONADSPERIODEN_TYPE.getKode(),
            HendelseType.FP_UTTAK_GENERELT_TYPE.getKode(),
            HendelseType.FP_UTTAK_UTSETTELSE_TYPE.getKode(),
            HendelseType.FP_UTTAK_KVOTENE_TYPE.getKode(),
            HendelseType.FP_VILKAAR_GENERELLE_TYPE.getKode(),
            HendelseType.FP_KUN_RETT_TYPE.getKode(),
            HendelseType.FP_UTTAK_ALENEOMSORG_TYPE.getKode(),
            HendelseType.FP_UTTAK_GRADERT_TYPE.getKode(),
            HendelseType.FP_ANNET_HENDELSE_TYPE.getKode()
        );
        assertThat(mapAvResultat.get(HendelseType.ØKONOMI_FEIL.getKode())).containsExactly(
            ØkonomiUndertyper.DOBBELTUTBETALING.getKode(),
            ØkonomiUndertyper.FOR_MYE_UTBETALT.getKode(),
            ØkonomiUndertyper.FEIL_TREKK.getKode(),
            ØkonomiUndertyper.FEIL_FERIEPENGER.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.MEDLEMSKAP_TYPE.getKode())).containsExactly(
            MedlemskapHendelseUndertyper.UTVANDRET.getKode(),
            MedlemskapHendelseUndertyper.IKKE_BOSATT.getKode(),
            MedlemskapHendelseUndertyper.IKKE_OPPHOLDSRETT_EØS.getKode(),
            MedlemskapHendelseUndertyper.IKKE_LOVLIG_OPPHOLD.getKode(),
            MedlemskapHendelseUndertyper.MEDLEM_I_ANNET_LAND.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.FP_OPPTJENING_TYPE.getKode())).containsExactly(
            FpHendelseUnderTyper.IKKE_INNTEKT.getKode(),
            FpHendelseUnderTyper.IKKE_YRKESAKTIV.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.FP_BEREGNING_TYPE.getKode())).containsExactly(
            FpHendelseUnderTyper.ENDRING_GRUNNLAG.getKode(),
            FpHendelseUnderTyper.INNTEKT_UNDER.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.FP_STONADSPERIODEN_TYPE.getKode())).containsExactly(
            FpHendelseUnderTyper.ENDRET_DEKNINGSGRAD.getKode(),
            FpHendelseUnderTyper.FEIL_FLERBARNSDAGER.getKode(),
            FpHendelseUnderTyper.OPPHOR_BARN_DOD.getKode(),
            FpHendelseUnderTyper.OPPHOR_MOTTAKER_DOD.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.FP_UTTAK_GENERELT_TYPE.getKode())).containsExactly(
            FpHendelseUnderTyper.STONADSPERIODE_OVER_3.getKode(),
            FpHendelseUnderTyper.NY_STONADSPERIODE.getKode(),
            FpHendelseUnderTyper.IKKE_OMSORG.getKode(),
            FpHendelseUnderTyper.MOTTAKER_I_ARBEID.getKode(),
            FpHendelseUnderTyper.FORELDRES_UTTAK.getKode(),
            FpHendelseUnderTyper.STONADSPERIODE_MANGEL.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.FP_UTTAK_UTSETTELSE_TYPE.getKode())).containsExactly(
            FpHendelseUnderTyper.LOVBESTEMT_FERIE.getKode(),
            FpHendelseUnderTyper.ARBEID_HELTID.getKode(),
            FpHendelseUnderTyper.MOTTAKER_HELT_AVHENGIG.getKode(),
            FpHendelseUnderTyper.MOTTAKER_INNLAGT.getKode(),
            FpHendelseUnderTyper.BARN_INNLAGT.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.FP_UTTAK_KVOTENE_TYPE.getKode())).containsExactly(
            FpHendelseUnderTyper.KVO_MOTTAKER_HELT_AVHENGIG.getKode(),
            FpHendelseUnderTyper.KVO_MOTTAKER_INNLAGT.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.FP_VILKAAR_GENERELLE_TYPE.getKode())).containsExactly(
            FpHendelseUnderTyper.MOR_IKKE_ARBEID.getKode(),
            FpHendelseUnderTyper.MOR_IKKE_STUDERT.getKode(),
            FpHendelseUnderTyper.MOR_IKKE_ARBEID_OG_STUDER.getKode(),
            FpHendelseUnderTyper.MOR_IKKE_HELT_AVHENGIG.getKode(),
            FpHendelseUnderTyper.MOR_IKKE_INNLAGT.getKode(),
            FpHendelseUnderTyper.MOR_IKKE_I_IP.getKode(),
            FpHendelseUnderTyper.MOR_IKKE_I_KP.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.FP_ANNET_HENDELSE_TYPE.getKode())).containsExactly(
            FellesUndertyper.REFUSJON_ARBEIDSGIVER.getKode(),
            FellesUndertyper.ANNET_FRITEKST.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.FP_KUN_RETT_TYPE.getKode())).containsOnly(
            FpHendelseUnderTyper.FEIL_I_ANTALL_DAGER.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.FP_UTTAK_ALENEOMSORG_TYPE.getKode())).containsOnly(
            FpHendelseUnderTyper.IKKE_ALENEOMSORG.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.FP_UTTAK_GRADERT_TYPE.getKode())).containsOnly(
            FpHendelseUnderTyper.GRADERT_UTTAK.getKode()
        );

    }

    @Test
    public void skal_ha_riktige_årsaker_og_underårsaker_for_svangerskapspenger() {
        List<FeilutbetalingÅrsakDto> feilutbetalingÅrsaker = hentÅrsakerForYtelseType(FagsakYtelseType.SVANGERSKAPSPENGER);

        Map<String, List<String>> mapAvResultat = feilutbetalingÅrsaker.stream().collect(Collectors.toMap(
            FeilutbetalingÅrsakDto::getÅrsakKode,
            dto -> dto.getUnderÅrsaker().stream().map(UnderÅrsakDto::getUnderÅrsakKode).collect(Collectors.toList())
        ));

        assertThat(mapAvResultat.keySet()).containsOnly(
            HendelseType.MEDLEMSKAP_TYPE.getKode(),
            HendelseType.ØKONOMI_FEIL.getKode(),
            HendelseType.SVP_OPPHØR.getKode(),
            HendelseType.SVP_FAKTA_TYPE.getKode(),
            HendelseType.SVP_ARBEIDSGIVERS_FORHOLD_TYPE.getKode(),
            HendelseType.SVP_ARBEIDSFORHOLD_TYPE.getKode(),
            HendelseType.SVP_OPPTJENING_TYPE.getKode(),
            HendelseType.SVP_BEREGNING_TYPE.getKode(),
            HendelseType.SVP_UTTAK_TYPE.getKode(),
            HendelseType.SVP_INNTEKT_TYPE.getKode(),
            HendelseType.SVP_ANNET_TYPE.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.MEDLEMSKAP_TYPE.getKode())).containsExactly(
            MedlemskapHendelseUndertyper.UTVANDRET.getKode(),
            MedlemskapHendelseUndertyper.IKKE_BOSATT.getKode(),
            MedlemskapHendelseUndertyper.IKKE_OPPHOLDSRETT_EØS.getKode(),
            MedlemskapHendelseUndertyper.IKKE_LOVLIG_OPPHOLD.getKode(),
            MedlemskapHendelseUndertyper.MEDLEM_I_ANNET_LAND.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.ØKONOMI_FEIL.getKode())).containsExactly(
            ØkonomiUndertyper.DOBBELTUTBETALING.getKode(),
            ØkonomiUndertyper.FOR_MYE_UTBETALT.getKode(),
            ØkonomiUndertyper.FEIL_TREKK.getKode(),
            ØkonomiUndertyper.FEIL_FERIEPENGER.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_OPPHØR.getKode())).containsExactly(
            SvpHendelseUnderTyper.MOTTAKER_DØD.getKode(),
            SvpHendelseUnderTyper.MOTTAKER_IKKE_GRAVID.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_FAKTA_TYPE.getKode())).containsExactly(
            SvpHendelseUnderTyper.SVP_ENDRING_TERMINDATO.getKode(),
            SvpHendelseUnderTyper.SVP_TIDLIG_FODSEL.getKode(),
            SvpHendelseUnderTyper.SVP_IKKE_HELSEFARLIG.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_ARBEIDSGIVERS_FORHOLD_TYPE.getKode())).containsExactly(
            SvpHendelseUnderTyper.SVP_TILRETTELEGGING_FULLT_MULIG.getKode(),
            SvpHendelseUnderTyper.SVP_TILRETTELEGGING_DELVIS_MULIG.getKode(),
            SvpHendelseUnderTyper.SVP_TILRETTELEGGING_IKKE_MULIG.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_ARBEIDSFORHOLD_TYPE.getKode())).containsExactly(
            SvpHendelseUnderTyper.SVP_MANGLER_ARBEIDSFORHOLD.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_OPPTJENING_TYPE.getKode())).containsExactly(
            SvpHendelseUnderTyper.SVP_IKKE_ARBEID.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_BEREGNING_TYPE.getKode())).containsExactly(
            SvpHendelseUnderTyper.SVP_ENDRING_GRUNNLAG.getKode(),
            SvpHendelseUnderTyper.SVP_INNTEKT_UNDER.getKode()

        );

        assertThat(mapAvResultat.get(HendelseType.SVP_UTTAK_TYPE.getKode())).containsExactly(
            SvpHendelseUnderTyper.SVP_ENDRING_PROSENT.getKode(),
            SvpHendelseUnderTyper.SVP_ENDRING_PERIODE.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_INNTEKT_TYPE.getKode())).containsOnly(
            SvpHendelseUnderTyper.SVP_INNTEKT_IKKE_TAP.getKode()
        );

        assertThat(mapAvResultat.get(HendelseType.SVP_ANNET_TYPE.getKode())).containsOnly(
            FellesUndertyper.REFUSJON_ARBEIDSGIVER.getKode(),
            FellesUndertyper.ANNET_FRITEKST.getKode()
        );
    }

    private List<FeilutbetalingÅrsakDto> hentÅrsakerForYtelseType(FagsakYtelseType fagsakYtelseType) {
        return feilutbetalingÅrsakTjeneste.hentFeilutbetalingårsaker()
            .stream()
            .filter(v -> v.getYtelseType().equals(fagsakYtelseType.getKode()))
            .map(FeiltubetalingÅrsakerYtelseTypeDto::getFeilutbetalingÅrsaker)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Ukjent ytelseType:" + fagsakYtelseType));
    }
}
