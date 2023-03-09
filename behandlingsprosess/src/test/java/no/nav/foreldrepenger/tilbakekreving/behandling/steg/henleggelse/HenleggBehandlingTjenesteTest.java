package no.nav.foreldrepenger.tilbakekreving.behandling.steg.henleggelse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.FlushModeType;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMock;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMockUtil;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;


class HenleggBehandlingTjenesteTest extends FellesTestOppsett {


    private BrevSporingRepository brevSporingRepository;

    private HenleggBehandlingTjeneste henleggBehandlingTjeneste;

    private final BehandlingResultatType behandlingsresultat = BehandlingResultatType.HENLAGT_FEILOPPRETTET;

    @BeforeEach
    void setUp() {
        entityManager.setFlushMode(FlushModeType.AUTO);

        brevSporingRepository = repoProvider.getBrevSporingRepository();

        behandlingskontrollTjeneste = new BehandlingskontrollTjeneste(new BehandlingskontrollServiceProvider(entityManager, new BehandlingModellRepository(), null));
        HistorikkinnslagTjeneste historikkinnslagTjeneste = new HistorikkinnslagTjeneste(historikkRepository, null);
        henleggBehandlingTjeneste = new HenleggBehandlingTjeneste(repoProvider, taskTjeneste,
                behandlingskontrollTjeneste, historikkinnslagTjeneste);
    }

    @Test
    void skal_henlegge_behandling_uten_brev() {
        henleggBehandlingTjeneste.henleggBehandling(internBehandlingId, behandlingsresultat);

        assertHenleggelse(internBehandlingId);
    }

    @Test
    void skal_henlegge_behandling_med_aksjonspunkt() {
        var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        var aksjonspunkt = behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(kontekst, List.of(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING)).get(0);
        Assertions.assertThat(aksjonspunkt.getStatus()).isEqualTo(AksjonspunktStatus.OPPRETTET);

        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);

