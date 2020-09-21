package no.nav.foreldrepenger.tilbakekreving.fplos.klient.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandlingImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fplos.klient.producer.FplosKafkaProducer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMock;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMockUtil;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.SlettGrunnlagEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.vedtak.felles.integrasjon.kafka.EventHendelse;
import no.nav.vedtak.felles.integrasjon.kafka.Fagsystem;
import no.nav.vedtak.felles.integrasjon.kafka.TilbakebetalingBehandlingProsessEventDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;

public class FplosPubliserEventTaskTest {

    private static final LocalDate FOM_1 = LocalDate.of(2019, 12, 1);
    private static final LocalDate TOM_1 = LocalDate.of(2019, 12, 31);
    private static final LocalDate FOM_2 = LocalDate.of(2020, 1, 1);
    private static final LocalDate TOM_2 = LocalDate.of(2020, 1, 31);
    private static final UUID FPSAK_BEHANDLING_UUID = UUID.randomUUID();

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private ProsessTaskRepository prosessTaskRepository = new ProsessTaskRepositoryImpl(repositoryRule.getEntityManager(), null, null);
    private AksjonspunktRepository aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();

    private GjenopptaBehandlingTjeneste mockGjenopptaBehandlingTjeneste = mock(GjenopptaBehandlingTjeneste.class);
    private BehandlingskontrollTjeneste mockBehandlingskontrollTjeneste = mock(BehandlingskontrollTjeneste.class);
    private SlettGrunnlagEventPubliserer mockEventPubliserer = mock(SlettGrunnlagEventPubliserer.class);
    private FagsystemKlient mockFagsystemKlient = mock(FagsystemKlient.class);
    private FplosKafkaProducer mockKafkaProducer = mock(FplosKafkaProducer.class);

    private KravgrunnlagTjeneste kravgrunnlagTjeneste = new KravgrunnlagTjeneste(repositoryProvider, mockGjenopptaBehandlingTjeneste,
        mockBehandlingskontrollTjeneste, mockEventPubliserer);
    private FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste = new FaktaFeilutbetalingTjeneste(repositoryProvider, kravgrunnlagTjeneste, mockFagsystemKlient);
    private InternalManipulerBehandling internalManipulerBehandling = new InternalManipulerBehandlingImpl(repositoryProvider);

    private FplosPubliserEventTask fplosPubliserEventTask = new FplosPubliserEventTask(repositoryProvider, faktaFeilutbetalingTjeneste, mockKafkaProducer);

    private Behandling behandling;
    private ProsessTaskData prosessTaskData;
    private Kravgrunnlag431 kravgrunnlag431;

    @Before
    public void setup() {
        behandling = ScenarioSimple.simple().lagre(repositoryProvider);
        behandling.setAnsvarligSaksbehandler("1234");
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(1l), FPSAK_BEHANDLING_UUID);
        repositoryProvider.getEksternBehandlingRepository().lagre(eksternBehandling);

        lagProsessTaskData();

