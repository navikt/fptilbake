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

    public Optional<FaktaFeilutbetaling> finnFaktaOmFeilutbetaling(Long behandlingId) {
        return finnFeilutbetaling(behandlingId).map(FaktaFeilutbetalingAggregate::getFaktaFeilutbetaling);
    }

    public Optional<Long> finnFaktaFeilutbetalingAggregateId(Long behandingId) {
        return finnFeilutbetaling(behandingId).map(FaktaFeilutbetalingAggregate::getId);
    }

    private Optional<FaktaFeilutbetalingAggregate> finnFeilutbetaling(Long behandlingId) {
        TypedQuery<FaktaFeilutbetalingAggregate> query = entityManager.createQuery(
            "from FaktaFeilutbetalingAggregate where behandlingId=:behandlingId and aktiv=:aktiv", FaktaFeilutbetalingAggregate.class);
        query.setParameter("behandlingId", behandlingId);
        query.setParameter("aktiv", true);
        return hentUniktResultat(query);
    }

    public void lagre(Long behandlingId, FaktaFeilutbetaling faktaFeilutbetaling) {
        Optional<FaktaFeilutbetalingAggregate> forrigeAggregate = finnFeilutbetaling(behandlingId);
        disableFaktaAggregate(forrigeAggregate);
        FaktaFeilutbetalingAggregate aggregate = new FaktaFeilutbetalingAggregate.Builder()
            .medBehandlingId(behandlingId)
            .medFeilutbetaling(faktaFeilutbetaling)
            .build();
        entityManager.persist(faktaFeilutbetaling);
        for (FaktaFeilutbetalingPeriode faktaPeriode : faktaFeilutbetaling.getFeilutbetaltPerioder()) {
            entityManager.persist(faktaPeriode);
        }
        entityManager.persist(aggregate);

        //TODO unngå flush, det er kun nyttig i test
        entityManager.flush();
    }

    public void sletteFaktaFeilutbetaling(Long behandlingId) {
        disableFaktaAggregate(finnFeilutbetaling(behandlingId));
    }

    private void disableFaktaAggregate(Optional<FaktaFeilutbetalingAggregate> forrigeAggregate) {
        if (forrigeAggregate.isPresent()) {
            forrigeAggregate.get().disable();
            entityManager.persist(forrigeAggregate.get());
        }
    }

}
