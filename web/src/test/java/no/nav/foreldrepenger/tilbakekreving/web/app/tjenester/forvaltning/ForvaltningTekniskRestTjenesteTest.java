package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskIdDto;

public class ForvaltningTekniskRestTjenesteTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private ProsessTaskRepository prosessTaskRepository = new ProsessTaskRepositoryImpl(repositoryRule.getEntityManager(), null, null);
    private ForvaltningTekniskRestTjeneste forvaltningTekniskRestTjeneste = new ForvaltningTekniskRestTjeneste(prosessTaskRepository);

    @Test
    public void skal_sett_task_ferdig_hvis_task_finnes() {
        ProsessTaskData prosessTaskData = new ProsessTaskData(TvingHenlegglBehandlingTask.TASKTYPE);
        prosessTaskRepository.lagre(prosessTaskData);

        ProsessTaskIdDto prosessTaskIdDto = new ProsessTaskIdDto(prosessTaskData.getId());
        forvaltningTekniskRestTjeneste.setTaskFerdig(prosessTaskIdDto);

        List<ProsessTaskData> taskData = prosessTaskRepository.finnAlle(ProsessTaskStatus.FERDIG);
        assertThat(taskData).isNotEmpty();
    }

}
