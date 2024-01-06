package no.nav.foreldrepenger.tilbakekreving.økonomixml;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

@ApplicationScoped
public class ØkonomiMottattXmlRepository {
    private EntityManager entityManager;

    ØkonomiMottattXmlRepository() {
        //for CDI proxy
    }

    @Inject
    public ØkonomiMottattXmlRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public String hentMottattXml(Long mottattXmlId) {
        return finnMottattXml(mottattXmlId).getMottattXml();
    }

    public ØkonomiXmlMottatt finnMottattXml(Long mottattXmlId) {
        return entityManager.find(ØkonomiXmlMottatt.class, mottattXmlId);
    }

    public Long lagreMottattXml(String xml) {
        var entity = new ØkonomiXmlMottatt(xml);
        entityManager.persist(entity);
        return entity.getId();
    }

    public void slettMottattXml(Long xmlId) {
        var entity = finnMottattXml(xmlId);
        entityManager.remove(entity);
        entityManager.flush();
    }

    public Optional<ØkonomiXmlMottatt> finnForHenvisning(Henvisning henvisning) {
        var query = entityManager.createQuery("from ØkonomiXmlMottatt where henvisning=:henvisning", ØkonomiXmlMottatt.class);
        query.setParameter("henvisning", henvisning);
        return hentUniktResultat(query);
    }

    public List<ØkonomiXmlMottatt> finnAlleForSaksnummerSomIkkeErKoblet(String saksnummer) {
        var query = entityManager.createQuery("from ØkonomiXmlMottatt where saksnummer=:saksnummer and tilkoblet=false", ØkonomiXmlMottatt.class);
        query.setParameter("saksnummer", saksnummer);
        return query.getResultList();
    }

    public List<ØkonomiXmlMottatt> finnAlleForSaksnummer(Saksnummer saksnummer) {
        var query = entityManager.createQuery("from ØkonomiXmlMottatt where saksnummer=:saksnummer", ØkonomiXmlMottatt.class);
        query.setParameter("saksnummer", saksnummer);
        return query.getResultList();
    }

    public void oppdaterMedHenvisningOgSaksnummer(Henvisning henvisning, String saksnummer, Long kravgrunnlagXmlId) {
        var entity = finnMottattXml(kravgrunnlagXmlId);
        var eksisterendeVersjon = finnHøyesteVersjonsnummer(henvisning);
        var nyVersjon = eksisterendeVersjon == null ? 1 : eksisterendeVersjon + 1;
        entity.setHenvisning(henvisning, nyVersjon);
        entity.setSaksnummer(saksnummer);
        entityManager.persist(entity);
    }

    public void opprettTilkobling(Long mottattXmlId) {
        var entity = finnMottattXml(mottattXmlId);
        entity.lagTilkobling();
        entityManager.persist(entity);
        entityManager.flush();
    }

    public void fjernTilkobling(Long mottattXmlId) {
        var entity = finnMottattXml(mottattXmlId);
        entity.fjernKobling();
        entityManager.persist(entity);
        entityManager.flush();
    }

    public boolean erMottattXmlTilkoblet(Long mottattXmlId) {
        return finnMottattXml(mottattXmlId).isTilkoblet();
    }

    public List<Long> hentGamleUkobledeKravgrunnlagXmlIds(LocalDateTime dato) {
        var query = entityManager.createQuery("""
                select id from ØkonomiXmlMottatt
                where tilkoblet = false
                and opprettetTidspunkt < :dato
                and mottattXml like '%detaljertKravgrunnlagMelding%'""", Long.class);
        query.setParameter("dato", dato);
        return query.getResultList();
    }

    public void arkiverMottattXml(Long mottattXmlId, String xml) {
        var økonomiXmlMottattArkiv = new ØkonomiXmlMottattArkiv(mottattXmlId, xml);
        entityManager.persist(økonomiXmlMottattArkiv);
        entityManager.flush();
    }

    public ØkonomiXmlMottattArkiv finnArkivertMottattXml(Long mottattXmlId) {
        return entityManager.find(ØkonomiXmlMottattArkiv.class, mottattXmlId);
    }

    public boolean erMottattXmlArkivert(Long mottattXmlId) {
        var query = entityManager.createQuery("select count(1) from ØkonomiXmlMottattArkiv arkiv where arkiv.id=:mottattXmlId", Long.class);
        query.setParameter("mottattXmlId", mottattXmlId);
        return query.getSingleResult() == 1;
    }

    private Long finnHøyesteVersjonsnummer(Henvisning henvisning) {
        var query = entityManager.createNativeQuery("select max(sekvens) from oko_xml_mottatt where henvisning=:henvisning");
        query.setParameter("henvisning", henvisning.getVerdi());
        var resultat = query.getSingleResult();
        return resultat != null ? ((BigDecimal) resultat).longValue() : null;
    }
}
