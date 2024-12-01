package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

@ApplicationScoped
public class BehandlingVedtakRepository {

    private EntityManager entityManager;

    BehandlingVedtakRepository() {
        // for hibernate
    }

    @Inject
    public BehandlingVedtakRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void lagre(BehandlingVedtak behandlingVedtak) {
        entityManager.persist(behandlingVedtak);
        entityManager.flush();
    }

    public Optional<BehandlingVedtak> hentBehandlingvedtakForBehandlingId(Long behandlingId) {
        TypedQuery<BehandlingVedtak> query = entityManager.createQuery(
                "FROM BehandlingVedtak vedtak WHERE vedtak.behandlingsresultat.behandling.id = :behandlingId",
                BehandlingVedtak.class);

        query.setParameter("behandlingId", behandlingId);
        return hentUniktResultat(query);
    }

    public List<BehandlingVedtak> hentAlleBehandlingVedtak() {
        TypedQuery<BehandlingVedtak> query = entityManager.createQuery("select vedtak FROM BehandlingVedtak vedtak join fetch vedtak.behandlingsresultat", BehandlingVedtak.class);
        return query.getResultList();
    }
}
