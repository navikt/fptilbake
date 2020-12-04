package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.dbstoette.FptilbakeEntityManagerAwareExtension;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskIdDto;

@ExtendWith(FptilbakeEntityManagerAwareExtension.class)
public class ForvaltningTekniskRestTjenesteTest {

    private ProsessTaskRepository prosessTaskRepository;
    private ForvaltningTekniskRestTjeneste forvaltningTekniskRestTjeneste;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        prosessTaskRepository = new ProsessTaskRepositoryImpl(entityManager, null, null);
        forvaltningTekniskRestTjeneste = new ForvaltningTekniskRestTjeneste(prosessTaskRepository, null, null);
    }

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
