package no.nav.foreldrepenger.tilbakekreving.økonomixml;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;

@ApplicationScoped
public class ØkonomiMottattXmlRepository {
    private EntityManager entityManager;

    ØkonomiMottattXmlRepository() {
        //for CDI proxy
    }

    @Inject
    public ØkonomiMottattXmlRepository( EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public String hentMottattXml(Long mottattXmlId) {
        return finnMottattXml(mottattXmlId).getMottattXml();
    }

    public ØkonomiXmlMottatt finnMottattXml(Long mottattXmlId) {
        return entityManager.find(ØkonomiXmlMottatt.class, mottattXmlId);
    }

    public Long lagreMottattXml(String xml) {
        ØkonomiXmlMottatt entity = new ØkonomiXmlMottatt(xml);
        entityManager.persist(entity);
        return entity.getId();
    }

    public void slettMottattXml(Long xmlId) {
        ØkonomiXmlMottatt entity = finnMottattXml(xmlId);
        entityManager.remove(entity);
        entityManager.flush();
    }

    public Optional<ØkonomiXmlMottatt> finnForHenvisning(Henvisning henvisning) {
        TypedQuery<ØkonomiXmlMottatt> query = entityManager.createQuery("from ØkonomiXmlMottatt where henvisning=:henvisning", ØkonomiXmlMottatt.class);
        query.setParameter("henvisning", henvisning);
        return hentUniktResultat(query);
    }

    public List<ØkonomiXmlMottatt> finnAlleForSaksnummerSomIkkeErKoblet(String saksnummer) {
        TypedQuery<ØkonomiXmlMottatt> query = entityManager.createQuery("from ØkonomiXmlMottatt where saksnummer=:saksnummer and tilkoblet='N'", ØkonomiXmlMottatt.class);
        query.setParameter("saksnummer", saksnummer);
        return query.getResultList();
    }

    public void oppdaterMedHenvisningOgSaksnummer(Henvisning henvisning, String saksnummer, Long kravgrunnlagXmlId) {
        ØkonomiXmlMottatt entity = finnMottattXml(kravgrunnlagXmlId);
        Long eksisterendeVersjon = finnHøyesteVersjonsnummer(henvisning);
        long nyVersjon = eksisterendeVersjon == null ? 1 : eksisterendeVersjon + 1;
        entity.setHenvisning(henvisning, nyVersjon);
        entity.setSaksnummer(saksnummer);
        entityManager.persist(entity);
    }

    public void opprettTilkobling(Long mottattXmlId) {
        ØkonomiXmlMottatt entity = finnMottattXml(mottattXmlId);
        entity.lagTilkobling();
        entityManager.persist(entity);
    }

    public boolean erMottattXmlTilkoblet(Long mottattXmlId) {
        return finnMottattXml(mottattXmlId).isTilkoblet();
    }

    public List<ØkonomiXmlMottatt> hentAlleMeldingerUtenSaksnummer() {
        TypedQuery<ØkonomiXmlMottatt> query = entityManager.createQuery("from ØkonomiXmlMottatt where saksnummer is null", ØkonomiXmlMottatt.class);
        return query.getResultList();
    }

    public void oppdaterSaksnummer(Long kravgrunnlagXmlId, String saksnummer) {
        ØkonomiXmlMottatt entity = finnMottattXml(kravgrunnlagXmlId);
        entity.setSaksnummer(saksnummer);
        entityManager.persist(entity);
    }

    public List<ØkonomiXmlMottatt> hentGamleUkobledeKravgrunnlagXml(LocalDateTime dato) {
        TypedQuery<ØkonomiXmlMottatt> query = entityManager.createQuery("from ØkonomiXmlMottatt where tilkoblet='N' and opprettetTidspunkt < :dato"+
            " and melding like '%detaljertKravgrunnlagMelding%'", ØkonomiXmlMottatt.class);
        query.setParameter("dato", dato);
        return query.getResultList();
    }

    public void arkiverMottattXml(Long mottattXmlId, String xml) {
        ØkonomiXmlMottattArkiv økonomiXmlMottattArkiv = new ØkonomiXmlMottattArkiv(mottattXmlId, xml);
        entityManager.persist(økonomiXmlMottattArkiv);
        entityManager.flush();
    }

    public ØkonomiXmlMottattArkiv finnArkivertMottattXml(Long mottattXmlId) {
        return entityManager.find(ØkonomiXmlMottattArkiv.class, mottattXmlId);
    }

    public boolean erMottattXmlArkivert(Long mottattXmlId) {
        TypedQuery<Long> query = entityManager.createQuery("select count(1) from ØkonomiXmlMottattArkiv arkiv where arkiv.id=:mottattXmlId", Long.class);
        query.setParameter("mottattXmlId", mottattXmlId);
        return query.getSingleResult() == 1;
    }

    private Long finnHøyesteVersjonsnummer(Henvisning henvisning) {
        Query query = entityManager.createNativeQuery("select max(sekvens) from oko_xml_mottatt where henvisning=:henvisning");
        query.setParameter("henvisning", henvisning.getVerdi());
        Object resultat = query.getSingleResult();
        return resultat != null ? ((BigDecimal) resultat).longValue() : null;
    }
}
