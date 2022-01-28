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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktKodeDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.fplos.klient.producer.FplosKafkaProducer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMock;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMockUtil;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.SlettGrunnlagEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.vedtak.felles.integrasjon.kafka.EventHendelse;
import no.nav.vedtak.felles.integrasjon.kafka.TilbakebetalingBehandlingProsessEventDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ExtendWith(JpaExtension.class)
public class FplosPubliserEventTaskTest {

    private static final LocalDate FOM_1 = LocalDate.of(2019, 12, 1);
    private static final LocalDate TOM_1 = LocalDate.of(2019, 12, 31);
    private static final LocalDate FOM_2 = LocalDate.of(2020, 1, 1);
    private static final LocalDate TOM_2 = LocalDate.of(2020, 1, 31);
    private static final UUID FPSAK_BEHANDLING_UUID = UUID.randomUUID();

    private BehandlingRepositoryProvider repositoryProvider;

    private FplosKafkaProducer mockKafkaProducer;

    private FplosPubliserEventTask fplosPubliserEventTask;

    private Behandling behandling;
    private ProsessTaskData prosessTaskData;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    @BeforeEach
    public void setup(EntityManager entityManager) {
        repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        GjenopptaBehandlingTjeneste mockGjenopptaBehandlingTjeneste = mock(GjenopptaBehandlingTjeneste.class);
        BehandlingskontrollTjeneste mockBehandlingskontrollTjeneste = mock(BehandlingskontrollTjeneste.class);
        behandlingskontrollTjeneste = new BehandlingskontrollTjeneste(new BehandlingskontrollServiceProvider(entityManager, new BehandlingModellRepository(), null));
        SlettGrunnlagEventPubliserer mockEventPubliserer = mock(SlettGrunnlagEventPubliserer.class);
        FagsystemKlient mockFagsystemKlient = mock(FagsystemKlient.class);
        mockKafkaProducer = mock(FplosKafkaProducer.class);
        KravgrunnlagTjeneste kravgrunnlagTjeneste = new KravgrunnlagTjeneste(repositoryProvider,
                mockGjenopptaBehandlingTjeneste, mockBehandlingskontrollTjeneste, mockEventPubliserer);
        FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste = new FaktaFeilutbetalingTjeneste(repositoryProvider,
                kravgrunnlagTjeneste, mockFagsystemKlient);
        fplosPubliserEventTask = new FplosPubliserEventTask(repositoryProvider, faktaFeilutbetalingTjeneste, mockKafkaProducer, Fagsystem.FPTILBAKE);

        behandling = ScenarioSimple.simple().lagre(repositoryProvider);
        behandling.setAnsvarligSaksbehandler("1234");
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(1l), FPSAK_BEHANDLING_UUID);
        repositoryProvider.getEksternBehandlingRepository().lagre(eksternBehandling);

        lagProsessTaskData();

