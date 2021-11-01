package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;

@ApplicationScoped
public class BehandlingresultatRepository {

    private EntityManager entityManager;

    BehandlingresultatRepository() {
        // CDI
    }

    @Inject
    public BehandlingresultatRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Long lagre(Behandlingsresultat behandlingsresultat) {
        entityManager.persist(behandlingsresultat);
        return behandlingsresultat.getId();
    }

    public void henlegg(Behandling behandling, BehandlingResultatType behandlingResultatType) {
        var builder = hent(behandling)
            .map(Behandlingsresultat::builderEndreEksisterende)
            .orElseGet(() -> Behandlingsresultat.builder().medBehandling(behandling))
            .medBehandlingResultatType(behandlingResultatType);
        lagre(builder.build());
    }

    public Optional<Behandlingsresultat> hent(Behandling behandling) {
        TypedQuery<Behandlingsresultat> query = entityManager.createQuery("from Behandlingresultat where behandling = :behandling", Behandlingsresultat.class);
        query.setParameter("behandling", behandling);
        return hentUniktResultat(query);
    }
}
