package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskStatusUtil;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.AsyncPollingStatus;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;

public class VurderProsessTaskStatusForPollingApi {

    private static final Logger log = LoggerFactory.getLogger(VurderProsessTaskStatusForPollingApi.class);

    private Long entityId;

    private static class ProsessTaskFeilmelder {

        static TekniskException utsattKjøringAvProsessTask(String callId, Long entityId, String gruppe, Long taskId, ProsessTaskStatus taskStatus, LocalDateTime nesteKjøringEtter) {
            return new TekniskException("FPT-193309", String.format("[%1$s]. Forespørsel på behandling [id=%2$s] som er utsatt i påvente av task [id=%4$s], Gruppe [%3$s] kjøres ikke før senere. Task status=%5$s, planlagt neste kjøring=%6$s", callId, entityId, gruppe, taskId, taskStatus, nesteKjøringEtter));
        }

        static TekniskException venterPåSvar(String callId, Long entityId, String gruppe, Long id, ProsessTaskStatus status) {
            return new TekniskException("FPT-193310", String.format("[%1$s]. Forespørsel på behandling [id=%2$s] som venter på svar fra annet system (task [id=%4$s], gruppe [%3$s] kjøres ikke før det er mottatt). Task status=%5$s", callId, entityId, gruppe, id, status));
        }
    }

    public VurderProsessTaskStatusForPollingApi(Long entityId) {
        this.entityId = entityId;
    }

    public Optional<AsyncPollingStatus> sjekkStatusNesteProsessTask(String gruppe, Map<String, ProsessTaskData> nesteTask) {
        LocalDateTime maksTidFørNesteKjøring = LocalDateTime.now().plusMinutes(2);
        nesteTask = nesteTask.entrySet().stream()
                .filter(e -> !ProsessTaskStatusUtil.FERDIG_STATUSER.contains(e.getValue().getStatus())) // trenger ikke FERDIG
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!nesteTask.isEmpty()) {
            Optional<ProsessTaskData> optTask = Optional.ofNullable(nesteTask.get(gruppe));
            if (!optTask.isPresent()) {
                // plukker neste til å polle på
                optTask = nesteTask.entrySet().stream()
                        .map(Map.Entry::getValue)
                        .findFirst();
            }

            if (optTask.isPresent()) {
                return sjekkStatus(maksTidFørNesteKjøring, optTask);
            }
        }
        return Optional.empty();
    }

    private Optional<AsyncPollingStatus> sjekkStatus(LocalDateTime maksTidFørNesteKjøring, Optional<ProsessTaskData> optTask) {
        ProsessTaskData task = optTask.get();
        String gruppe = task.getGruppe();
        String callId = task.getPropertyValue("callId");
        ProsessTaskStatus taskStatus = task.getStatus();
        if (ProsessTaskStatus.KLAR.equals(taskStatus)) {
            return ventPåKlar(gruppe, maksTidFørNesteKjøring, task, callId);
        } else if (ProsessTaskStatus.VENTER_SVAR.equals(taskStatus)) {
            return ventPåSvar(gruppe, task, callId);
        } else {
            // dekker SUSPENDERT, FEILET, VETO
            return håndterFeil(gruppe, task, callId);
        }
    }

    private Optional<AsyncPollingStatus> håndterFeil(String gruppe, ProsessTaskData task, String callId) {
        log.info(String.format("FPT-193308:[%1$s]. Forespørsel på behandling [id=%2$s] som ikke kan fortsette, Problemer med task gruppe [%3$s]. Siste prosesstask[id=%4$s] status=%5$s", callId, entityId, gruppe, task.getId(), task.getStatus()));

        AsyncPollingStatus status = new AsyncPollingStatus(AsyncPollingStatus.Status.HALTED, null, task.getSisteFeil());
        return Optional.of(status); // fortsett å polle på gruppe, er ikke ferdig.
    }

    private Optional<AsyncPollingStatus> ventPåSvar(String gruppe, ProsessTaskData task, String callId) {
        var feil = ProsessTaskFeilmelder.venterPåSvar(callId, entityId, gruppe, task.getId(), task.getStatus());
        logInfo(feil);

        AsyncPollingStatus status = new AsyncPollingStatus(
                AsyncPollingStatus.Status.DELAYED,
                task.getNesteKjøringEtter(),
                feil.getMessage());

        return Optional.of(status); // er ikke ferdig, men ok å videresende til visning av behandling med feilmelding der.
    }

    private void logInfo(TekniskException feil) {
        log.info("{}: {}", feil.getKode(), feil.getMessage());
    }

    private Optional<AsyncPollingStatus> ventPåKlar(String gruppe, LocalDateTime maksTidFørNesteKjøring, ProsessTaskData task, String callId) {
        if (task.getNesteKjøringEtter().isBefore(maksTidFørNesteKjøring)) {

            AsyncPollingStatus status = new AsyncPollingStatus(
                    AsyncPollingStatus.Status.PENDING,
                    task.getNesteKjøringEtter(),
                    "Venter på prosesstask [" + task.getTaskType() + "][id: " + task.getId() + "]",
                    null, 500L);

            return Optional.of(status); // fortsett å polle på gruppe, er ikke ferdig.
        } else {
            var feil = ProsessTaskFeilmelder.utsattKjøringAvProsessTask(
                    callId, entityId, gruppe, task.getId(), task.getStatus(), task.getNesteKjøringEtter());
            logInfo(feil);

            AsyncPollingStatus status = new AsyncPollingStatus(
                    AsyncPollingStatus.Status.DELAYED,
                    task.getNesteKjøringEtter(),
                    feil.getMessage());

            return Optional.of(status);
        }
    }

}
