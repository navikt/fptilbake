package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

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
                "FROM BehandlingVedtak vedtak WHERE vedtak.behandlingsresultat.behandling.id = :behandlingId", //$NON-NLS-1$
                BehandlingVedtak.class);

        query.setParameter("behandlingId", behandlingId); //$NON-NLS-1$
        return hentUniktResultat(query);
    }

    public List<BehandlingVedtak> hentAlleBehandlingVedtak() {
        TypedQuery<BehandlingVedtak> query = entityManager.createQuery("select vedtak FROM BehandlingVedtak vedtak join fetch vedtak.behandlingsresultat", BehandlingVedtak.class); //$NON-NLS-1$
        return query.getResultList();
    }
}
