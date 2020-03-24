package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import com.google.common.collect.Lists;
import no.nav.foreldrepenger.domene.dokumentarkiv.journal.JournalTjeneste;
import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingModell;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegKonfigurasjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
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
import org.junit.Before;
import org.junit.Test;

import javax.persistence.FlushModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


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
        historikkinnslagTjeneste = new HistorikkinnslagTjeneste(historikkRepository, mock(JournalTjeneste.class), mock(PersoninfoAdapter.class));
        henleggBehandlingTjeneste = new HenleggBehandlingTjeneste(repoProvider, prosessTaskRepository, behandlingskontrollTjeneste, historikkinnslagTjeneste);
    }

    @Test
    public void skal_henlegge_behandling_uten_brev() {
        henleggBehandlingTjeneste.henleggBehandling(internBehandlingId, behandlingsresultat);

        assertHenleggelse();
    }

    @Test
    public void skal_henlegge_behandling_med_aksjonspunkt() {
        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING);
        assertThat(aksjonspunkt.getStatus()).isEqualTo(AksjonspunktStatus.OPPRETTET);

        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);

        assertHenleggelse();
        assertThat(aksjonspunkt.getStatus()).isEqualTo(AksjonspunktStatus.AVBRUTT);
    }

    @Test
    public void skal_ikke_henlegge_behandling_manuelt_når_grunnlag_finnes() {
        KravgrunnlagMock mockMedFeilPostering = new KravgrunnlagMock(FOM, LocalDate.of(2016, 3, 31), KlasseType.FEIL,
            BigDecimal.valueOf(11000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = new KravgrunnlagMock(FOM, LocalDate.of(2016, 3, 31),
            KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(11000));
        mockMedYtelPostering.setKlasseKode(KlasseKode.FPADATAL);

        Kravgrunnlag431 kravgrunnlag431 = KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList(mockMedFeilPostering, mockMedYtelPostering));
        grunnlagRepository.lagre(internBehandlingId, kravgrunnlag431);

        expectedException.expect(FunksjonellException.class);
        expectedException.expectMessage("FPT-663491");

        henleggBehandlingTjeneste.henleggBehandlingManuelt(behandling.getId(), behandlingsresultat, "");
    }

    @Test
    public void kan_henlegge_behandling_som_er_satt_på_vent() {
        AksjonspunktDefinisjon def = AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING;
        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, def);
        aksjonspunktRepository.setFrist(aksjonspunkt, LocalDateTime.now(), Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);

        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL);

        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);
        assertHenleggelse();
    }

    @Test
    public void kan_henlegge_behandling_der_vedtak_er_foreslått() {
        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FORESLÅ_VEDTAK);
        henleggBehandlingTjeneste.henleggBehandling(behandling.getId(), behandlingsresultat);

        assertHenleggelse();
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
        assertHenleggelse();
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
        assertHenleggelse();
    }


    private void assertHenleggelse() {
        assertThat(historikkRepository.hentHistorikk(internBehandlingId)).isNotEmpty();
        assertThat(behandlingRepository.hentBehandling(internBehandlingId).getStatus()).isEqualByComparingTo(BehandlingStatus.AVSLUTTET);
        assertThat(repoProvider.getEksternBehandlingRepository().hentOptionalFraInternId(internBehandlingId)).isEmpty();
    }
}
