package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.spi;

import jakarta.persistence.EntityManager;

import no.nav.vedtak.felles.jpa.savepoint.RunWithSavepoint;
import no.nav.vedtak.felles.jpa.savepoint.Work;

public class TekniskRepository {

    private EntityManager entityManager;

    public TekniskRepository(EntityManager em) {
        this.entityManager = em;
    }

    public <V> V doWorkInSavepoint(Work<V> work) {
        var setJdbcSavepoint = new RunWithSavepoint(entityManager);
        return setJdbcSavepoint.doWork(work);
    }
}
