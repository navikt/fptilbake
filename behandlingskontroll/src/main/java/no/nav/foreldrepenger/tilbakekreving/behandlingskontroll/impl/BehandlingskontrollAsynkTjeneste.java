package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.task.FortsettBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
public class BehandlingskontrollAsynkTjeneste {

    private ProsessTaskTjeneste taskTjeneste;

    BehandlingskontrollAsynkTjeneste() {
        // CDI
    }

    @Inject
    public BehandlingskontrollAsynkTjeneste(ProsessTaskTjeneste taskTjeneste) {
        this.taskTjeneste = taskTjeneste;
    }

    /**
     * Sjekker om prosess tasks pågår nå for angitt behandling. Returnerer neste utestående tasks per gruppe for ikke-kjørte statuser.
     * <p>
     * Hvis gruppe angis sjekkes angitt gruppe først; dersom den er ferdig returneres eventuelle andre åpne grupper for angitt behandling.
     */
    public Map<String, ProsessTaskData> sjekkProsessTaskPågårForBehandling(Behandling behandling, String gruppe) {
        var statusProsessTasks = sjekkStatusProsessTasks(behandling.getSaksnummer(), behandling.getId(), gruppe).stream()
                .collect(Collectors.groupingBy(ProsessTaskData::getGruppe));
        return nesteProsessTaskPerGruppe(statusProsessTasks);
    }

    private Map<String, ProsessTaskData> nesteProsessTaskPerGruppe(Map<String, List<ProsessTaskData>> tasks) {
        // velg top task per gruppe
        return tasks.values().stream()
            .filter(ptdList -> !ptdList.isEmpty())
            .map(ptdList -> ptdList.stream()
                .min(Comparator.comparing(ProsessTaskData::getSekvens) /* NB: neste avhenger av enum ordinal! */
                    .thenComparing(Comparator.comparing(ProsessTaskData::getStatus).reversed()))
                .orElseThrow())
            .collect(Collectors.toMap(ProsessTaskData::getGruppe, Function.identity()));
    }

    private List<ProsessTaskData> sjekkStatusProsessTasks(Saksnummer saksnummer, Long behandlingId, String gruppe) {
        Objects.requireNonNull(saksnummer, "saksnummer");

        var now = LocalDateTime.now().withNano(0).withSecond(0);

        // et tidsrom for neste kjøring vi kan ta hensyn til. Det som er lenger ut i fremtiden er ikke relevant her, kun det vi kan forvente
        // kjøres i umiddelbar fremtid. tar i tillegg hensyn til alt som skulle ha vært kjørt tilbake i tid (som har stoppet av en eller annen
        // grunn).
        var fom = now.minusWeeks(2);
        var tom = now.plusMinutes(10); // kun det som forventes kjørt om kort tid.

        if (gruppe != null) {
            var aktuelle = taskTjeneste.finnUferdigForGruppe(gruppe).stream()
                    .filter(t -> Objects.equals(t.getSaksnummer(), saksnummer.getVerdi()))
                    .filter(t -> behandlingId == null || Objects.equals(t.getBehandlingIdAsLong(), behandlingId))
                    .filter(t -> t.getNesteKjøringEtter() == null || (t.getNesteKjøringEtter().isAfter(fom) && t.getNesteKjøringEtter().isBefore(tom)))
                    .toList();
            if (!aktuelle.isEmpty()) {
                return aktuelle;
            }
        }

        return taskTjeneste.finnAktive(saksnummer.getVerdi()).stream()
                .filter(t -> behandlingId == null || Objects.equals(t.getBehandlingIdAsLong(), behandlingId))
                .filter(t -> t.getNesteKjøringEtter() == null || (t.getNesteKjøringEtter().isAfter(fom) && t.getNesteKjøringEtter().isBefore(tom)))
                .toList();
    }

    /**
     * Kjør prosess asynkront (i egen prosess task) videre.
     *
     * @return gruppe assignet til prosess task
     */
    public String asynkProsesserBehandling(Behandling behandling) {
        ProsessTaskData taskData = ProsessTaskData.forProsessTask(FortsettBehandlingTask.class);
        taskData.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getId());
        return taskTjeneste.lagre(taskData);
    }

}
