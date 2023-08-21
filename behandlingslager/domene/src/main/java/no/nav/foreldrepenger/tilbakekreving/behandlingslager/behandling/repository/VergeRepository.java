package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeAggregateEntitet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeEntitet;

@ApplicationScoped
public class VergeRepository {

    private EntityManager entityManager;

    VergeRepository() {
        // for CDI proxy
    }

    @Inject
    public VergeRepository(EntityManager entityManager) {
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

    public void fjernVergeInformasjon(long behandlingId) {
        Optional<VergeAggregateEntitet> vergeAggregateEntitet = hentVergeForBehandling(behandlingId);
        vergeAggregateEntitet.ifPresent(vergeAggregate -> {
            vergeAggregate.disable();
            entityManager.persist(vergeAggregate);
        });
    }

    public boolean finnesVerge(long behandlingId) {
        return finnVergeInformasjon(behandlingId).isPresent();
    }

    private Optional<VergeAggregateEntitet> hentVergeForBehandling(long behandlingId) {
        TypedQuery<VergeAggregateEntitet> query = entityManager.createQuery("from VergeAggregateEntitet where behandlingId=:behandlingId and aktiv='J'", VergeAggregateEntitet.class);
        query.setParameter("behandlingId", behandlingId);
        return hentUniktResultat(query);
    }
}
