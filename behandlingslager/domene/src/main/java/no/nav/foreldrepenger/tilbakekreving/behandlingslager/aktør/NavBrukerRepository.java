package no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;

@ApplicationScoped
public class NavBrukerRepository {

    private EntityManager entityManager;

    NavBrukerRepository() {
        // for CDI proxy
    }

    @Inject
    public NavBrukerRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    public Optional<NavBruker> hent(AktørId aktorId) {
        TypedQuery<NavBruker> query = entityManager.createQuery("from Bruker where aktørId=:aktorId", NavBruker.class);
        query.setParameter("aktorId", aktorId);
        return hentUniktResultat(query);
    }

    public NavBruker opprett(NavBruker bruker) {
        entityManager.persist(bruker);
        return bruker;
    }


}
