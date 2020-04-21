package no.nav.foreldrepenger.tilbakekreving.fplos.klient.task;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.fplos.klient.producer.FplosKafkaProducer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.vedtak.felles.integrasjon.kafka.EventHendelse;
import no.nav.vedtak.felles.integrasjon.kafka.Fagsystem;
import no.nav.vedtak.felles.integrasjon.kafka.TilbakebetalingBehandlingProsessEventDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(FplosPubliserEventTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class FplosPubliserEventTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "fplos.oppgavebehandling.PubliserEvent";
    public static final String PROPERTY_EVENT_NAME = "eventName";
    public static final String PROPERTY_KRAVGRUNNLAG_MANGLER_FRIST_TID = "kravgrunnlagManglerFristTid";
    public static final String PROPERTY_KRAVGRUNNLAG_MANGLER_AKSJONSPUNKT_STATUS_KODE = "kravgrunnlagManglerAksjonspunktStatusKode";
    public static final String DEFAULT_HREF = "/fpsak/fagsak/%s/behandling/%s/?punkt=default&fakta=default";

    private static final Logger logger = LoggerFactory.getLogger(FplosPubliserEventTask.class);

    private KravgrunnlagRepository grunnlagRepository;
    private BehandlingRepository behandlingRepository;
    private FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste;
    private FplosKafkaProducer fplosKafkaProducer;
    private ObjectMapper objectMapper = new ObjectMapper();
    private LocalDateTime kravgrunnlagManglerFristTid = null;
    private String kravgrunnlagManglerAksjonspunktStatusKode = null;

    FplosPubliserEventTask() {
        // for CDI proxy
    }

    @Inject
    public FplosPubliserEventTask(BehandlingRepositoryProvider repositoryProvider,
                                  FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste,
                                  FplosKafkaProducer fplosKafkaProducer) {
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.faktaFeilutbetalingTjeneste = faktaFeilutbetalingTjeneste;
        this.fplosKafkaProducer = fplosKafkaProducer;

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        String eventName = prosessTaskData.getPropertyValue(PROPERTY_EVENT_NAME);
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        String fristTidVerdi = prosessTaskData.getPropertyValue(PROPERTY_KRAVGRUNNLAG_MANGLER_FRIST_TID);
        kravgrunnlagManglerFristTid = fristTidVerdi != null ? LocalDateTime.parse(fristTidVerdi) : null;
        kravgrunnlagManglerAksjonspunktStatusKode = prosessTaskData.getPropertyValue(PROPERTY_KRAVGRUNNLAG_MANGLER_AKSJONSPUNKT_STATUS_KODE);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Kravgrunnlag431 kravgrunnlag431 = grunnlagRepository.harGrunnlagForBehandlingId(behandlingId) ? grunnlagRepository.finnKravgrunnlag(behandlingId) : null;
        try {
            fplosKafkaProducer.sendJsonMedNøkkel(behandling.getUuid().toString(), opprettEventJson(behandling, eventName, kravgrunnlag431));
            logger.info("Publiserer event:{} på kafka slik at f.eks fplos kan fordele oppgaven for videre behandling. BehandlingsId: {}", eventName, behandlingId);
        } catch (Exception e) {
            throw FplosPubliserEventTaskFeil.FACTORY.kanIkkePublisereFplosEventTilKafka(eventName, e).toException();
        }
    }

    private String opprettEventJson(Behandling behandling, String eventName, Kravgrunnlag431 kravgrunnlag431) throws IOException {
        TilbakebetalingBehandlingProsessEventDto behandlingProsessEventDto = getTilbakebetalingBehandlingProsessEventDto(behandling, eventName, kravgrunnlag431);
        String jsonData = getJson(behandlingProsessEventDto);
        logger.debug("Publiserer json data {} til Fplos for behandlingId={}", jsonData, behandling.getId());
        return jsonData;
    }

    public TilbakebetalingBehandlingProsessEventDto getTilbakebetalingBehandlingProsessEventDto(Behandling behandling, String eventName, Kravgrunnlag431 kravgrunnlag431) {
        Fagsak fagsak = behandling.getFagsak();
        String saksnummer = fagsak.getSaksnummer().getVerdi();

        Map<String, String> aksjonspunktKoderMedStatusListe = new HashMap<>();
        if (kravgrunnlagManglerFristTid != null) {
            aksjonspunktKoderMedStatusListe.put(AksjonspunktDefinisjon.VURDER_HENLEGGELSE_MANGLER_KRAVGRUNNLAG.getKode(), kravgrunnlagManglerAksjonspunktStatusKode);
        } else {
            behandling.getAksjonspunkter().forEach(aksjonspunkt ->
                aksjonspunktKoderMedStatusListe.put(aksjonspunkt.getAksjonspunktDefinisjon().getKode(), aksjonspunkt.getStatus().getKode()));
        }

        return TilbakebetalingBehandlingProsessEventDto.builder()
            .medBehandlingStatus(behandling.getStatus().getKode())
            .medEksternId(behandling.getUuid())
            .medFagsystem(Fagsystem.FPTILBAKE)
            .medSaksnummer(saksnummer)
            .medAktørId(behandling.getAktørId().getId())
            .medBehandlingSteg(behandling.getAktivtBehandlingSteg() == null ? null : behandling.getAktivtBehandlingSteg().getKode())
            .medBehandlingTypeKode(behandling.getType().getKode())
            .medBehandlendeEnhet(behandling.getBehandlendeEnhetId())
            .medEventHendelse(EventHendelse.valueOf(eventName))
            .medEventTid(LocalDateTime.now())
            .medOpprettetBehandling(behandling.getOpprettetTidspunkt())
            .medYtelseTypeKode(fagsak.getFagsakYtelseType().getKode())
            .medAksjonspunktKoderMedStatusListe(aksjonspunktKoderMedStatusListe)
            .medHref(String.format(DEFAULT_HREF, saksnummer, behandling.getId()))
            .medAnsvarligSaksbehandlerIdent(behandling.getAnsvarligSaksbehandler())
            .medFørsteFeilutbetaling(hentFørsteFeilutbetalingDato(kravgrunnlag431))
            .medFeilutbetaltBeløp(kravgrunnlag431 != null ? hentFeilutbetaltBeløp(behandling.getId()) : BigDecimal.ZERO)
            .build();
    }

    private LocalDate hentFørsteFeilutbetalingDato(Kravgrunnlag431 kravgrunnlag431) {
        if (kravgrunnlagManglerFristTid != null) {
            return kravgrunnlagManglerFristTid.toLocalDate();
        }
        if (kravgrunnlag431 == null) {
            return null;
        }
        Optional<KravgrunnlagPeriode432> førstePeriode = kravgrunnlag431.getPerioder().stream()
            .min(Comparator.comparing(KravgrunnlagPeriode432::getFom));
        return førstePeriode.map(KravgrunnlagPeriode432::getFom).orElse(null);
    }

    private BigDecimal hentFeilutbetaltBeløp(Long behandlingId) {
        return faktaFeilutbetalingTjeneste.hentBehandlingFeilutbetalingFakta(behandlingId).getAktuellFeilUtbetaltBeløp();
    }

    private String getJson(TilbakebetalingBehandlingProsessEventDto behandlingProsessEventDto) throws IOException {
        Writer jsonWriter = new StringWriter();
        objectMapper.writeValue(jsonWriter, behandlingProsessEventDto);
        jsonWriter.flush();
        return jsonWriter.toString();
    }

}
