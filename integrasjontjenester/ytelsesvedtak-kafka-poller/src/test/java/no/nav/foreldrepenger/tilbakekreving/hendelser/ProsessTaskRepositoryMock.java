package no.nav.foreldrepenger.tilbakekreving.hendelser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTypeInfo;
import no.nav.vedtak.felles.prosesstask.api.TaskStatus;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskEntitet;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskType;

public class ProsessTaskRepositoryMock implements ProsessTaskRepository {

    private List<ProsessTaskData> prosesstasker = new ArrayList<>();
    private int gruppeTeller;
    private long idTeller;

    @Override
    public String lagre(ProsessTaskGruppe prosessTaskGruppe) {
        String gruppe = Integer.toString(gruppeTeller++);
        for (ProsessTaskGruppe.Entry entry : prosessTaskGruppe.getTasks()) {
            lagre(entry.getTask(), gruppe);
        }
        return gruppe;
    }

    @Override
    public String lagre(ProsessTaskData task) {
        String gruppe = Integer.toString(gruppeTeller++);
        return lagre(task, gruppe);
    }

    private String lagre(ProsessTaskData task, String gruppe) {
        task.setStatus(ProsessTaskStatus.KLAR);
        task.setId(idTeller++);
        task.setOpprettetTid(LocalDateTime.now());
        task.setGruppe(gruppe);
        prosesstasker.add(task);
        return gruppe;
    }

    @Override
    public ProsessTaskData finn(Long id) {
        return prosesstasker.stream()
            .filter(pt -> id.equals(pt.getId()))
            .findAny().orElse(null);
    }

    @Override
    public List<ProsessTaskData> finnAlle(ProsessTaskStatus... prosessTaskStatuses) {
        List<ProsessTaskStatus> statuser = Arrays.asList(prosessTaskStatuses);
        return prosesstasker.stream()
            .filter(pt -> statuser.contains(pt.getStatus()))
            .collect(Collectors.toList());
    }

    @Override
    public List<ProsessTaskData> finnIkkeStartet() {
        return finnAlle(ProsessTaskStatus.KLAR, ProsessTaskStatus.VETO);
    }

    @Override
    public List<ProsessTaskData> finnAlle(List<ProsessTaskStatus> statuser, LocalDateTime fom, LocalDateTime tom) {
        return prosesstasker.stream()
            .filter(pt -> statuser.contains(pt.getStatus()))
            .filter(pt -> !pt.getOpprettetTid().isBefore(fom))
            .filter(pt -> pt.getOpprettetTid().isBefore(tom))
            .collect(Collectors.toList());
    }

    @Override
    public List<ProsessTaskData> finnUferdigeBatchTasks(String s) {
        throw new IllegalArgumentException("Ikke implementert");
    }

    @Override
    public List<TaskStatus> finnStatusForTaskIGruppe(String s, String s1) {
        throw new IllegalArgumentException("Ikke implementert");
    }

    @Override
    public Optional<ProsessTaskTypeInfo> finnProsessTaskType(String s) {
        throw new IllegalArgumentException("Ikke implementert");
    }

    @Override
    public List<ProsessTaskData> finnAlleForAngittSøk(List<ProsessTaskStatus> list, String s, LocalDateTime localDateTime, LocalDateTime localDateTime1, String s1) {
        throw new IllegalArgumentException("Ikke implementert");
    }

    @Override
    public Map<ProsessTaskType, ProsessTaskEntitet> finnStatusForBatchTasks() {
        throw new IllegalArgumentException("Ikke implementert");
    }

    @Override
    public boolean suspenderAlle(Collection<ProsessTaskData> collection) {
        throw new IllegalArgumentException("Ikke implementert");
    }
}