        doNothing().when(mockKafkaProducer).sendJsonMedNøkkel(anyString(), anyString());
        SamletEksternBehandlingInfo samletEksternBehandlingInfo = SamletEksternBehandlingInfo.builder(Tillegsinformasjon.TILBAKEKREVINGSVALG)
            .setGrunninformasjon(new EksternBehandlingsinfoDto())
            .build();
        when(mockFagsystemKlient.hentBehandlingsinfo(FPSAK_BEHANDLING_UUID, Tillegsinformasjon.TILBAKEKREVINGSVALG)).thenReturn(samletEksternBehandlingInfo);
    }

    @Test
    public void skal_publisere_fplos_data_til_kafka() {
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING, BehandlingStegType.FAKTA_FEILUTBETALING);
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, BehandlingStegType.VARSEL);
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FAKTA_FEILUTBETALING);
        kravgrunnlag431 = lagkravgrunnlag();

        fplosPubliserEventTask.doTask(prosessTaskData);

        verify(mockKafkaProducer, atLeastOnce()).sendJsonMedNøkkel(anyString(), anyString());
        TilbakebetalingBehandlingProsessEventDto event = fplosPubliserEventTask.getTilbakebetalingBehandlingProsessEventDto(prosessTaskData, behandling, EventHendelse.AKSJONSPUNKT_OPPRETTET.name(), kravgrunnlag431);

        assertThat(event.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(22000));
        assertThat(event.getFørsteFeilutbetaling()).isEqualTo(FOM_1);
        assertThat(event.getAnsvarligSaksbehandlerIdent()).isNotEmpty();

        Map<String, String> aksjonpunkterMap = event.getAksjonspunktKoderMedStatusListe();
        assertThat(aksjonpunkterMap).isNotEmpty();
        assertThat(aksjonpunkterMap.get(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING.getKode())).isEqualTo(AksjonspunktStatus.OPPRETTET.getKode());
        assertThat(aksjonpunkterMap.get(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING.getKode())).isEqualTo(AksjonspunktStatus.OPPRETTET.getKode());

        assertThat(event.getEksternId()).isEqualByComparingTo(behandling.getUuid());
        assertThat(event.getFagsystem()).isEqualByComparingTo(Fagsystem.FPTILBAKE);
        assertThat(event.getEventHendelse()).isEqualByComparingTo(EventHendelse.AKSJONSPUNKT_OPPRETTET);
        assertThat(event.getAktørId()).isEqualTo(behandling.getAktørId().getId());
        assertThat(event.getYtelseTypeKode()).isEqualTo(FagsakYtelseType.FORELDREPENGER.getKode());
        assertThat(event.getBehandlingTypeKode()).isEqualTo(BehandlingType.TILBAKEKREVING.getKode());
        assertThat(event.getBehandlingSteg()).isEqualTo(BehandlingStegType.FAKTA_FEILUTBETALING.getKode());
        assertThat(event.getHref()).isNotEmpty();
    }

    @Test
    public void skal_publisere_fplos_data_til_kafka_for_henleggelse_når_kravgrunnlag_ikke_finnes() {
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, BehandlingStegType.VARSEL);
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL);

        fplosPubliserEventTask.doTask(prosessTaskData);
        verify(mockKafkaProducer, atLeastOnce()).sendJsonMedNøkkel(anyString(), anyString());

        TilbakebetalingBehandlingProsessEventDto event = fplosPubliserEventTask.getTilbakebetalingBehandlingProsessEventDto(prosessTaskData, behandling, EventHendelse.AKSJONSPUNKT_AVBRUTT.name(), null);

        assertThat(event.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(event.getFørsteFeilutbetaling()).isNull();

        Map<String, String> aksjonpunkterMap = event.getAksjonspunktKoderMedStatusListe();
        assertThat(aksjonpunkterMap).isNotEmpty();
        assertThat(aksjonpunkterMap.get(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING.getKode())).isEqualTo(AksjonspunktStatus.OPPRETTET.getKode());

        assertThat(event.getEksternId()).isEqualByComparingTo(behandling.getUuid());
        assertThat(event.getFagsystem()).isEqualByComparingTo(Fagsystem.FPTILBAKE);
        assertThat(event.getEventHendelse()).isEqualByComparingTo(EventHendelse.AKSJONSPUNKT_AVBRUTT);
        assertThat(event.getAktørId()).isEqualTo(behandling.getAktørId().getId());
        assertThat(event.getYtelseTypeKode()).isEqualTo(FagsakYtelseType.FORELDREPENGER.getKode());
        assertThat(event.getBehandlingTypeKode()).isEqualTo(BehandlingType.TILBAKEKREVING.getKode());
        assertThat(event.getBehandlingSteg()).isEqualTo(BehandlingStegType.VARSEL.getKode());
        assertThat(event.getHref()).isNotEmpty();
    }

    @Test
    public void skal_publisere_fplos_data_til_kafka_når_behandling_venter_på_kravgrunnlag_og_fristen_går_ut() {
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, BehandlingStegType.TBKGSTEG);
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.TBKGSTEG);
        LocalDateTime fristTid = LocalDateTime.now();
        ProsessTaskData prosessTaskData = lagProsessTaskData();
        prosessTaskData.setProperty(FplosPubliserEventTask.PROPERTY_KRAVGRUNNLAG_MANGLER_FRIST_TID, fristTid.toString());
        prosessTaskData.setProperty(FplosPubliserEventTask.PROPERTY_KRAVGRUNNLAG_MANGLER_AKSJONSPUNKT_STATUS_KODE, AksjonspunktStatus.OPPRETTET.getKode());

        fplosPubliserEventTask.doTask(prosessTaskData);
        verify(mockKafkaProducer, atLeastOnce()).sendJsonMedNøkkel(anyString(), anyString());
        TilbakebetalingBehandlingProsessEventDto event = fplosPubliserEventTask.getTilbakebetalingBehandlingProsessEventDto(prosessTaskData, behandling, EventHendelse.AKSJONSPUNKT_OPPRETTET.name(), null);

        assertThat(event.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(event.getFørsteFeilutbetaling()).isEqualTo(fristTid.toLocalDate());

        Map<String, String> aksjonpunkterMap = event.getAksjonspunktKoderMedStatusListe();
        assertThat(aksjonpunkterMap).isNotEmpty();
        assertThat(aksjonpunkterMap.get(AksjonspunktDefinisjon.VURDER_HENLEGGELSE_MANGLER_KRAVGRUNNLAG.getKode())).isEqualTo(AksjonspunktStatus.OPPRETTET.getKode());

        assertThat(event.getEksternId()).isEqualByComparingTo(behandling.getUuid());
        assertThat(event.getFagsystem()).isEqualByComparingTo(Fagsystem.FPTILBAKE);
        assertThat(event.getEventHendelse()).isEqualByComparingTo(EventHendelse.AKSJONSPUNKT_OPPRETTET);
        assertThat(event.getAktørId()).isEqualTo(behandling.getAktørId().getId());
        assertThat(event.getYtelseTypeKode()).isEqualTo(FagsakYtelseType.FORELDREPENGER.getKode());
        assertThat(event.getBehandlingTypeKode()).isEqualTo(BehandlingType.TILBAKEKREVING.getKode());
        assertThat(event.getBehandlingSteg()).isEqualTo(BehandlingStegType.TBKGSTEG.getKode());
        assertThat(event.getHref()).isNotEmpty();
    }

    @Test
    public void skal_publisere_fplos_data_til_kafka_når_behandling_venter_på_kravgrunnlag_og_fristen_er_endret() {
        aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG, BehandlingStegType.TBKGSTEG);
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.TBKGSTEG);
        LocalDateTime fristTid = LocalDateTime.now();
        ProsessTaskData prosessTaskData = lagProsessTaskData();
        prosessTaskData.setProperty(FplosPubliserEventTask.PROPERTY_KRAVGRUNNLAG_MANGLER_FRIST_TID, fristTid.toString());
        prosessTaskData.setProperty(FplosPubliserEventTask.PROPERTY_KRAVGRUNNLAG_MANGLER_AKSJONSPUNKT_STATUS_KODE, AksjonspunktStatus.AVBRUTT.getKode());

        fplosPubliserEventTask.doTask(prosessTaskData);
        verify(mockKafkaProducer, atLeastOnce()).sendJsonMedNøkkel(anyString(), anyString());
        TilbakebetalingBehandlingProsessEventDto event = fplosPubliserEventTask.getTilbakebetalingBehandlingProsessEventDto(prosessTaskData, behandling, EventHendelse.AKSJONSPUNKT_AVBRUTT.name(), null);

        assertThat(event.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(event.getFørsteFeilutbetaling()).isEqualTo(fristTid.toLocalDate());

        Map<String, String> aksjonpunkterMap = event.getAksjonspunktKoderMedStatusListe();
        assertThat(aksjonpunkterMap).isNotEmpty();
        assertThat(aksjonpunkterMap.get(AksjonspunktDefinisjon.VURDER_HENLEGGELSE_MANGLER_KRAVGRUNNLAG.getKode())).isEqualTo(AksjonspunktStatus.AVBRUTT.getKode());

        assertThat(event.getEksternId()).isEqualByComparingTo(behandling.getUuid());
        assertThat(event.getFagsystem()).isEqualByComparingTo(Fagsystem.FPTILBAKE);
        assertThat(event.getEventHendelse()).isEqualByComparingTo(EventHendelse.AKSJONSPUNKT_AVBRUTT);
        assertThat(event.getAktørId()).isEqualTo(behandling.getAktørId().getId());
        assertThat(event.getYtelseTypeKode()).isEqualTo(FagsakYtelseType.FORELDREPENGER.getKode());
        assertThat(event.getBehandlingTypeKode()).isEqualTo(BehandlingType.TILBAKEKREVING.getKode());
        assertThat(event.getBehandlingSteg()).isEqualTo(BehandlingStegType.TBKGSTEG.getKode());
        assertThat(event.getHref()).isNotEmpty();
    }

    private Kravgrunnlag431 lagkravgrunnlag() {
        KravgrunnlagMock mockMedFeilPostering = new KravgrunnlagMock(FOM_1, TOM_1, KlasseType.FEIL,
            BigDecimal.valueOf(10000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedFeilPostering2 = new KravgrunnlagMock(FOM_2, TOM_2, KlasseType.FEIL,
            BigDecimal.valueOf(12000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = new KravgrunnlagMock(FOM_1, TOM_1, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(10000));
        KravgrunnlagMock mockMedYtelPostering1 = new KravgrunnlagMock(FOM_2, TOM_2, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(12000));

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagMockUtil.lagMockObject(Lists.newArrayList(mockMedFeilPostering, mockMedFeilPostering2, mockMedYtelPostering, mockMedYtelPostering1));
        repositoryProvider.getGrunnlagRepository().lagre(behandling.getId(), kravgrunnlag);
        return kravgrunnlag;
    }

    private ProsessTaskData lagProsessTaskData() {
        prosessTaskData = new ProsessTaskData(FplosPubliserEventTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setProperty(FplosPubliserEventTask.PROPERTY_EVENT_NAME, EventHendelse.AKSJONSPUNKT_OPPRETTET.name());
        return prosessTaskData;
    }

}
