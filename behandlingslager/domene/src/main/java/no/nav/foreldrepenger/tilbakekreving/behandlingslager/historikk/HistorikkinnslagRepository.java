package no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

@ApplicationScoped
public class HistorikkinnslagRepository {

    private EntityManager entityManager;

    @Inject
    public HistorikkinnslagRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public HistorikkinnslagRepository() {
        //CDI
    }

    public List<Historikkinnslag> hent(Saksnummer saksnummer) {
        return entityManager.createQuery("select h from Historikkinnslag2 h inner join Fagsak f On f.id = h.fagsakId where f.saksnummer= :saksnummer",
            Historikkinnslag.class).setParameter("saksnummer", saksnummer).getResultStream().toList();
    }

    public void lagre(Historikkinnslag historikkinnslag) {
        entityManager.persist(historikkinnslag);
        for (var linje : historikkinnslag.getLinjer()) {
            entityManager.persist(linje);
        }
        for (var dokument : historikkinnslag.getDokumentLinker()) {
            entityManager.persist(dokument);
        }
        entityManager.flush();
    }
}
