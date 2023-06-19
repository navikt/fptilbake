package no.nav.foreldrepenger.tilbakekreving.los.klient.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.GjenopptaBehandlingMedGrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingModellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi.BehandlingskontrollServiceProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
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
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMock;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagMockUtil;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.SlettGrunnlagEventPubliserer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KlasseType;
import no.nav.foreldrepenger.tilbakekreving.los.klient.producer.LosKafkaProducerAiven;
import no.nav.vedtak.felles.integrasjon.kafka.EventHendelse;
import no.nav.vedtak.felles.integrasjon.kafka.TilbakebetalingBehandlingProsessEventDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ExtendWith(JpaExtension.class)
class LosPubliserEventTaskAivenTest {

    private static final LocalDate FOM_1 = LocalDate.of(2019, 12, 1);
    private static final LocalDate TOM_1 = LocalDate.of(2019, 12, 31);
    private static final LocalDate FOM_2 = LocalDate.of(2020, 1, 1);
    private static final LocalDate TOM_2 = LocalDate.of(2020, 1, 31);
    private static final UUID FPSAK_BEHANDLING_UUID = UUID.randomUUID();

    private BehandlingRepositoryProvider repositoryProvider;

    private LosKafkaProducerAiven mockKafkaProducerAiven = mock(LosKafkaProducerAiven.class);

    private LosPubliserEventTask losPubliserEventTask;

    private Behandling behandling;
    private ProsessTaskData prosessTaskData;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    @BeforeEach
    void setup(EntityManager entityManager) throws IOException {
        repositoryProvider = new BehandlingRepositoryProvider(entityManager);
        GjenopptaBehandlingMedGrunnlagTjeneste mockGjenopptaBehandlingTjeneste = mock(GjenopptaBehandlingMedGrunnlagTjeneste.class);
        BehandlingskontrollTjeneste mockBehandlingskontrollTjeneste = mock(BehandlingskontrollTjeneste.class);
        behandlingskontrollTjeneste = new BehandlingskontrollTjeneste(new BehandlingskontrollServiceProvider(entityManager, new BehandlingModellRepository(), null));
        SlettGrunnlagEventPubliserer mockEventPubliserer = mock(SlettGrunnlagEventPubliserer.class);
        FagsystemKlient mockFagsystemKlient = mock(FagsystemKlient.class);
        KravgrunnlagTjeneste kravgrunnlagTjeneste = new KravgrunnlagTjeneste(repositoryProvider,
                mockGjenopptaBehandlingTjeneste, mockBehandlingskontrollTjeneste, mockEventPubliserer, entityManager);
        FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste = new FaktaFeilutbetalingTjeneste(repositoryProvider,
                kravgrunnlagTjeneste, mockFagsystemKlient);
        losPubliserEventTask = new LosPubliserEventTask(repositoryProvider, faktaFeilutbetalingTjeneste, mockKafkaProducerAiven, Fagsystem.K9TILBAKE);

        behandling = ScenarioSimple.simple().lagre(repositoryProvider);
        behandling.setAnsvarligSaksbehandler("1234");
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, Henvisning.fraEksternBehandlingId(1l), FPSAK_BEHANDLING_UUID);
        repositoryProvider.getEksternBehandlingRepository().lagre(eksternBehandling);

        lagProsessTaskData();

