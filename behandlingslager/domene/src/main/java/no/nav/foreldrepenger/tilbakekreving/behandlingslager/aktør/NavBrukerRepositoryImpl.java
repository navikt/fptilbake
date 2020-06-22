package no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;

@ApplicationScoped
public class NavBrukerRepositoryImpl implements NavBrukerRepository {

    private EntityManager entityManager;

    NavBrukerRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public NavBrukerRepositoryImpl(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    @Override
    public Optional<NavBruker> hent(AktørId aktorId) {
        TypedQuery<NavBruker> query = entityManager.createQuery("from Bruker where aktørId=:aktorId", NavBruker.class);
        query.setParameter("aktorId", aktorId);
        return hentUniktResultat(query);
    }

    @Override
    public NavBruker opprett(NavBruker bruker) {
        entityManager.persist(bruker);
        return bruker;
    }


}
