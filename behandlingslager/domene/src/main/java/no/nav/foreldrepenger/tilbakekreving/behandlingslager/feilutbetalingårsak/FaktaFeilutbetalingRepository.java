package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class FaktaFeilutbetalingRepository {

    private EntityManager entityManager;

    FaktaFeilutbetalingRepository() {
        // For CDI
    }

    @Inject
    public FaktaFeilutbetalingRepository(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<FaktaFeilutbetalingAggregate> finnFeilutbetaling(Long behandlingId) {
        TypedQuery<FaktaFeilutbetalingAggregate> query = entityManager.createQuery(
                "from FaktaFeilutbetalingAggregate where behandlingId=:behandlingId and aktiv=:aktiv", FaktaFeilutbetalingAggregate.class);
        query.setParameter("behandlingId", behandlingId);
        query.setParameter("aktiv", true);
        return hentUniktResultat(query);
    }

    public void lagre(FaktaFeilutbetalingAggregate faktaFeilutbetalingAggregate) {
        Optional<FaktaFeilutbetalingAggregate> forrigeAggregate = finnFeilutbetaling(faktaFeilutbetalingAggregate.getBehandlingId());
        if (forrigeAggregate.isPresent()) {
            forrigeAggregate.get().disable();
            entityManager.persist(forrigeAggregate.get());
        }
        entityManager.persist(faktaFeilutbetalingAggregate.getFaktaFeilutbetaling());
        for (FaktaFeilutbetalingPeriode periodeÅrsak : faktaFeilutbetalingAggregate.getFaktaFeilutbetaling().getFeilutbetaltPerioder()) {
            entityManager.persist(periodeÅrsak);
        }
        entityManager.persist(faktaFeilutbetalingAggregate);
        entityManager.flush();
    }

}
