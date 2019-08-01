package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class BehandlingVedtakRepository {

    private EntityManager entityManager;

    BehandlingVedtakRepository() {
        // for hibernate
    }

    @Inject
    public BehandlingVedtakRepository(@VLPersistenceUnit EntityManager entityManager) {
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
}
