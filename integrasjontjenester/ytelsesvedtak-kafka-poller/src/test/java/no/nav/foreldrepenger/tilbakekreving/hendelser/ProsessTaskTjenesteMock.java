package no.nav.foreldrepenger.tilbakekreving.hendelser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

public class ProsessTaskTjenesteMock implements ProsessTaskTjeneste {

    private List<ProsessTaskData> prosesstasker = new ArrayList<>();
    private int gruppeTeller;
    private long idTeller;

    @Override
    public String lagre(ProsessTaskGruppe prosessTaskGruppe) {
        String gruppe = Integer.toString(gruppeTeller++);
        for (ProsessTaskGruppe.Entry entry : prosessTaskGruppe.getTasks()) {
            lagre(entry.task(), gruppe);
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
    public int restartAlleFeiledeTasks() {
        throw new IllegalArgumentException("Ikke implementert");
    }

    @Override
    public int slettÅrsgamleFerdige() {
        throw new IllegalArgumentException("Ikke implementert");
    }

    @Override
    public int tømNestePartisjon() {
        throw new IllegalArgumentException("Ikke implementert");
    }

    @Override
    public String lagreValidert(ProsessTaskGruppe var1) {
        throw new IllegalArgumentException("Ikke implementert");
    }

    @Override
    public String lagreValidert(ProsessTaskData var1) {
        throw new IllegalArgumentException("Ikke implementert");
    }


    @Override
    public void flaggProsessTaskForRestart(Long var1, String var2) {
        throw new IllegalArgumentException("Ikke implementert");
    }
    @Override
    public List<Long> flaggAlleFeileteProsessTasksForRestart() {
        throw new IllegalArgumentException("Ikke implementert");
    }

    @Override
    public void setProsessTaskFerdig(Long var1, ProsessTaskStatus var2) {
        throw new IllegalArgumentException("Ikke implementert");
    }
    @Override
    public void mottaHendelse(ProsessTaskData var1, String var2) {
        throw new IllegalArgumentException("Ikke implementert");
    }
    @Override
    public void mottaHendelse(ProsessTaskData var1, String var2, Properties var3) {
        throw new IllegalArgumentException("Ikke implementert");
    }

}
