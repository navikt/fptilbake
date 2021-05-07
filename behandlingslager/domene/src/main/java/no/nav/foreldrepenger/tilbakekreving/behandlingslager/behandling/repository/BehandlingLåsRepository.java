package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;

/**
 * @see BehandlingLås
 */
@ApplicationScoped
public class BehandlingLåsRepository {
    private EntityManager entityManager;

    BehandlingLåsRepository() {
        // for CDI proxy
    }

    @Inject
    public BehandlingLåsRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Initialiser lås og ta lock på tilhørende database rader.
     */
    public BehandlingLås taLås(final Long behandlingId) {
        if (behandlingId != null) {
            final LockModeType lockModeType = LockModeType.PESSIMISTIC_WRITE;
            låsBehandling(behandlingId, lockModeType);
            BehandlingLås lås = new BehandlingLås(behandlingId);
            return lås;
        } else {
            return new BehandlingLås(null);
        }

    }

    private Long låsBehandling(final Long behandlingId, LockModeType lockModeType) {
        Object[] result = (Object[]) entityManager
            .createQuery("select beh.fagsak.id, beh.versjon from Behandling beh where beh.id=:id") //$NON-NLS-1$
            .setParameter("id", behandlingId) //$NON-NLS-1$
            .setLockMode(lockModeType)
            .getSingleResult();
        return (Long) result[0];
    }

    /**
     * Verifiser lås ved å sjekke mot underliggende lager.
     */
    public void oppdaterLåsVersjon(BehandlingLås lås) {
        if (lås.getBehandlingId() != null) {
            verifisertLås(lås.getBehandlingId());
        } // else NO-OP (for ny behandling uten id)
    }

    private Object verifisertLås(Long id) {
        LockModeType lockMode = LockModeType.PESSIMISTIC_FORCE_INCREMENT;
        Object entity = entityManager.find(Behandling.class, id);
        if (entity == null) {
            throw BehandlingRepositoryFeil.fantIkkeEntitetForLåsing(Behandling.class.getSimpleName(), id);
        } else {
            entityManager.lock(entity, lockMode);
        }
        return entity;
    }

}
