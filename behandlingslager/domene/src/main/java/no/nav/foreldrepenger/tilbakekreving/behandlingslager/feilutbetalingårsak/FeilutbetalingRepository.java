package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class FeilutbetalingRepository {

    private EntityManager entityManager;

    FeilutbetalingRepository() {
        // For CDI
    }

    @Inject
    public FeilutbetalingRepository(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    public Optional<FeilutbetalingAggregate> finnFeilutbetaling(Long behandlingId) {
        TypedQuery<FeilutbetalingAggregate> query = entityManager.createQuery(
                "from FeilutbetalingAggregate where behandlingId=:behandlingId and aktiv=:aktiv", FeilutbetalingAggregate.class);
        query.setParameter("behandlingId", behandlingId);
        query.setParameter("aktiv", true);
        return hentUniktResultat(query);
    }

    public void lagre(FeilutbetalingAggregate feilutbetalingAggregate) {
        Optional<FeilutbetalingAggregate> forrigeAggregate = finnFeilutbetaling(feilutbetalingAggregate.getBehandlingId());
        if (forrigeAggregate.isPresent()) {
            forrigeAggregate.get().disable();
            entityManager.persist(forrigeAggregate.get());
        }
        entityManager.persist(feilutbetalingAggregate.getFeilutbetaling());
        for (FeilutbetalingPeriodeÅrsak periodeÅrsak : feilutbetalingAggregate.getFeilutbetaling().getFeilutbetaltPerioder()) {
            entityManager.persist(periodeÅrsak);
        }
        entityManager.persist(feilutbetalingAggregate);
        entityManager.flush();
    }

}
