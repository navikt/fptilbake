package no.nav.foreldrepenger.tilbakekreving.los.klient.task;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
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
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.kafka.EventHendelse;
import no.nav.vedtak.felles.integrasjon.kafka.TilbakebetalingBehandlingProsessEventDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(value = "fplos.oppgavebehandling.PubliserEvent", prioritet = 2)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class K9LosPubliserEventTask implements ProsessTaskHandler {

    public static final String PROPERTY_EVENT_NAME = "eventName";
    public static final String FP_DEFAULT_HREF = "/fpsak/fagsak/%s/behandling/%s/?punkt=default&fakta=default";
    public static final String K9_DEFAULT_HREF = "/k9/web/fagsak/%s/behandling/%s/?punkt=default&fakta=default";

    private Fagsystem fagsystem;
    private String defaultHRef;

    private static final Logger LOG = LoggerFactory.getLogger(K9LosPubliserEventTask.class);

    private KravgrunnlagRepository grunnlagRepository;
    private BehandlingRepository behandlingRepository;
    private FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste;
    private LosKafkaProducerAiven losKafkaProducerAiven;

    boolean brukAiven;

    K9LosPubliserEventTask() {
        // for CDI proxy
    }

    @Inject
    public K9LosPubliserEventTask(BehandlingRepositoryProvider repositoryProvider,
                                  FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste,
                                  LosKafkaProducerAiven losKafkaProducerAiven) {
        this(repositoryProvider, faktaFeilutbetalingTjeneste, losKafkaProducerAiven, ApplicationName.hvilkenTilbake());
    }

    public K9LosPubliserEventTask(BehandlingRepositoryProvider repositoryProvider,
                                  FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste,
                                  LosKafkaProducerAiven losKafkaProducerAiven,
                                  Fagsystem applikasjonNavn) {
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.faktaFeilutbetalingTjeneste = faktaFeilutbetalingTjeneste;
        this.losKafkaProducerAiven = losKafkaProducerAiven;

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
            if (Fagsystem.K9TILBAKE.equals(fagsystem)) {
                TilbakebetalingBehandlingProsessEventDto behandlingProsessEventDto = getTilbakebetalingBehandlingProsessEventDto(behandling, eventName, kravgrunnlag431);
                losKafkaProducerAiven.sendHendelse(behandling.getUuid(), behandlingProsessEventDto);
            } else if (Fagsystem.FPTILBAKE.equals(fagsystem)) {
                LOG.info("Publiser ikke behandlingshendelse for behandling {}", behandling.getId());
            } else {
                throw new IllegalStateException("Mangler fagsystem");
            }
        } catch (Exception e) {
            throw new TekniskException("FPT-770744", String.format("Publisering av FPLOS event=%s feilet med exception %s", eventName, e), e);
        }
    }

    public TilbakebetalingBehandlingProsessEventDto getTilbakebetalingBehandlingProsessEventDto(Behandling behandling, String eventName, Kravgrunnlag431 kravgrunnlag431) {
        Fagsak fagsak = behandling.getFagsak();
        String saksnummer = fagsak.getSaksnummer().getVerdi();
        var kravgrunnlagManglerFristTid = behandling.getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)
            .filter(Aksjonspunkt::erOpprettet)
            .map(Aksjonspunkt::getFristTid).map(LocalDateTime::toLocalDate).orElse(null);


        Map<String, String> aksjonspunktKoderMedStatusListe = new HashMap<>();
        behandling.getAksjonspunkter().forEach(aksjonspunkt ->
            aksjonspunktKoderMedStatusListe.put(aksjonspunkt.getAksjonspunktDefinisjon().getKode(), aksjonspunkt.getStatus().getKode()));

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

    private LocalDate hentFørsteFeilutbetalingDato(Kravgrunnlag431 kravgrunnlag431, LocalDate kravgrunnlagManglerFristTid) {
        if (kravgrunnlag431 == null) {
            return kravgrunnlagManglerFristTid;
        }
        return kravgrunnlag431.getPerioder().stream()
            .map(KravgrunnlagPeriode432::getFom)
            .min(LocalDate::compareTo)
            .orElseGet(() -> kravgrunnlagManglerFristTid);
    }

    private BigDecimal hentFeilutbetaltBeløp(Long behandlingId) {
        return faktaFeilutbetalingTjeneste.hentBehandlingFeilutbetalingFakta(behandlingId).getAktuellFeilUtbetaltBeløp();
    }

}