        SamletEksternBehandlingInfo samletEksternBehandlingInfo = SamletEksternBehandlingInfo.builder(Tillegsinformasjon.TILBAKEKREVINGSVALG)
                .setGrunninformasjon(new EksternBehandlingsinfoDto())
                .setTilbakekrevingvalg(new TilbakekrevingValgDto(VidereBehandling.TILBAKEKR_OPPRETT))
                .build();
        when(mockFagsystemKlient.hentBehandlingsinfo(FPSAK_BEHANDLING_UUID, Tillegsinformasjon.TILBAKEKREVINGSVALG)).thenReturn(samletEksternBehandlingInfo);
    }

    @Test
    void skal_publisere_fplos_data_til_kafka() throws IOException {
        var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(kontekst, List.of(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING, AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING));
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.FAKTA_FEILUTBETALING);
        Kravgrunnlag431 kravgrunnlag431 = lagkravgrunnlag();

        losPubliserEventTask.doTask(prosessTaskData);

        verify(mockKafkaProducerAiven, atLeastOnce()).sendHendelse(any(), any());
        TilbakebetalingBehandlingProsessEventDto event = losPubliserEventTask.getTilbakebetalingBehandlingProsessEventDto(prosessTaskData, behandling, EventHendelse.AKSJONSPUNKT_OPPRETTET.name(),
                kravgrunnlag431);

        assertThat(event.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.valueOf(22000));
        assertThat(event.getFørsteFeilutbetaling()).isEqualTo(FOM_1);
        assertThat(event.getAnsvarligSaksbehandlerIdent()).isNotEmpty();

        Map<String, String> aksjonpunkterMap = event.getAksjonspunktKoderMedStatusListe();
        assertThat(aksjonpunkterMap).isNotEmpty()
                .containsEntry(AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING.getKode(), AksjonspunktStatus.OPPRETTET.getKode())
                .containsEntry(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING.getKode(), AksjonspunktStatus.OPPRETTET.getKode());

        assertThat(event.getEksternId()).isEqualByComparingTo(behandling.getUuid());
        assertThat(event.getFagsystem()).isEqualTo(Fagsystem.K9TILBAKE.getKode());
        assertThat(event.getEventHendelse()).isEqualByComparingTo(EventHendelse.AKSJONSPUNKT_OPPRETTET);
        assertThat(event.getAktørId()).isEqualTo(behandling.getAktørId().getId());
        assertThat(event.getYtelseTypeKode()).isEqualTo(FagsakYtelseType.FORELDREPENGER.getKode());
        assertThat(event.getBehandlingTypeKode()).isEqualTo(BehandlingType.TILBAKEKREVING.getKode());
        assertThat(event.getBehandlingSteg()).isEqualTo(BehandlingStegType.FAKTA_FEILUTBETALING.getKode());
        assertThat(event.getHref()).isNotEmpty();
    }

    @Test
    void skal_publisere_fplos_data_til_kafka_for_henleggelse_når_kravgrunnlag_ikke_finnes() throws IOException {
        var kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandlingskontrollTjeneste.lagreAksjonspunkterFunnet(kontekst, BehandlingStegType.VARSEL, List.of(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING));
        InternalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.VARSEL);

        losPubliserEventTask.doTask(prosessTaskData);
        verify(mockKafkaProducerAiven, atLeastOnce()).sendHendelse(any(), any());

        TilbakebetalingBehandlingProsessEventDto event = losPubliserEventTask.getTilbakebetalingBehandlingProsessEventDto(prosessTaskData, behandling, EventHendelse.AKSJONSPUNKT_AVBRUTT.name(), null);

        assertThat(event.getFeilutbetaltBeløp()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(event.getFørsteFeilutbetaling()).isNull();

        Map<String, String> aksjonpunkterMap = event.getAksjonspunktKoderMedStatusListe();
        assertThat(aksjonpunkterMap).isNotEmpty()
                .containsEntry(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING.getKode(), AksjonspunktStatus.OPPRETTET.getKode());

        assertThat(event.getEksternId()).isEqualByComparingTo(behandling.getUuid());
        assertThat(event.getFagsystem()).isEqualTo(Fagsystem.K9TILBAKE.getKode());
        assertThat(event.getEventHendelse()).isEqualByComparingTo(EventHendelse.AKSJONSPUNKT_AVBRUTT);
        assertThat(event.getAktørId()).isEqualTo(behandling.getAktørId().getId());
        assertThat(event.getYtelseTypeKode()).isEqualTo(FagsakYtelseType.FORELDREPENGER.getKode());
        assertThat(event.getBehandlingTypeKode()).isEqualTo(BehandlingType.TILBAKEKREVING.getKode());
        assertThat(event.getBehandlingSteg()).isEqualTo(BehandlingStegType.VARSEL.getKode());
        assertThat(event.getHref()).isNotEmpty();
    }

    private Kravgrunnlag431 lagkravgrunnlag() {
        KravgrunnlagMock mockMedFeilPostering = new KravgrunnlagMock(FOM_1, TOM_1, KlasseType.FEIL,
                BigDecimal.valueOf(10000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedFeilPostering2 = new KravgrunnlagMock(FOM_2, TOM_2, KlasseType.FEIL,
                BigDecimal.valueOf(12000), BigDecimal.ZERO);
        KravgrunnlagMock mockMedYtelPostering = new KravgrunnlagMock(FOM_1, TOM_1, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(10000));
        KravgrunnlagMock mockMedYtelPostering1 = new KravgrunnlagMock(FOM_2, TOM_2, KlasseType.YTEL, BigDecimal.ZERO, BigDecimal.valueOf(12000));

        Kravgrunnlag431 kravgrunnlag = KravgrunnlagMockUtil.lagMockObject(List.of(mockMedFeilPostering, mockMedFeilPostering2, mockMedYtelPostering, mockMedYtelPostering1));
        repositoryProvider.getGrunnlagRepository().lagre(behandling.getId(), kravgrunnlag);
        return kravgrunnlag;
    }

    private ProsessTaskData lagProsessTaskData() {
        prosessTaskData = ProsessTaskData.forProsessTask(LosPubliserEventTask.class);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setProperty(LosPubliserEventTask.PROPERTY_EVENT_NAME, EventHendelse.AKSJONSPUNKT_OPPRETTET.name());
        return prosessTaskData;
    }

}
