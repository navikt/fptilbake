package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.vedtak.felles.jpa.savepoint.RunWithSavepoint;
import no.nav.vedtak.felles.jpa.savepoint.Work;

public class TekniskRepository {

    private BehandlingRepositoryProviderImpl repositoryProvider;

    public TekniskRepository(BehandlingRepositoryProvider repositoryProvider) {
        // tar en cast, trenger EntityManager
        this.repositoryProvider = (BehandlingRepositoryProviderImpl) repositoryProvider;  // NOSONAR
        
    }
    
    public <V> V doWorkInSavepoint(Work<V> work) {
        RunWithSavepoint setJdbcSavepoint = new RunWithSavepoint(repositoryProvider.getEntityManager());
        return setJdbcSavepoint.doWork(work);
    }
 }
