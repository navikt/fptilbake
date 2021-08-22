package no.nav.foreldrepenger.tilbakekreving.behandlingslager.prosesstask;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskEntitet;

@Dependent
public class UtvidetProsessTaskRepository {

    private static final Logger logger = LoggerFactory.getLogger(UtvidetProsessTaskRepository.class);

    private EntityManager entityManager;

    @Inject
    public UtvidetProsessTaskRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<ProsessTaskData> finnSisteProsessTaskForProsessTaskGruppe(String task, String gruppe) {
        Query query = entityManager.createNativeQuery("select * from (select * from PROSESS_TASK where TASK_TYPE = :task and task_gruppe = :gruppe order by task_sekvens desc) where rownum = 1", ProsessTaskEntitet.class);
        query.setParameter("task", task);
        query.setParameter("gruppe", gruppe);

        logger.info("Henter tasktype {} gruppe {}, query {}", task, gruppe);

        List<ProsessTaskEntitet> alleTasker = query.getResultList();

        return alleTasker.stream().findFirst().map(ProsessTaskEntitet::tilProsessTask);
    }

}
