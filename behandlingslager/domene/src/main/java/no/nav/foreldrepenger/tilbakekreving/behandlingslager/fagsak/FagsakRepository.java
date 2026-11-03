package no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.hibernate.KeyType;

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
        Objects.requireNonNull(entityManager, "entityManager");
        this.entityManager = entityManager;
    }

    public List<Fagsak> hentForBruker(AktørId aktørId) {
        TypedQuery<Fagsak> query = entityManager.createQuery("from Fagsak where navBruker.aktørId=:aktørId", Fagsak.class);
        query.setParameter("aktørId", aktørId);
        return query.getResultList();
    }

    public Optional<Fagsak> hentSakGittSaksnummer(Saksnummer saksnummer) {
        return Optional.ofNullable(entityManager.find(Fagsak.class, saksnummer, KeyType.NATURAL));
    }

    public Fagsak hentEksaktFagsakForGittSaksnummer(Saksnummer saksnummer) {
        return hentSakGittSaksnummer(saksnummer)
            .orElseThrow(() -> new TekniskException("FPT-429884", String.format("Fant ikke fagsak med saksnummer: %s", saksnummer)));
    }

    public Long lagre(Fagsak fagsak) {
        entityManager.persist(fagsak.getNavBruker());
        entityManager.persist(fagsak);
        entityManager.flush();
        return fagsak.getId();
    }

}
