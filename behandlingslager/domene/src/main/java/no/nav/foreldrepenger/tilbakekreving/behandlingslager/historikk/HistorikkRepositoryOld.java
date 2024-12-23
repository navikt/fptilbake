package no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

@Deprecated(forRemoval = true) // Etter migrering
@ApplicationScoped
public class HistorikkRepositoryOld {

    private EntityManager entityManager;

    HistorikkRepositoryOld() {
        // CDI
    }

    @Inject
    public HistorikkRepositoryOld(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void lagre(HistorikkinnslagOld historikkinnslag) {

        if (historikkinnslag.getFagsakId() == null) {
        historikkinnslag.setFagsakId(getFagsakId(historikkinnslag.getBehandlingId()));
        }

        entityManager.persist(historikkinnslag);
        for (HistorikkinnslagOldDel historikkinnslagDel : historikkinnslag.getHistorikkinnslagDeler()) {
            entityManager.persist(historikkinnslagDel);
            for (HistorikkinnslagOldFelt historikkinnslagFelt : historikkinnslagDel.getHistorikkinnslagFelt()) {
                entityManager.persist(historikkinnslagFelt);
            }
        }
        entityManager.flush();
    }

    public List<HistorikkinnslagOld> hentHistorikkForSaksnummer(Saksnummer saksnummer) {
        return entityManager.createQuery(
                        "select h from Historikkinnslag h inner join Fagsak f On f.id = h.fagsakId where f.saksnummer= :saksnummer",
                        HistorikkinnslagOld.class)
                .setParameter("saksnummer", saksnummer)
                .getResultList();
    }

    private Long getFagsakId(long behandlingId) {
        return entityManager.createQuery("select b.fagsak.id from Behandling b where b.id = :behandlingId", Long.class)
                .setParameter("behandlingId", behandlingId)
                .getSingleResult();
    }

    public List<HistorikkinnslagOld> hentHistorikk(Long behandlingId) {

        Long fagsakId = getFagsakId(behandlingId);

        return entityManager.createQuery(
                        "select h from Historikkinnslag h where (h.behandlingId = :behandlingId OR h.behandlingId = NULL) AND h.fagsakId = :fagsakId ",
                        HistorikkinnslagOld.class)
                .setParameter("fagsakId", fagsakId)// NOSONAR
                .setParameter("behandlingId", behandlingId)
                .getResultList();
    }


}
