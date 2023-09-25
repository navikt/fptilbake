package no.nav.foreldrepenger.tilbakekreving.los.klient.task;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.los.klient.producer.LosKafkaProducerAiven;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.hendelser.behandling.Behandlingstype;
import no.nav.vedtak.hendelser.behandling.Hendelse;
import no.nav.vedtak.hendelser.behandling.Kildesystem;
import no.nav.vedtak.hendelser.behandling.Ytelse;
import no.nav.vedtak.hendelser.behandling.v1.BehandlingHendelseV1;

@ApplicationScoped
@ProsessTask("fplos.oppgavebehandling.behandlingshendelse")
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class FpLosPubliserEventTask implements ProsessTaskHandler {

    public static final String PROPERTY_EVENT_NAME = "eventName";

    private static final Set<Hendelse> ENDELIGE_HENDELSER = Set.of(Hendelse.OPPRETTET, Hendelse.AVSLUTTET, Hendelse.ENHET);

    private Fagsystem fagsystem;

    private static final Logger LOG = LoggerFactory.getLogger(FpLosPubliserEventTask.class);

    private BehandlingRepository behandlingRepository;
    private LosKafkaProducerAiven losKafkaProducerAiven;

    boolean brukAiven;

    FpLosPubliserEventTask() {
        // for CDI proxy
    }

    @Inject
    public FpLosPubliserEventTask(BehandlingRepositoryProvider repositoryProvider,
                                  LosKafkaProducerAiven losKafkaProducerAiven) {
        this(repositoryProvider, losKafkaProducerAiven, ApplicationName.hvilkenTilbake());
    }

    public FpLosPubliserEventTask(BehandlingRepositoryProvider repositoryProvider,
                                  LosKafkaProducerAiven losKafkaProducerAiven,
                                  Fagsystem applikasjonNavn) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.losKafkaProducerAiven = losKafkaProducerAiven;

        this.fagsystem = switch (applikasjonNavn) {
            case FPTILBAKE -> Fagsystem.FPTILBAKE;
            case K9TILBAKE -> Fagsystem.K9TILBAKE;
            default -> throw new IllegalStateException("applikasjonsnavn er satt til " + applikasjonNavn + " som ikke er en støttet verdi");
        };
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        var behandling = behandlingRepository.hentBehandling(behandlingId);
        var hendelse = utledHendelse(behandling, Hendelse.valueOf(prosessTaskData.getPropertyValue(PROPERTY_EVENT_NAME)));
        if (hendelse == null || !Fagsystem.FPTILBAKE.equals(fagsystem)) {
            LOG.info("Publiser ikke behandlingshendelse for behandling {} hendelse {}", behandling.getId(), hendelse);
            return;
        }

        var losHendelseDto = new BehandlingHendelseV1.Builder().medKildesystem(Kildesystem.FPTILBAKE).medHendelse(hendelse)
            .medHendelseUuid(UUID.randomUUID())
            .medBehandlingUuid(behandling.getUuid())
            .medSaksnummer(behandling.getFagsak().getSaksnummer().getVerdi())
            .medAktørId(behandling.getAktørId().getId())
            .medYtelse(mapYtelse(behandling))
            .medBehandlingstype(mapBehandlingstype(behandling))
            .medTidspunkt(LocalDateTime.now())
            .build();
        losKafkaProducerAiven.sendHendelseFplos(behandling.getFagsak().getSaksnummer(), losHendelseDto);
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

    private static Hendelse utledHendelse(Behandling behandling, Hendelse oppgittHendelse) {
        if (ENDELIGE_HENDELSER.contains(oppgittHendelse)) {
            return oppgittHendelse;
        }
        if (behandling.isBehandlingPåVent()) {
            return Hendelse.VENTETILSTAND;
        } else if (!behandling.getAksjonspunkter().isEmpty()) {
            return Hendelse.AKSJONSPUNKT;
        } else {
            return null;
        }
    }

}