        doNothing().when(mockKafkaProducer).sendJsonMedNøkkel(anyString(), anyString());
        SamletEksternBehandlingInfo samletEksternBehandlingInfo = SamletEksternBehandlingInfo.builder(Tillegsinformasjon.TILBAKEKREVINGSVALG)
                .setGrunninformasjon(new EksternBehandlingsinfoDto())
                .setTilbakekrevingvalg(new TilbakekrevingValgDto(VidereBehandling.TILBAKEKR_OPPRETT))
                .build();
        when(mockFagsystemKlient.hentBehandlingsinfo(FPSAK_BEHANDLING_UUID, Tillegsinformasjon.TILBAKEKREVINGSVALG)).thenReturn(samletEksternBehandlingInfo);
    }

    @Test
    public void skal_publisere_fplos_data_til_kafka() {
        var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(kontekst, List.of(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING));
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FAKTA_FEILUTBETALING);
        Kravgrunnlag431 kravgrunnlag431 = lagkravgrunnlag();

        fplosPubliserEventTask.doTask(prosessTaskData);

        verify(mockKafkaProducer, atLeastOnce()).sendJsonMedNøkkel(anyString(), anyString());
        TilbakebetalingBehandlingProsessEventDto event = fplosPubliserEventTask.getTilbakebetalingBehandlingProsessEventDto(prosessTaskData, behandling, EventHendelse.AKSJONSPUNKT_OPPRETTET.name(),
                kravgrunnlag431);

        assertThat(event.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(22000));
        assertThat(event.getFørsteFeilutbetaling()).isEqualTo(FOM_1);
        assertThat(event.getAnsvarligSaksbehandlerIdent()).isNotEmpty();

        Map<String, String> aksjonpunkterMap = event.getAksjonspunktKoderMedStatusListe();
        assertThat(aksjonpunkterMap).isNotEmpty()
                .containsEntry(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING.getKode(), AksjonspunktStatus.OPPRETTET.getKode())
                .containsEntry(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING.getKode(), AksjonspunktStatus.OPPRETTET.getKode());

        assertThat(event.getEksternId()).isEqualByComparingTo(behandling.getUuid());
        assertThat(event.getFagsystem()).isEqualTo(Fagsystem.FPTILBAKE.getKode());
        assertThat(event.getEventHendelse()).isEqualByComparingTo(EventHendelse.AKSJONSPUNKT_OPPRETTET);
        assertThat(event.getAktørId()).isEqualTo(behandling.getAktørId().getId());
        assertThat(event.getYtelseTypeKode()).isEqualTo(FagsakYtelseType.FORELDREPENGER.getKode());
        assertThat(event.getBehandlingTypeKode()).isEqualTo(BehandlingType.TILBAKEKREVING.getKode());
        assertThat(event.getBehandlingSteg()).isEqualTo(BehandlingStegType.FAKTA_FEILUTBETALING.getKode());
        assertThat(event.getHref()).isNotEmpty();
    }

    @Test
    public void skal_publisere_fplos_data_til_kafka_for_henleggelse_når_kravgrunnlag_ikke_finnes() {
        var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(kontekst, BehandlingStegType.VARSEL, List.of(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING));
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL);

        fplosPubliserEventTask.doTask(prosessTaskData);
        verify(mockKafkaProducer, atLeastOnce()).sendJsonMedNøkkel(anyString(), anyString());

        TilbakebetalingBehandlingProsessEventDto event = fplosPubliserEventTask.getTilbakebetalingBehandlingProsessEventDto(prosessTaskData, behandling, EventHendelse.AKSJONSPUNKT_AVBRUTT.name(), null);

        assertThat(event.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(event.getFørsteFeilutbetaling()).isNull();

        Map<String, String> aksjonpunkterMap = event.getAksjonspunktKoderMedStatusListe();
        assertThat(aksjonpunkterMap).isNotEmpty()
                .containsEntry(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING.getKode(), AksjonspunktStatus.OPPRETTET.getKode());

        assertThat(event.getEksternId()).isEqualByComparingTo(behandling.getUuid());
        assertThat(event.getFagsystem()).isEqualTo(Fagsystem.FPTILBAKE.getKode());
        assertThat(event.getEventHendelse()).isEqualByComparingTo(EventHendelse.AKSJONSPUNKT_AVBRUTT);
        assertThat(event.getAktørId()).isEqualTo(behandling.getAktørId().getId());
        assertThat(event.getYtelseTypeKode()).isEqualTo(FagsakYtelseType.FORELDREPENGER.getKode());
        assertThat(event.getBehandlingTypeKode()).isEqualTo(BehandlingType.TILBAKEKREVING.getKode());
        assertThat(event.getBehandlingSteg()).isEqualTo(BehandlingStegType.VARSEL.getKode());
        assertThat(event.getHref()).isNotEmpty();
    }

    @Test
    public void skal_publisere_fplos_data_til_kafka_når_behandling_venter_på_kravgrunnlag_og_fristen_går_ut() {
        var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(kontekst, BehandlingStegType.TBKGSTEG, List.of(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG));
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.TBKGSTEG);
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
        assertThat(aksjonpunkterMap).isNotEmpty()
                .containsEntry(AksjonspunktKodeDefinisjon.VURDER_HENLEGGELSE_MANGLER_KRAVGRUNNLAG, AksjonspunktStatus.OPPRETTET.getKode());

        assertThat(event.getEksternId()).isEqualByComparingTo(behandling.getUuid());
        assertThat(event.getFagsystem()).isEqualTo(Fagsystem.FPTILBAKE.getKode());
        assertThat(event.getEventHendelse()).isEqualByComparingTo(EventHendelse.AKSJONSPUNKT_OPPRETTET);
        assertThat(event.getAktørId()).isEqualTo(behandling.getAktørId().getId());
        assertThat(event.getYtelseTypeKode()).isEqualTo(FagsakYtelseType.FORELDREPENGER.getKode());
        assertThat(event.getBehandlingTypeKode()).isEqualTo(BehandlingType.TILBAKEKREVING.getKode());
        assertThat(event.getBehandlingSteg()).isEqualTo(BehandlingStegType.TBKGSTEG.getKode());
        assertThat(event.getHref()).isNotEmpty();
    }

    @Test
    public void skal_publisere_fplos_data_til_kafka_når_behandling_venter_på_kravgrunnlag_og_fristen_er_endret() {
        var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(kontekst, BehandlingStegType.TBKGSTEG, List.of(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG));
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.TBKGSTEG);
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
        assertThat(aksjonpunkterMap).isNotEmpty()
                .containsEntry(AksjonspunktKodeDefinisjon.VURDER_HENLEGGELSE_MANGLER_KRAVGRUNNLAG, AksjonspunktStatus.AVBRUTT.getKode());

        assertThat(event.getEksternId()).isEqualByComparingTo(behandling.getUuid());
        assertThat(event.getFagsystem()).isEqualTo(Fagsystem.FPTILBAKE.getKode());
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
        prosessTaskData = ProsessTaskData.forProsessTask(FplosPubliserEventTask.class);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setProperty(FplosPubliserEventTask.PROPERTY_EVENT_NAME, EventHendelse.AKSJONSPUNKT_OPPRETTET.name());
        return prosessTaskData;
    }

}
