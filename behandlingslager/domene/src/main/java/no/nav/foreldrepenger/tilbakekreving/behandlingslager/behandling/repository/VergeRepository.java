package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeAggregateEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class VergeRepository {

    private EntityManager entityManager;

    VergeRepository() {
        // for CDI proxy
    }

    @Inject
    public VergeRepository(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public long lagreVergeInformasjon(long behandlingId, VergeEntitet vergeEntitet) {
        Optional<VergeAggregateEntitet> forrigeVergeAggregateEntitet = hentVergeForBehandling(behandlingId);
        if (forrigeVergeAggregateEntitet.isPresent()) {
            VergeAggregateEntitet forrigeAggregate = forrigeVergeAggregateEntitet.get();
            forrigeAggregate.disable();
            entityManager.persist(forrigeAggregate);
        }
        VergeAggregateEntitet vergeAggregateEntitet = VergeAggregateEntitet.builder().medBehandlingId(behandlingId)
            .medVergeEntitet(vergeEntitet).build();
        entityManager.persist(vergeEntitet);
        entityManager.persist(vergeAggregateEntitet);
        return vergeEntitet.getId();
    }

    public Optional<VergeEntitet> finnVergeInformasjon(long behandlingId) {
        Optional<VergeAggregateEntitet> vergeAggregateEntitet = hentVergeForBehandling(behandlingId);
        return vergeAggregateEntitet.map(VergeAggregateEntitet::getVergeEntitet);
    }

    private Optional<VergeAggregateEntitet> hentVergeForBehandling(long behandlingId) {
        TypedQuery<VergeAggregateEntitet> query = entityManager.createQuery("from VergeAggregateEntitet where behandlingId=:behandlingId and aktiv='J'", VergeAggregateEntitet.class);
        query.setParameter("behandlingId", behandlingId);
        return hentUniktResultat(query);
    }
}
