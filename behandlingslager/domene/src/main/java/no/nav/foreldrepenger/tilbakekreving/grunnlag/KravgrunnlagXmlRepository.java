package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import java.math.BigDecimal;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class KravgrunnlagXmlRepository {
    private EntityManager entityManager;

    KravgrunnlagXmlRepository() {
        //for CDI proxy
    }

    @Inject
    public KravgrunnlagXmlRepository(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public String hentKravgrunnlagXml(Long kravgrunnlagXmlId) {
        return finnKravgrunnlagXml(kravgrunnlagXmlId).getKravgrunnlagXml();
    }

    public Long lagreKravgrunnlagXml(String xml) {
        KravgrunnlagXml entity = new KravgrunnlagXml(xml);
        entityManager.persist(entity);
        return entity.getId();
    }

    public void slettGrunnlagXml(Long xmlId) {
        KravgrunnlagXml entity = finnKravgrunnlagXml(xmlId);
        entityManager.remove(entity);
    }

    private KravgrunnlagXml finnKravgrunnlagXml(Long kravgrunnlagXmlId) {
        return entityManager.find(KravgrunnlagXml.class, kravgrunnlagXmlId);
    }

    public void oppdaterMedEksternBehandlingId(String eksternBehandlingId, Long kravgrunnlagXmlId) {
        KravgrunnlagXml entity = finnKravgrunnlagXml(kravgrunnlagXmlId);
        Long eksisterendeVersjon = finnHøyesteVersjonsnummer(eksternBehandlingId);
        long nyVersjon = eksisterendeVersjon == null ? 1 : eksisterendeVersjon + 1;
        entity.setEksternBehandling(eksternBehandlingId, nyVersjon);
        entityManager.persist(entity);
    }

    private Long finnHøyesteVersjonsnummer(String eksternBehandlingId) {
        Query query = entityManager.createNativeQuery("select max(sekvens) from kravgrunnlag_xml where ekstern_behandling_id=:eksternBehandlingId");
        query.setParameter("eksternBehandlingId", eksternBehandlingId);
        Object resultat = query.getSingleResult();
        return resultat != null ? ((BigDecimal) resultat).longValue() : null;
    }
}
