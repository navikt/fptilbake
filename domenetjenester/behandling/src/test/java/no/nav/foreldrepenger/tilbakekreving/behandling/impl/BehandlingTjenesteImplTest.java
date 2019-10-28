package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KonsekvensForYtelsen;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.BehandlingsresultatDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.BehandlingÅrsakDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMock;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMockUtil;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;

public class BehandlingTjenesteImplTest extends FellesTestOppsett {

    private static final LocalDate NOW = LocalDate.now();

    @Before
    public void setup() {
        when(mockFpsakKlient.hentTilbakekrevingValg(eksternBehandlingUuid)).thenReturn(Optional.of(new TilbakekrevingValgDto(VidereBehandling.TILBAKEKREV_I_INFOTRYGD)));
        when(mockFpsakKlient.hentBehandling(eksternBehandlingUuid)).thenReturn(Optional.of(lagEksternBehandlingsInfo()));
    }

    @Test
    public void skalReturnereTomFeilutbetalingFaktaNårGrunnlagIkkeFinnes() {
        Optional<BehandlingFeilutbetalingFakta> feilutbetalingFakta = behandlingTjeneste.hentBehandlingFeilutbetalingFakta(internBehandlingId);
        assertThat(feilutbetalingFakta).isEmpty();
    }

    @Test
    public void skal_hente_feilutbetalingfakta_når_varselBeløp_finnes_ikke() {
        KravgrunnlagMock mockMedFeilPostering = lagKravgrunnlag(FOM, TOM, KlasseType.FEIL, BigDecimal.valueOf(10000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = lagKravgrunnlag(FOM, TOM, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(10000));

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList(mockMedFeilPostering, mockMedYtelPostering));

        grunnlagRepository.lagre(internBehandlingId,kravgrunnlag431);

        Optional<BehandlingFeilutbetalingFakta> feilutbetalingFakta = behandlingTjeneste.hentBehandlingFeilutbetalingFakta(internBehandlingId);
        assertThat(feilutbetalingFakta).isNotEmpty();
        BehandlingFeilutbetalingFakta fakta = feilutbetalingFakta.get();
        fellesFaktaResponsSjekk(fakta);
        assertThat(fakta.getTidligereVarseltBeløp()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void skalHenteFeilutbetalingFaktaMedEnkelPeriode() {
        KravgrunnlagMock mockMedFeilPostering = lagKravgrunnlag(FOM, TOM, KlasseType.FEIL, BigDecimal.valueOf(10000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = lagKravgrunnlag(FOM, TOM, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(10000));

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList(mockMedFeilPostering, mockMedYtelPostering));

        grunnlagRepository.lagre(internBehandlingId,kravgrunnlag431);
        varselRepository.lagre(internBehandlingId,"hello",23000l);
        Optional<BehandlingFeilutbetalingFakta> feilutbetalingFakta = behandlingTjeneste.hentBehandlingFeilutbetalingFakta(internBehandlingId);

        assertThat(feilutbetalingFakta).isNotEmpty();
        BehandlingFeilutbetalingFakta fakta = feilutbetalingFakta.get();
        fellesFaktaResponsSjekk(fakta);
        assertThat(fakta.getAktuellFeilUtbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(fakta.getPerioder().get(0).getBelop()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(fakta.getTidligereVarseltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(23000));
        assertThat(fakta.getPerioder().get(0).getFom()).isEqualTo(FOM);
        assertThat(fakta.getPerioder().get(0).getTom()).isEqualTo(TOM);
    }

    @Test
    public void skalHenteFeilutbetalingFaktaMedFlerePerioder() {
        KravgrunnlagMock mockMedFeilPostering = lagKravgrunnlag(FOM, LocalDate.of(2016, 03, 31), KlasseType.FEIL,
            BigDecimal.valueOf(10000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedFeilPostering2 = lagKravgrunnlag(LocalDate.of(2016, 04, 01), LocalDate.of(2016, 04, 15),
            KlasseType.FEIL, BigDecimal.valueOf(12000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedFeilPostering3 = lagKravgrunnlag(LocalDate.of(2016, 04, 22), TOM,
            KlasseType.FEIL, BigDecimal.valueOf(15000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = lagKravgrunnlag(FOM, TOM, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(37000));

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList(mockMedFeilPostering, mockMedFeilPostering2,
            mockMedFeilPostering3, mockMedYtelPostering));

        grunnlagRepository.lagre(internBehandlingId,kravgrunnlag431);
        varselRepository.lagre(internBehandlingId,"hello",23000l);

        Optional<BehandlingFeilutbetalingFakta> feilutbetalingFakta = behandlingTjeneste.hentBehandlingFeilutbetalingFakta(internBehandlingId);

        assertThat(feilutbetalingFakta).isNotEmpty();
        BehandlingFeilutbetalingFakta fakta = feilutbetalingFakta.get();
        fellesFaktaResponsSjekk(fakta);
        assertThat(fakta.getAktuellFeilUtbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(37000));
        assertThat(fakta.getTidligereVarseltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(23000));
        assertThat(fakta.getPerioder().size()).isEqualTo(2);
        assertThat(fakta.getPerioder().get(0).getFom()).isEqualTo(FOM);
        assertThat(fakta.getPerioder().get(0).getTom()).isEqualTo(LocalDate.of(2016, 04, 15));
        assertThat(fakta.getPerioder().get(0).getBelop()).isEqualTo(BigDecimal.valueOf(22000));

        assertThat(fakta.getPerioder().get(1).getTom()).isEqualTo(TOM);
        assertThat(fakta.getPerioder().get(1).getFom()).isEqualTo(LocalDate.of(2016, 04, 22));
        assertThat(fakta.getPerioder().get(1).getBelop()).isEqualTo(BigDecimal.valueOf(15000));
    }

    @Test
    public void skalHenteFeilutbetalingFaktaMedFlerePerioderOgSisteDagIHelgen() {
        KravgrunnlagMock mockMedFeilPostering = lagKravgrunnlag(FOM, LocalDate.of(2016, 03, 26), KlasseType.FEIL,
            BigDecimal.valueOf(10000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedFeilPostering2 = lagKravgrunnlag(LocalDate.of(2016, 03, 28), LocalDate.of(2016, 04, 15),
            KlasseType.FEIL, BigDecimal.valueOf(12000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedFeilPostering3 = lagKravgrunnlag(LocalDate.of(2016, 04, 19), TOM,
            KlasseType.FEIL, BigDecimal.valueOf(15000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = lagKravgrunnlag(FOM, TOM, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(37000));

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList(mockMedFeilPostering, mockMedFeilPostering2,
            mockMedFeilPostering3, mockMedYtelPostering));

        grunnlagRepository.lagre(internBehandlingId,kravgrunnlag431);
        Optional<BehandlingFeilutbetalingFakta> feilutbetalingFakta = behandlingTjeneste.hentBehandlingFeilutbetalingFakta(internBehandlingId);

        assertThat(feilutbetalingFakta).isNotEmpty();
        BehandlingFeilutbetalingFakta fakta = feilutbetalingFakta.get();
        fellesFaktaResponsSjekk(fakta);
        assertThat(fakta.getAktuellFeilUtbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(37000));
        assertThat(fakta.getPerioder().size()).isEqualTo(2);
        assertThat(fakta.getPerioder().get(0).getFom()).isEqualTo(FOM);
        assertThat(fakta.getPerioder().get(0).getTom()).isEqualTo(LocalDate.of(2016, 04, 15));
        assertThat(fakta.getPerioder().get(0).getBelop()).isEqualTo(BigDecimal.valueOf(22000));

        assertThat(fakta.getPerioder().get(1).getTom()).isEqualTo(TOM);
        assertThat(fakta.getPerioder().get(1).getFom()).isEqualTo(LocalDate.of(2016, 04, 19));
        assertThat(fakta.getPerioder().get(1).getBelop()).isEqualTo(BigDecimal.valueOf(15000));
    }

    @Test
    public void skal_opprette_behandling_automatisk() {
        avsluttBehandling();
        Long behandlingId = behandlingTjeneste.opprettBehandlingAutomatisk(saksnummer, UUID.randomUUID(), eksternBehandlingId, aktørId, FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING);
        fellesBehandlingAssert(behandlingId);
    }


    @Test
    public void skal_opprette_behandling_automatisk_med_allerede_åpen_behandling() {
        expectedException.expectMessage("FPT-663486");
        behandlingTjeneste.opprettBehandlingAutomatisk(saksnummer, eksternBehandlingUuid, eksternBehandlingId, aktørId, FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING);
    }

    @Test
    public void skal_opprette_behandling_manell() {
        avsluttBehandling();
        Long behandlingId = behandlingTjeneste.opprettBehandlingManuell(saksnummer, UUID.randomUUID(), FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING);
        fellesBehandlingAssert(behandlingId);
    }

    @Test
    public void skal_opprette_behandling_manell_med_allerede_åpen_behandling() {
        expectedException.expectMessage("FPT-663486");
        behandlingTjeneste.opprettBehandlingManuell(saksnummer, eksternBehandlingUuid, FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING);
    }

    @Test
    public void skal_opprette_behandling_manell_med_allerede_åpen_revurdeing_behandling() {
        UUID eksternUUID = UUID.randomUUID();
        avsluttBehandling();
        revurderingTjeneste.opprettRevurdering(saksnummer, eksternBehandlingUuid, BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR, BehandlingType.REVURDERING_TILBAKEKREVING);

        Long behandlingId = behandlingTjeneste.opprettBehandlingManuell(saksnummer, eksternUUID, FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING);
        fellesBehandlingAssert(behandlingId);
    }

    @Test
    public void skal_opprette_behandling_manell_med_allerede_avsluttet_behandling_med_samme_fpsak_revurdering() {
        avsluttBehandling();
        expectedException.expectMessage("FPT-663488");

        behandlingTjeneste.opprettBehandlingManuell(saksnummer, eksternBehandlingUuid, FagsakYtelseType.FORELDREPENGER, BehandlingType.TILBAKEKREVING);
    }

    @Test
    public void kan_opprette_behandling_med_åpen_behandling_finnes() {
        boolean result = behandlingTjeneste.kanOppretteBehandling(saksnummer, eksternBehandlingUuid);
        assertThat(result).isFalse();
    }

    @Test
    public void kan_opprette_behandling_med_allerede_avsluttet_behandling_med_samme_fpsak_revurdering() {
        avsluttBehandling();

        boolean result = behandlingTjeneste.kanOppretteBehandling(saksnummer, eksternBehandlingUuid);
        assertThat(result).isFalse();
    }

    @Test
    public void kan_opprette_behandling() {
        avsluttBehandling();

        boolean result = behandlingTjeneste.kanOppretteBehandling(saksnummer, UUID.randomUUID());
        assertThat(result).isTrue();
    }

    @Test
    public void skal_oppdatere_behandling_medEksternReferanse() {
        UUID eksternUuid = testUtility.genererEksternUuid();
        long eksternBehandlingId = 5l;
        behandlingTjeneste.oppdaterBehandlingMedEksternReferanse(saksnummer,eksternBehandlingId, eksternUuid);

        EksternBehandling eksternBehandling = repoProvider.getEksternBehandlingRepository().hentFraInternId(behandling.getId());
        assertThat(eksternBehandling.getEksternUuid()).isEqualByComparingTo(eksternUuid);
        assertThat(eksternBehandling.getEksternId()).isEqualByComparingTo(eksternBehandlingId);
    }

    @Test
    public void skal_oppdatere_behandling_medEksternReferanse_med_ugyldig_saksnummer() {
        UUID eksternUuid = testUtility.genererEksternUuid();
        expectedException.expectMessage("FPT-663490");

        behandlingTjeneste.oppdaterBehandlingMedEksternReferanse(new Saksnummer("1233434"),5l,  eksternUuid);
    }

    private void avsluttBehandling() {
        behandling.avsluttBehandling();
        BehandlingLås behandlingLås = repoProvider.getBehandlingRepository().taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
    }


    private KravgrunnlagMock lagKravgrunnlag(LocalDate fom, LocalDate tom, KlasseType klasseType, BigDecimal nyBeløp, BigDecimal tilbakeBeløp) {
        return new KravgrunnlagMock(fom, tom, klasseType, nyBeløp, tilbakeBeløp);
    }

    private void fellesFaktaResponsSjekk(BehandlingFeilutbetalingFakta fakta) {
        assertThat(fakta.getTotalPeriodeFom()).isEqualTo(FOM);
        assertThat(fakta.getTotalPeriodeTom()).isEqualTo(TOM);
        assertThat(fakta.getPerioder()).isNotEmpty();
        assertThat(fakta.getDatoForRevurderingsvedtak()).isEqualTo(NOW);
        assertThat(fakta.getTilbakekrevingValg().getVidereBehandling()).isEqualToComparingFieldByField(VidereBehandling.TILBAKEKREV_I_INFOTRYGD);
        assertThat(fakta.getBehandlingsresultat().getType()).isEqualByComparingTo(BehandlingResultatType.OPPHØR);
        assertThat(fakta.getBehandlingsresultat().getKonsekvenserForYtelsen()).contains(KonsekvensForYtelsen.ENDRING_I_BEREGNING);
        assertThat(fakta.getBehandlingÅrsaker().size()).isEqualTo(1);
    }

    private void fellesBehandlingAssert(Long behandlingId) {
        assertThat(behandlingId).isNotNull();
        Behandling behandling = behandlingTjeneste.hentBehandling(behandlingId);
        assertThat(behandling).isNotNull();
        assertThat(behandling.getFagsak().getFagsakYtelseType()).isEqualByComparingTo(FagsakYtelseType.FORELDREPENGER);
        assertThat(behandling.getType()).isEqualByComparingTo(BehandlingType.TILBAKEKREVING);
        assertThat(behandling.getBehandlendeEnhetId()).isNotEmpty();
        assertThat(behandling.getBehandlendeEnhetNavn()).isNotEmpty();
    }

    private EksternBehandlingsinfoDto lagEksternBehandlingsInfo() {
        EksternBehandlingsinfoDto eksternBehandlingsinfo = new EksternBehandlingsinfoDto();
        eksternBehandlingsinfo.setSprakkode(Språkkode.nb);
        eksternBehandlingsinfo.setUuid(eksternBehandlingUuid);
        eksternBehandlingsinfo.setVedtakDato(NOW);

        BehandlingsresultatDto behandlingsresultatDto = new BehandlingsresultatDto();
        behandlingsresultatDto.setType(BehandlingResultatType.OPPHØR);
        behandlingsresultatDto.setKonsekvenserForYtelsen(Lists.newArrayList(KonsekvensForYtelsen.ENDRING_I_BEREGNING, KonsekvensForYtelsen.FORELDREPENGER_OPPHØRER));
        eksternBehandlingsinfo.setBehandlingsresultat(behandlingsresultatDto);

        BehandlingÅrsakDto behandlingÅrsakDto = new BehandlingÅrsakDto();
        behandlingÅrsakDto.setBehandlingÅrsakType(BehandlingÅrsakType.RE_KLAGE_KA);
        eksternBehandlingsinfo.setBehandlingÅrsaker(Lists.newArrayList(behandlingÅrsakDto));
        return eksternBehandlingsinfo;
    }

}
