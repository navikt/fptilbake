package no.nav.foreldrepenger.tilbakekreving.behandlingslager.prosesstask;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskEntitet;

@ApplicationScoped
public class UtvidetProsessTaskRepository {

    private EntityManager entityManager;

    UtvidetProsessTaskRepository() {
        // for CDI proxy
    }

    @Inject
    public UtvidetProsessTaskRepository(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<ProsessTaskData> finnSisteProsessTaskForProsessTaskGruppe(String task, String gruppe) {
        TypedQuery<ProsessTaskEntitet> query = entityManager.createQuery("from ProsessTaskEntitet pt where pt.taskType=:task and pt.gruppe=:gruppe order by pt.sekvens desc", ProsessTaskEntitet.class);
        query.setParameter("task", task);
        query.setParameter("gruppe", gruppe);
        query.setMaxResults(1);

        List<ProsessTaskEntitet> alleTasker = query.getResultList();

        return alleTasker.stream().findFirst().map(ProsessTaskEntitet::tilProsessTask);
    }

}
