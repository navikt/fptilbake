package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev;

import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class VedtaksbrevSporingRepository {

    private EntityManager entityManager;

    VedtaksbrevSporingRepository() {
        //for CDI proxy
    }

    @Inject
    public VedtaksbrevSporingRepository(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager);
        this.entityManager = entityManager;
    }

    public void lagreVedtaksbrevData(VedtaksbrevSporing vedtaksbrevSporing) {
        entityManager.persist(vedtaksbrevSporing);
    }

    public List<VedtaksbrevSporing> hentVedtaksbrevData(Long behandlingId) {
        TypedQuery<VedtaksbrevSporing> query = entityManager.createQuery("from VedtaksbrevSporing where behandling_id = :behandlingId", VedtaksbrevSporing.class);
        query.setParameter("behandlingId", behandlingId);
        return query.getResultList();
    }

}