        assertHenleggelse(internBehandlingId);
        Assertions.assertThat(aksjonspunkt.getStatus()).isEqualTo(AksjonspunktStatus.AVBRUTT);
    }

    @Test
    void skal_ikke_henlegge_behandling_manuelt_når_grunnlag_finnes() {
        lagKravgrunnlag(behandling.getId());

        assertThatThrownBy(
                () -> henleggBehandlingTjeneste.henleggBehandlingManuelt(behandling.getId(), behandlingsresultat, "", ""))
                .isInstanceOf(FunksjonellException.class)
                .hasMessageContaining("FPT-663491");
    }

    @Test
    void kan_ikke_henlegge_behandling_når_behandling_opprettes_nå() {
        assertFalse(henleggBehandlingTjeneste.kanHenleggeBehandlingManuelt(behandling));
    }

    @Test
    void kan_henlegge_behandling_når_behandling_opprettes_før_bestemte_dager() {
        Behandling behandling = mock(Behandling.class);
        when(behandling.getOpprettetTidspunkt()).thenReturn(LocalDateTime.now().minusDays(8l));
        when(behandling.getType()).thenReturn(BehandlingType.TILBAKEKREVING);
        assertTrue(henleggBehandlingTjeneste.kanHenleggeBehandlingManuelt(behandling));
    }

    @Test
    void kan_henlegge_tilbakekreving_revurdering_med_grunnlag() {
        Long revurderingBehandlingId = opprettTilbakekrevingRevurdering();
        lagKravgrunnlag(revurderingBehandlingId);

        henleggBehandlingTjeneste.henleggBehandlingManuelt(revurderingBehandlingId,
                BehandlingResultatType.HENLAGT_FEILOPPRETTET, "hello all", "hello all");

        Assertions.assertThat(historikkRepository.hentHistorikk(revurderingBehandlingId)).isNotEmpty();
        Assertions.assertThat(behandlingRepository.hentBehandling(revurderingBehandlingId).getStatus()).isEqualByComparingTo(
                BehandlingStatus.AVSLUTTET);
        Assertions.assertThat(
                repoProvider.getEksternBehandlingRepository().hentOptionalFraInternId(revurderingBehandlingId)).isEmpty();
    }

    @Test
    void kan_henlegge_behandling_som_er_satt_på_vent() {
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL);
        behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING,
                BehandlingStegType.VARSEL, LocalDateTime.now(), Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);

        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);
        assertHenleggelse(internBehandlingId);
    }

    @Test
    void kan_henlegge_behandling_der_vedtak_er_foreslått() {
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FORESLÅ_VEDTAK);
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);

        assertHenleggelse(internBehandlingId);
    }

    @Test
    void kan_ikke_henlegge_behandling_der_vedtak_er_fattet() {
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.IVERKSETT_VEDTAK);

        assertThatThrownBy(
                () -> henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat))
                .isInstanceOf(TekniskException.class)
                .hasMessageContaining("FPT-143308");
    }

    @Test
    void kan_ikke_henlegge_behandling_som_allerede_er_henlagt() {
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);
        // forsøker å henlegge behandlingen igjen
        assertThatThrownBy(
                () -> henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat))
                .isInstanceOf(TekniskException.class)
                .hasMessageContaining("FPT-143308");
    }

    @Test
    void kan_ikke_sende_henleggelsesbrev_hvis_varselbrev_ikke_sendt() {
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);

        Mockito.verifyNoInteractions(taskTjeneste);
        assertHenleggelse(internBehandlingId);
    }

    @Test
    void kan_ikke_sende_henleggelsesbrev_for_tilbakekreving_revurdering_med_henlagt_feilopprettet_uten_brev() {
        Long revuderingBehandlingId = opprettTilbakekrevingRevurdering();
        henleggBehandlingTjeneste.henleggBehandling(revuderingBehandlingId,
                BehandlingResultatType.HENLAGT_FEILOPPRETTET_UTEN_BREV);

        Mockito.verifyNoInteractions(taskTjeneste);
        assertHenleggelse(revuderingBehandlingId);
    }

    @Test
    void kan_sende_henleggelsesbrev_hvis_varselbrev_er_sendt() {
        JournalpostId journalpostId = new JournalpostId("123");
        BrevSporing henleggelsesBrevsporing = new BrevSporing.Builder().medBehandlingId(internBehandlingId)
                .medJournalpostId(journalpostId)
                .medDokumentId("123")
                .medBrevType(BrevType.VARSEL_BREV)
                .build();
        brevSporingRepository.lagre(henleggelsesBrevsporing);

        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);
        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(2)).lagre(captor.capture());
        var prosessTaskData = captor.getAllValues();
        assertThat(prosessTaskData).isNotEmpty();
        assertThat(prosessTaskData.get(0).taskType()).isEqualTo(HenleggBehandlingTjeneste.HENLEGGELSESBREV_TASK_TYPE);
        assertThat(prosessTaskData.get(1).taskType()).isEqualTo(HenleggBehandlingTjeneste.SELVBETJENING_HENLAGT_TASKTYPE);
        assertHenleggelse(internBehandlingId);
    }

    @Test
    void kan_sende_henleggelsesbrev_for_tilbakekreving_revurdering_med_henlagt_feilopprettet_med_brev() {
        Long revuderingBehandlingId = opprettTilbakekrevingRevurdering();
        henleggBehandlingTjeneste.henleggBehandling(revuderingBehandlingId,
                BehandlingResultatType.HENLAGT_FEILOPPRETTET_MED_BREV);

        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        var prosessTaskData = captor.getAllValues();
        assertThat(prosessTaskData).isNotEmpty();
        assertThat(prosessTaskData.get(0).taskType()).isEqualTo(HenleggBehandlingTjeneste.HENLEGGELSESBREV_TASK_TYPE);
        assertHenleggelse(revuderingBehandlingId);
    }

    @Test
    void kan_sende_henleggelsesbrev_for_tilbakekreving_revurdering_med_henlagt_feilopprettet_med_brev_når_varsel_er_sendt() {
        Long revuderingBehandlingId = opprettTilbakekrevingRevurdering();
        JournalpostId journalpostId = new JournalpostId("123");
        BrevSporing henleggelsesBrevsporing = new BrevSporing.Builder().medBehandlingId(revuderingBehandlingId)
                .medJournalpostId(journalpostId)
                .medDokumentId("123")
                .medBrevType(BrevType.VARSEL_BREV)
                .build();
        brevSporingRepository.lagre(henleggelsesBrevsporing);

        henleggBehandlingTjeneste.henleggBehandling(revuderingBehandlingId,
                BehandlingResultatType.HENLAGT_FEILOPPRETTET_MED_BREV);
        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(2)).lagre(captor.capture());
        var prosessTaskData = captor.getAllValues();
        assertThat(prosessTaskData).isNotEmpty();
        assertThat(prosessTaskData.get(0).taskType()).isEqualTo(HenleggBehandlingTjeneste.HENLEGGELSESBREV_TASK_TYPE);
        assertThat(prosessTaskData.get(1).taskType()).isEqualTo(HenleggBehandlingTjeneste.SELVBETJENING_HENLAGT_TASKTYPE);
        assertHenleggelse(revuderingBehandlingId);
    }

    private void lagKravgrunnlag(long behandlingId) {
        KravgrunnlagMock mockMedFeilPostering = new KravgrunnlagMock(FellesTestOppsett.FOM, LocalDate.of(2016, 3, 31), KlasseType.FEIL,
                BigDecimal.valueOf(11000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = new KravgrunnlagMock(FellesTestOppsett.FOM, LocalDate.of(2016, 3, 31), KlasseType.YTEL,
                BigDecimal.ZERO, BigDecimal.valueOf(11000));
        mockMedYtelPostering.setKlasseKode(KlasseKode.FPADATAL);

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(
                Lists.newArrayList(mockMedFeilPostering, mockMedYtelPostering));
        grunnlagRepository.lagre(behandlingId, kravgrunnlag431);
    }

    private Long opprettTilbakekrevingRevurdering() {
        behandling.avsluttBehandling();
        Behandling revurdering = revurderingTjeneste.opprettRevurdering(behandling.getId(),
                BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR);
        return revurdering.getId();
    }

    private void assertHenleggelse(Long behandlingId) {
        Assertions.assertThat(historikkRepository.hentHistorikk(behandlingId)).isNotEmpty();
        Assertions.assertThat(behandlingRepository.hentBehandling(behandlingId).getStatus()).isEqualByComparingTo(
                BehandlingStatus.AVSLUTTET);
        Assertions.assertThat(repoProvider.getEksternBehandlingRepository().hentOptionalFraInternId(behandlingId)).isEmpty();
    }
}
