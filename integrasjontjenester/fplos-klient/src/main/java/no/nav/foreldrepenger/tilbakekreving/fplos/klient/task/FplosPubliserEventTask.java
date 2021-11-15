package no.nav.foreldrepenger.tilbakekreving.fplos.klient.task;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktKodeDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.fplos.klient.producer.FplosKafkaProducer;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.kafka.EventHendelse;
import no.nav.vedtak.felles.integrasjon.kafka.TilbakebetalingBehandlingProsessEventDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask("fplos.oppgavebehandling.PubliserEvent")
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class FplosPubliserEventTask implements ProsessTaskHandler {

    public static final String PROPERTY_EVENT_NAME = "eventName";
    public static final String PROPERTY_KRAVGRUNNLAG_MANGLER_FRIST_TID = "kravgrunnlagManglerFristTid";
    public static final String PROPERTY_KRAVGRUNNLAG_MANGLER_AKSJONSPUNKT_STATUS_KODE = "kravgrunnlagManglerAksjonspunktStatusKode";
    public static final String FP_DEFAULT_HREF = "/fpsak/fagsak/%s/behandling/%s/?punkt=default&fakta=default";
    public static final String K9_DEFAULT_HREF = "/k9/web/fagsak/%s/behandling/%s/?punkt=default&fakta=default";
    private String fagsystem;
    private String defaultHRef;

    private static final Logger logger = LoggerFactory.getLogger(FplosPubliserEventTask.class);

    private ObjectMapper objectMapper = lagObjectMapper();

    private KravgrunnlagRepository grunnlagRepository;
    private BehandlingRepository behandlingRepository;
    private FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste;
    private FplosKafkaProducer fplosKafkaProducer;

    FplosPubliserEventTask() {
        // for CDI proxy
    }

    @Inject
    public FplosPubliserEventTask(BehandlingRepositoryProvider repositoryProvider,
                                  FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste,
                                  FplosKafkaProducer fplosKafkaProducer) {
        this(repositoryProvider, faktaFeilutbetalingTjeneste, fplosKafkaProducer, ApplicationName.hvilkenTilbake());
    }

    public FplosPubliserEventTask(BehandlingRepositoryProvider repositoryProvider,
                                  FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste,
                                  FplosKafkaProducer fplosKafkaProducer,
                                  Fagsystem applikasjonNavn) {
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.faktaFeilutbetalingTjeneste = faktaFeilutbetalingTjeneste;
        this.fplosKafkaProducer = fplosKafkaProducer;

        switch (applikasjonNavn) {
            case FPTILBAKE -> {
                fagsystem = Fagsystem.FPTILBAKE.getKode();
                defaultHRef = FP_DEFAULT_HREF;
            }
            case K9TILBAKE -> {
                fagsystem = Fagsystem.K9TILBAKE.getKode();
                defaultHRef = K9_DEFAULT_HREF;
            }
            default -> throw new IllegalStateException("app.name er satt til " + applikasjonNavn + " som ikke er en støttet verdi");
        }
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        String eventName = prosessTaskData.getPropertyValue(PROPERTY_EVENT_NAME);
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();

        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Kravgrunnlag431 kravgrunnlag431 = grunnlagRepository.harGrunnlagForBehandlingId(behandlingId) ? grunnlagRepository.finnKravgrunnlag(behandlingId) : null;
        try {
            fplosKafkaProducer.sendJsonMedNøkkel(behandling.getUuid().toString(), opprettEventJson(prosessTaskData, behandling, eventName, kravgrunnlag431));
            logger.info("Publiserer event:{} på kafka slik at f.eks fplos kan fordele oppgaven for videre behandling. BehandlingsId: {}", eventName, behandlingId);
        } catch (Exception e) {
            throw new TekniskException("FPT-770744", String.format("Publisering av FPLOS event=%s feilet med exception %s", eventName, e), e);
        }
    }

    private String opprettEventJson(ProsessTaskData prosessTaskData, Behandling behandling, String eventName, Kravgrunnlag431 kravgrunnlag431) throws IOException {
        TilbakebetalingBehandlingProsessEventDto behandlingProsessEventDto = getTilbakebetalingBehandlingProsessEventDto(prosessTaskData, behandling, eventName, kravgrunnlag431);
        return getJson(behandlingProsessEventDto);
    }

    public TilbakebetalingBehandlingProsessEventDto getTilbakebetalingBehandlingProsessEventDto(ProsessTaskData prosessTaskData, Behandling behandling, String eventName, Kravgrunnlag431 kravgrunnlag431) {
        Fagsak fagsak = behandling.getFagsak();
        String saksnummer = fagsak.getSaksnummer().getVerdi();
        String fristTidVerdi = prosessTaskData.getPropertyValue(PROPERTY_KRAVGRUNNLAG_MANGLER_FRIST_TID);
        LocalDateTime kravgrunnlagManglerFristTid = fristTidVerdi != null ? LocalDateTime.parse(fristTidVerdi) : null;
        String kravgrunnlagManglerAksjonspunktStatusKode = prosessTaskData.getPropertyValue(PROPERTY_KRAVGRUNNLAG_MANGLER_AKSJONSPUNKT_STATUS_KODE);

        Map<String, String> aksjonspunktKoderMedStatusListe = new HashMap<>();
        if (kravgrunnlagManglerFristTid != null) {
            aksjonspunktKoderMedStatusListe.put(AksjonspunktKodeDefinisjon.VURDER_HENLEGGELSE_MANGLER_KRAVGRUNNLAG, kravgrunnlagManglerAksjonspunktStatusKode);
        } else {
            behandling.getAksjonspunkter().forEach(aksjonspunkt ->
                aksjonspunktKoderMedStatusListe.put(aksjonspunkt.getAksjonspunktDefinisjon().getKode(), aksjonspunkt.getStatus().getKode()));
        }

        return TilbakebetalingBehandlingProsessEventDto.builder()
            .medBehandlingStatus(behandling.getStatus().getKode())
            .medEksternId(behandling.getUuid())
            .medFagsystem(fagsystem)
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
            .medHref(String.format(defaultHRef, saksnummer, behandling.getId()))
            .medAnsvarligSaksbehandlerIdent(behandling.getAnsvarligSaksbehandler())
            .medFørsteFeilutbetaling(hentFørsteFeilutbetalingDato(kravgrunnlag431, kravgrunnlagManglerFristTid))
            .medFeilutbetaltBeløp(kravgrunnlag431 != null ? hentFeilutbetaltBeløp(behandling.getId()) : BigDecimal.ZERO)
            .build();
    }

    private LocalDate hentFørsteFeilutbetalingDato(Kravgrunnlag431 kravgrunnlag431, LocalDateTime kravgrunnlagManglerFristTid) {
        if (kravgrunnlagManglerFristTid != null) {
            return kravgrunnlagManglerFristTid.toLocalDate();
        }
        if (kravgrunnlag431 == null) {
            return null;
        }
        return kravgrunnlag431.getPerioder().stream()
            .map(KravgrunnlagPeriode432::getFom)
            .min(LocalDate::compareTo)
            .orElse(null);
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

    private ObjectMapper lagObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.registerModule(new Jdk8Module());
        return om;
    }

}
