package no.nav.foreldrepenger.tilbakekreving.økonomixml;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;


@ApplicationScoped
public class ØkonomiSendtXmlRepository {

    private EntityManager entityManager;

    ØkonomiSendtXmlRepository() {
        //for CDI proxy
    }

    @Inject
    public ØkonomiSendtXmlRepository(@VLPersistenceUnit EntityManager entityManager) {
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
        TypedQuery<ØkonomiXmlSendt> query = entityManager.createQuery("from OkoXmlSendt where behandling_id = :behandlingId and melding_type =:meldingType", ØkonomiXmlSendt.class);
        query.setParameter("behandlingId", behandlingId);
        query.setParameter("meldingType", meldingType.getKode());
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

    public Optional<ØkonomiXmlSendt> finn(Long behandlingId, MeldingType meldingType) {
        TypedQuery<ØkonomiXmlSendt> query = entityManager.createQuery("from OkoXmlSendt where behandling_id = :behandlingId and melding_type =:meldingType", ØkonomiXmlSendt.class);
        query.setParameter("behandlingId", behandlingId);
        query.setParameter("meldingType", meldingType.getKode());
        return query.getResultList().stream().findFirst();
    }

    public Collection<ØkonomiXmlSendt> finn(MeldingType meldingType, LocalDate opprettetDato) {
        TypedQuery<ØkonomiXmlSendt> query = entityManager.createQuery("from OkoXmlSendt where meldingType = :meldingType and opprettet_tid >= :t0 and opprettetTid < :t1 order by opprettetTid desc", ØkonomiXmlSendt.class);
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
