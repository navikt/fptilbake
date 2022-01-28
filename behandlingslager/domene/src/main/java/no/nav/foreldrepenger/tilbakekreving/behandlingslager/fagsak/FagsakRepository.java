package no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentEksaktResultat;
import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
public class FagsakRepository {

    private EntityManager entityManager;

    FagsakRepository() {
        // for CDI proxy
    }

    @Inject
    public FagsakRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    public Fagsak finnEksaktFagsak(long fagsakId) {
        TypedQuery<Fagsak> query = entityManager.createQuery("from Fagsak where id=:fagsakId", Fagsak.class);
        query.setParameter("fagsakId", fagsakId);
        return hentEksaktResultat(query);
    }

    public Optional<Fagsak> finnUnikFagsak(long fagsakId) {
        TypedQuery<Fagsak> query = entityManager.createQuery("from Fagsak where id=:fagsakId", Fagsak.class);
        query.setParameter("fagsakId", fagsakId);
        return hentUniktResultat(query);
    }


    public List<Fagsak> hentForBruker(AktørId aktørId) {
        TypedQuery<Fagsak> query = entityManager.createQuery("from Fagsak where navBruker.aktørId=:aktørId", Fagsak.class);
        query.setParameter("aktørId", aktørId);
        return query.getResultList();
    }

    public List<Fagsak> hentForBrukerAktørId(AktørId aktørId) {
        TypedQuery<Fagsak> query = entityManager
                .createQuery("select fagsak from Fagsak fagsak join fagsak.navBruker bruk where bruk.aktørId=:aktoerId", Fagsak.class);
        query.setParameter("aktoerId", aktørId);
        return query.getResultList();
    }


    public Optional<Fagsak> hentSakGittSaksnummer(Saksnummer saksnummer) {
        TypedQuery<Fagsak> query = entityManager.createQuery("from Fagsak where saksnummer=:saksnummer", Fagsak.class);
        query.setParameter("saksnummer", saksnummer);

        List<Fagsak> fagsaker = query.getResultList();
        if (fagsaker.size() > 1) {
            throw new TekniskException("FPT-429883", String.format("Det var flere enn en Fagsak for saksnummer: %s", saksnummer));
        }

        return fagsaker.isEmpty() ? Optional.empty() : Optional.of(fagsaker.get(0));
    }

    public Fagsak hentEksaktFagsakForGittSaksnummer(Saksnummer saksnummer) {
        Optional<Fagsak> fagsak = hentSakGittSaksnummer(saksnummer);
        if (fagsak.isEmpty()) {
            throw new TekniskException("FPT-429884", String.format("Fant ikke fagsak med saksnummer: %s", saksnummer));
        }
        return fagsak.get();
    }

    public Long lagre(Fagsak fagsak) {
        entityManager.persist(fagsak.getNavBruker());
        entityManager.persist(fagsak);
        entityManager.flush();
        return fagsak.getId();
    }


    /**
     * Oppderer status på fagsak.
     *
     * @param fagsakId - id på fagsak
     * @param status   - ny status
     */
    public void oppdaterFagsakStatus(Long fagsakId, FagsakStatus status) {
        Fagsak fagsak = finnEksaktFagsak(fagsakId);
        fagsak.oppdaterStatus(status);
        entityManager.persist(fagsak);
    }

    public List<Fagsak> hentForStatus(FagsakStatus fagsakStatus) {
        TypedQuery<Fagsak> query = entityManager.createQuery("select fagsak from Fagsak fagsak where fagsak.fagsakStatus=:fagsakStatus", Fagsak.class);
        query.setParameter("fagsakStatus", fagsakStatus);

        return query.getResultList();
    }
}
