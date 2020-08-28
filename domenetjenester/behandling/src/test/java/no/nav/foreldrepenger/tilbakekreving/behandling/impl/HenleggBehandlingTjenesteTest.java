package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.FlushModeType;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandlingImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.KlasseKode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporing;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevSporingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMock;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMockUtil;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;


public class HenleggBehandlingTjenesteTest extends FellesTestOppsett {


    private BehandlingModellRepository mockBehandlingModellRepository = mock(BehandlingModellRepository.class);
    private BehandlingModell modell = mock(BehandlingModell.class);

    private AksjonspunktRepository aksjonspunktRepository;

    private InternalManipulerBehandling manipulerInternBehandling;

    private BrevSporingRepository brevSporingRepository;

    private HistorikkinnslagTjeneste historikkinnslagTjeneste;
    private HenleggBehandlingTjeneste henleggBehandlingTjeneste;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    private BehandlingResultatType behandlingsresultat = BehandlingResultatType.HENLAGT_FEILOPPRETTET;

    @Before
    public void setUp() {
        repoRule.getEntityManager().setFlushMode(FlushModeType.AUTO);
        when(mockBehandlingModellRepository.getBehandlingStegKonfigurasjon()).thenReturn(BehandlingStegKonfigurasjon.lagDummy());
        when(mockBehandlingModellRepository.getModell(any())).thenReturn(modell);
        when(modell.erStegAFørStegB(any(), any())).thenReturn(true);

        aksjonspunktRepository = repoProvider.getAksjonspunktRepository();
        brevSporingRepository = repoProvider.getBrevSporingRepository();

        manipulerInternBehandling = new InternalManipulerBehandlingImpl(repoProvider);
        behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repoProvider, mockBehandlingModellRepository, null);
        historikkinnslagTjeneste = new HistorikkinnslagTjeneste(historikkRepository, mock(PersoninfoAdapter.class));
        henleggBehandlingTjeneste = new HenleggBehandlingTjeneste(repoProvider, prosessTaskRepository, behandlingskontrollTjeneste, historikkinnslagTjeneste);
    }

    @Test
    public void skal_henlegge_behandling_uten_brev() {
        henleggBehandlingTjeneste.henleggBehandling(internBehandlingId, behandlingsresultat);

        assertHenleggelse(internBehandlingId);
    }

    @Test
    public void skal_henlegge_behandling_med_aksjonspunkt() {
        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING);
        assertThat(aksjonspunkt.getStatus()).isEqualTo(AksjonspunktStatus.OPPRETTET);

        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);

        assertHenleggelse(internBehandlingId);
        assertThat(aksjonspunkt.getStatus()).isEqualTo(AksjonspunktStatus.AVBRUTT);
    }

    @Test
    public void skal_ikke_henlegge_behandling_manuelt_når_grunnlag_finnes() {
        lagKravgrunnlag(behandling.getId());

        expectedException.expect(FunksjonellException.class);
        expectedException.expectMessage("FPT-663491");

        henleggBehandlingTjeneste.henleggBehandlingManuelt(behandling.getId(), behandlingsresultat, "", "");
    }

    @Test
    public void kan_henlegge_tilbakekreving_revurdering_med_grunnlag(){
        Long revurderingBehandlingId = opprettTilbakekrevingRevurdering();
        lagKravgrunnlag(revurderingBehandlingId);

        henleggBehandlingTjeneste.henleggBehandlingManuelt(revurderingBehandlingId,BehandlingResultatType.HENLAGT_FEILOPPRETTET,"hello all"
            ,"hello all");

        assertThat(historikkRepository.hentHistorikk(revurderingBehandlingId)).isNotEmpty();
        assertThat(behandlingRepository.hentBehandling(revurderingBehandlingId).getStatus()).isEqualByComparingTo(BehandlingStatus.AVSLUTTET);
        assertThat(repoProvider.getEksternBehandlingRepository().hentOptionalFraInternId(revurderingBehandlingId)).isEmpty();
    }

    @Test
    public void kan_henlegge_behandling_som_er_satt_på_vent() {
        AksjonspunktDefinisjon def = AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING;
        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, def);
        aksjonspunktRepository.setFrist(aksjonspunkt, LocalDateTime.now(), Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);

        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL);

        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);
        assertHenleggelse(internBehandlingId);
    }

    @Test
    public void kan_henlegge_behandling_der_vedtak_er_foreslått() {
        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FORESLÅ_VEDTAK);
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);

        assertHenleggelse(internBehandlingId);
    }

    @Test
    public void kan_ikke_henlegge_behandling_der_vedtak_er_fattet() {
        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.IVERKSETT_VEDTAK);

        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FPT-143308");

        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);
    }

    @Test
    public void kan_ikke_henlegge_behandling_som_allerede_er_henlagt() {
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FPT-143308");

        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);
        // forsøker å henlegge behandlingen igjen
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);
    }

    @Test
    public void kan_ikke_sende_henleggelsesbrev_hvis_varselbrev_ikke_sendt() {
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);

        List<ProsessTaskData> prosessTaskData = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTaskData).isEmpty();
        assertHenleggelse(internBehandlingId);
    }

    @Test
    public void kan_ikke_sende_henleggelsesbrev_for_tilbakekreving_revurdering_med_henlagt_feilopprettet_uten_brev() {
        Long revuderingBehandlingId = opprettTilbakekrevingRevurdering();
        henleggBehandlingTjeneste.henleggBehandling(revuderingBehandlingId, BehandlingResultatType.HENLAGT_FEILOPPRETTET_UTEN_BREV);

        List<ProsessTaskData> prosessTaskData = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTaskData).isEmpty();
        assertHenleggelse(revuderingBehandlingId);
    }

    @Test
    public void kan_sende_henleggelsesbrev_hvis_varselbrev_er_sendt() {
        JournalpostId journalpostId = new JournalpostId("123");
        BrevSporing henleggelsesBrevsporing = new BrevSporing.Builder()
            .medBehandlingId(internBehandlingId)
            .medJournalpostId(journalpostId)
            .medDokumentId("123")
            .medBrevType(BrevType.VARSEL_BREV).build();
        brevSporingRepository.lagre(henleggelsesBrevsporing);

        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);
        List<ProsessTaskData> prosessTaskData = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTaskData).isNotEmpty();
        assertThat(prosessTaskData.get(0).getTaskType()).isEqualTo("brev.sendhenleggelse");
        assertThat(prosessTaskData.get(1).getTaskType()).isEqualTo("send.beskjed.tilbakekreving.henlagt.selvbetjening");
        assertHenleggelse(internBehandlingId);
    }

    @Test
    public void kan_sende_henleggelsesbrev_for_tilbakekreving_revurdering_med_henlagt_feilopprettet_med_brev() {
        Long revuderingBehandlingId = opprettTilbakekrevingRevurdering();
        henleggBehandlingTjeneste.henleggBehandling(revuderingBehandlingId, BehandlingResultatType.HENLAGT_FEILOPPRETTET_MED_BREV);

        List<ProsessTaskData> prosessTaskData = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTaskData).isNotEmpty();
        assertThat(prosessTaskData.get(0).getTaskType()).isEqualTo("brev.sendhenleggelse");
        assertHenleggelse(revuderingBehandlingId);
    }

    @Test
    public void kan_sende_henleggelsesbrev_for_tilbakekreving_revurdering_med_henlagt_feilopprettet_med_brev_når_varsel_er_sendt() {
        Long revuderingBehandlingId = opprettTilbakekrevingRevurdering();
        JournalpostId journalpostId = new JournalpostId("123");
        BrevSporing henleggelsesBrevsporing = new BrevSporing.Builder()
            .medBehandlingId(revuderingBehandlingId)
            .medJournalpostId(journalpostId)
            .medDokumentId("123")
            .medBrevType(BrevType.VARSEL_BREV).build();
        brevSporingRepository.lagre(henleggelsesBrevsporing);

        henleggBehandlingTjeneste.henleggBehandling(revuderingBehandlingId, BehandlingResultatType.HENLAGT_FEILOPPRETTET_MED_BREV);
        List<ProsessTaskData> prosessTaskData = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTaskData).isNotEmpty();
        assertThat(prosessTaskData.get(0).getTaskType()).isEqualTo("brev.sendhenleggelse");
        assertThat(prosessTaskData.get(1).getTaskType()).isEqualTo("send.beskjed.tilbakekreving.henlagt.selvbetjening");
        assertHenleggelse(revuderingBehandlingId);
    }

    private void lagKravgrunnlag(long behandlingId) {
        KravgrunnlagMock mockMedFeilPostering = new KravgrunnlagMock(FOM, LocalDate.of(2016, 3, 31), KlasseType.FEIL,
            BigDecimal.valueOf(11000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = new KravgrunnlagMock(FOM, LocalDate.of(2016, 3, 31),
            KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(11000));
        mockMedYtelPostering.setKlasseKode(KlasseKode.FPADATAL);

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList(mockMedFeilPostering, mockMedYtelPostering));
        grunnlagRepository.lagre(behandlingId, kravgrunnlag431);
    }

    private Long opprettTilbakekrevingRevurdering() {
        behandling.avsluttBehandling();
        Behandling revurdering = revurderingTjeneste.opprettRevurdering(behandling.getId(), BehandlingÅrsakType.RE_OPPLYSNINGER_OM_VILKÅR);
        return revurdering.getId();
    }

    private void assertHenleggelse(Long behandlingId) {
        assertThat(historikkRepository.hentHistorikk(behandlingId)).isNotEmpty();
        assertThat(behandlingRepository.hentBehandling(behandlingId).getStatus()).isEqualByComparingTo(BehandlingStatus.AVSLUTTET);
        assertThat(repoProvider.getEksternBehandlingRepository().hentOptionalFraInternId(behandlingId)).isEmpty();
    }
}
