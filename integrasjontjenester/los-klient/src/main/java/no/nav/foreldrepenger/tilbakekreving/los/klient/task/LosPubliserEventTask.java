package no.nav.foreldrepenger.tilbakekreving.los.klient.task;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktKodeDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagPeriode432;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.los.klient.producer.LosKafkaProducerAiven;
import no.nav.foreldrepenger.tilbakekreving.los.klient.producer.LosKafkaProducerOnPrem;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.kafka.EventHendelse;
import no.nav.vedtak.felles.integrasjon.kafka.TilbakebetalingBehandlingProsessEventDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.hendelser.behandling.Behandlingstype;
import no.nav.vedtak.hendelser.behandling.Hendelse;
import no.nav.vedtak.hendelser.behandling.Kildesystem;
import no.nav.vedtak.hendelser.behandling.Ytelse;
import no.nav.vedtak.hendelser.behandling.v1.BehandlingHendelseV1;

@ApplicationScoped
@ProsessTask("fplos.oppgavebehandling.PubliserEvent")
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class LosPubliserEventTask implements ProsessTaskHandler {

    public static final String PROPERTY_EVENT_NAME = "eventName";
    public static final String PROPERTY_KRAVGRUNNLAG_MANGLER_FRIST_TID = "kravgrunnlagManglerFristTid";
    public static final String PROPERTY_KRAVGRUNNLAG_MANGLER_AKSJONSPUNKT_STATUS_KODE = "kravgrunnlagManglerAksjonspunktStatusKode";
    public static final String FP_DEFAULT_HREF = "/fpsak/fagsak/%s/behandling/%s/?punkt=default&fakta=default";
    public static final String K9_DEFAULT_HREF = "/k9/web/fagsak/%s/behandling/%s/?punkt=default&fakta=default";

    private static final boolean IS_PROD = Environment.current().isProd();

    private Fagsystem fagsystem;
    private String defaultHRef;

    private static final Logger logger = LoggerFactory.getLogger(LosPubliserEventTask.class);

    private KravgrunnlagRepository grunnlagRepository;
    private BehandlingRepository behandlingRepository;
    private FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste;
    private LosKafkaProducerOnPrem losKafkaProducerOnPrem;
    private LosKafkaProducerAiven losKafkaProducerAiven;

    boolean brukAiven;

    LosPubliserEventTask() {
        // for CDI proxy
    }

    @Inject
    public LosPubliserEventTask(BehandlingRepositoryProvider repositoryProvider,
                                FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste,
                                LosKafkaProducerOnPrem losKafkaProducerOnPrem,
                                LosKafkaProducerAiven losKafkaProducerAiven,
                                @KonfigVerdi(value = "toggle.aiven.los", defaultVerdi = "true") boolean brukAiven) {
        this(repositoryProvider, faktaFeilutbetalingTjeneste, losKafkaProducerOnPrem, losKafkaProducerAiven, ApplicationName.hvilkenTilbake(), brukAiven);
    }

    public LosPubliserEventTask(BehandlingRepositoryProvider repositoryProvider,
                                FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste,
                                LosKafkaProducerOnPrem losKafkaProducerOnPrem,
                                LosKafkaProducerAiven losKafkaProducerAiven,
                                Fagsystem applikasjonNavn,
                                boolean brukAiven) {
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.faktaFeilutbetalingTjeneste = faktaFeilutbetalingTjeneste;
        this.losKafkaProducerOnPrem = losKafkaProducerOnPrem;
        this.losKafkaProducerAiven = losKafkaProducerAiven;
        this.brukAiven = brukAiven;

        switch (applikasjonNavn) {
            case FPTILBAKE -> {
                fagsystem = Fagsystem.FPTILBAKE;
                defaultHRef = FP_DEFAULT_HREF;
            }
            case K9TILBAKE -> {
                fagsystem = Fagsystem.K9TILBAKE;
                defaultHRef = K9_DEFAULT_HREF;
            }
            default -> throw new IllegalStateException("applikasjonsnavn er satt til " + applikasjonNavn + " som ikke er en støttet verdi");
        }
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        String eventName = prosessTaskData.getPropertyValue(PROPERTY_EVENT_NAME);
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();

        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Kravgrunnlag431 kravgrunnlag431 = grunnlagRepository.harGrunnlagForBehandlingId(behandlingId) ? grunnlagRepository.finnKravgrunnlag(behandlingId) : null;
        try {
            if (fagsystem == Fagsystem.K9TILBAKE && brukAiven) {
                TilbakebetalingBehandlingProsessEventDto behandlingProsessEventDto = getTilbakebetalingBehandlingProsessEventDto(prosessTaskData, behandling, eventName, kravgrunnlag431);
                losKafkaProducerAiven.sendHendelse(behandling.getUuid(), behandlingProsessEventDto);
            } else if (!IS_PROD && fagsystem == Fagsystem.FPTILBAKE) {
                var losHendelseDto = new BehandlingHendelseV1.Builder().medKildesystem(Kildesystem.FPTILBAKE)
                    .medHendelseUuid(UUID.randomUUID())
                    .medBehandlingUuid(behandling.getUuid())
                    .medSaksnummer(behandling.getFagsak().getSaksnummer().getVerdi())
                    .medAktørId(behandling.getAktørId().getId())
                    .medYtelse(mapYtelse(behandling))
                    .medBehandlingstype(mapBehandlingstype(behandling))
                    .medHendelse(utledHendelse(behandling))
                    .build();
                losKafkaProducerAiven.sendHendelseFplos(behandling.getFagsak().getSaksnummer(), losHendelseDto);
            } else {
                TilbakebetalingBehandlingProsessEventDto behandlingProsessEventDto = getTilbakebetalingBehandlingProsessEventDto(prosessTaskData, behandling, eventName, kravgrunnlag431);
                losKafkaProducerOnPrem.sendHendelse(behandling.getUuid(), behandlingProsessEventDto);
                logger.info("Publiserer event:{} på on-prem kafka slik at los kan fordele oppgaven for videre behandling. BehandlingsId: {}", eventName, behandlingId);
            }
        } catch (Exception e) {
            throw new TekniskException("FPT-770744", String.format("Publisering av FPLOS event=%s feilet med exception %s", eventName, e), e);
        }
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
            .medFagsystem(fagsystem.getKode())
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
            .medAnsvarligBeslutterIdent(behandling.getAnsvarligBeslutter())
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

    private static Ytelse mapYtelse(Behandling behandling) {
        return switch (behandling.getFagsak().getFagsakYtelseType()) {
            case ENGANGSTØNAD -> Ytelse.ENGANGSTØNAD;
            case FORELDREPENGER -> Ytelse.FORELDREPENGER;
            case SVANGERSKAPSPENGER -> Ytelse.SVANGERSKAPSPENGER;
            default -> null;
        };
    }

    private static Behandlingstype mapBehandlingstype(Behandling behandling) {
        return switch (behandling.getType()) {
            case TILBAKEKREVING -> Behandlingstype.TILBAKEBETALING;
            case REVURDERING_TILBAKEKREVING -> Behandlingstype.TILBAKEBETALING_REVURDERING;
            default -> null;
        };
    }

    private static Hendelse utledHendelse(Behandling behandling) {
        if (behandling.isBehandlingPåVent()) {
            return Hendelse.VENTETILSTAND;
        } else if (BehandlingStatus.OPPRETTET.equals(behandling.getStatus()) && behandling.getAksjonspunkter().isEmpty()) {
            return Hendelse.OPPRETTET;
        } else if (BehandlingStatus.AVSLUTTET.equals(behandling.getStatus())) {
            return Hendelse.AVSLUTTET;
        } else {
            return Hendelse.AKSJONSPUNKT;
        }
    }

}
