package no.nav.foreldrepenger.tilbakekreving.økonomixml;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class ØkonomiMottattXmlRepository {
    private EntityManager entityManager;

    ØkonomiMottattXmlRepository() {
        //for CDI proxy
    }

    @Inject
    public ØkonomiMottattXmlRepository(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public String hentMottattXml(Long mottattXmlId) {
        return finnMottattXml(mottattXmlId).getMottattXml();
    }

    public Long lagreMottattXml(String xml) {
        ØkonomiXmlMottatt entity = new ØkonomiXmlMottatt(xml);
        entityManager.persist(entity);
        return entity.getId();
    }

    public void slettMottattXml(Long xmlId) {
        ØkonomiXmlMottatt entity = finnMottattXml(xmlId);
        entityManager.remove(entity);
    }

    public Optional<ØkonomiXmlMottatt> finnForEksternBehandlingId(String eksternBehandlingId) {
        TypedQuery<ØkonomiXmlMottatt> query = entityManager.createQuery("from ØkonomiXmlMottatt where eksternBehandlingId=:eksternBehandlingId", ØkonomiXmlMottatt.class);
        query.setParameter("eksternBehandlingId", eksternBehandlingId);
        return hentUniktResultat(query);
    }

    public List<ØkonomiXmlMottatt> finnAlleForEksternBehandlingId(String eksternBehandlingId) {
        TypedQuery<ØkonomiXmlMottatt> query = entityManager.createQuery("from ØkonomiXmlMottatt where eksternBehandlingId=:eksternBehandlingId", ØkonomiXmlMottatt.class);
        query.setParameter("eksternBehandlingId", eksternBehandlingId);
        return query.getResultList();
    }

    private ØkonomiXmlMottatt finnMottattXml(Long mottattXmlId) {
        return entityManager.find(ØkonomiXmlMottatt.class, mottattXmlId);
    }

    public void oppdaterMedEksternBehandlingId(String eksternBehandlingId, Long kravgrunnlagXmlId) {
        ØkonomiXmlMottatt entity = finnMottattXml(kravgrunnlagXmlId);
        Long eksisterendeVersjon = finnHøyesteVersjonsnummer(eksternBehandlingId);
        long nyVersjon = eksisterendeVersjon == null ? 1 : eksisterendeVersjon + 1;
        entity.setEksternBehandling(eksternBehandlingId, nyVersjon);
        entityManager.persist(entity);
    }

    private Long finnHøyesteVersjonsnummer(String eksternBehandlingId) {
        Query query = entityManager.createNativeQuery("select max(sekvens) from oko_xml_mottatt where ekstern_behandling_id=:eksternBehandlingId");
        query.setParameter("eksternBehandlingId", eksternBehandlingId);
        Object resultat = query.getSingleResult();
        return resultat != null ? ((BigDecimal) resultat).longValue() : null;
    }
}
