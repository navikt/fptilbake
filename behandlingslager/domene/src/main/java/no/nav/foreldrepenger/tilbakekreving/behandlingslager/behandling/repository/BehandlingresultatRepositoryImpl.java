package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;

@ApplicationScoped
public class BehandlingresultatRepositoryImpl implements BehandlingresultatRepository {

    private EntityManager entityManager;

    BehandlingresultatRepositoryImpl() {
        // CDI
    }

    @Inject
    public BehandlingresultatRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Long lagre(Behandlingsresultat behandlingsresultat) {
        entityManager.persist(behandlingsresultat);
        return behandlingsresultat.getId();
    }

    @Override
    public Optional<Behandlingsresultat> hent(Behandling behandling) {
        TypedQuery<Behandlingsresultat> query = entityManager.createQuery("from Behandlingresultat where behandling = :behandling", Behandlingsresultat.class);
        query.setParameter("behandling", behandling);
        return hentUniktResultat(query);
    }
}
