package no.nav.foreldrepenger.tilbakekreving.sporing;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;


@ApplicationScoped
public class VedtakXmlRepository {

    private EntityManager entityManager;

    VedtakXmlRepository() {
        //for CDI proxy
    }

    @Inject
    public VedtakXmlRepository(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void lagre(Long behandlingId, String xml) {
        VedtakXml entity = new VedtakXml(behandlingId, xml);
        entityManager.persist(entity);
    }

    public Collection<String> finnVedtakXml(Long behandlingId) {
        TypedQuery<VedtakXml> query = entityManager.createQuery("from VedtakXml where behandling_id = :behandlingId", VedtakXml.class);
        query.setParameter("behandlingId", behandlingId);
        return query.getResultList()
            .stream()
            .map(VedtakXml::getVedtakXml)
            .collect(Collectors.toList());
    }
}
