package no.nav.foreldrepenger.tilbakekreving.økonomixml;

import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;


@ApplicationScoped
public class ØkonomiSendtXmlRepository {

    private EntityManager entityManager;

    ØkonomiSendtXmlRepository() {
        //for CDI proxy
    }

    @Inject
    public ØkonomiSendtXmlRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public Long lagre(Long behandlingId, String xml, MeldingType meldingType) {
        ØkonomiXmlSendt entity = new ØkonomiXmlSendt(behandlingId, xml);
        entity.setMeldingType(meldingType);
        entityManager.persist(entity);
        entityManager.flush();
        return entity.getId();
    }

    public Collection<String> finnXml(Long behandlingId, MeldingType meldingType) {
        TypedQuery<ØkonomiXmlSendt> query = entityManager.createQuery("from OkoXmlSendt where behandlingId = :behandlingId and meldingType =:meldingType", ØkonomiXmlSendt.class);
        query.setParameter("behandlingId", behandlingId);
        query.setParameter("meldingType", meldingType);
        return query.getResultList()
                .stream()
                .map(ØkonomiXmlSendt::getMelding)
                .collect(Collectors.toList());
    }

    public void oppdatereKvittering(Long sendtXmlId, String kvitteringXml) {
        ØkonomiXmlSendt entity = finnSendtXml(sendtXmlId);
        entity.setKvittering(kvitteringXml);
        entityManager.persist(entity);
    }

    public Collection<ØkonomiXmlSendt> finn(MeldingType meldingType, LocalDate opprettetDato) {
        TypedQuery<ØkonomiXmlSendt> query = entityManager.createQuery("from OkoXmlSendt where meldingType = :meldingType and opprettetTidspunkt >= :t0 and opprettetTidspunkt < :t1 order by opprettetTidspunkt desc", ØkonomiXmlSendt.class);
        query.setParameter("meldingType", meldingType);
        query.setParameter("t0", opprettetDato.atStartOfDay());
        query.setParameter("t1", opprettetDato.plusDays(1).atStartOfDay());
        return query.getResultList();
    }

    public void slettSendtXml(Long xmlId) {
        ØkonomiXmlSendt entity = finnSendtXml(xmlId);
        entityManager.remove(entity);
    }

    private ØkonomiXmlSendt finnSendtXml(Long sendtXmlId) {
        return entityManager.find(ØkonomiXmlSendt.class, sendtXmlId);
    }


}
