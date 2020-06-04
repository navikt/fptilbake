package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;

public class ØkonomiXmlMottattRepositoryTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private EntityManager em = repoRule.getEntityManager();

    private ØkonomiMottattXmlRepository repository = new ØkonomiMottattXmlRepository(em);

    @Test
    public void skal_lagre_grunnlag_xml() {
        String xml = "foo";
        Long xmlId = repository.lagreMottattXml(xml);

        ØkonomiXmlMottatt lagret = em.find(ØkonomiXmlMottatt.class, xmlId);
        assertThat(lagret.getMottattXml()).isEqualTo("foo");
    }

    @Test
    public void skal_slette_grunnlag() {
        String xml = "foo";
        Long xmlId = repository.lagreMottattXml(xml);
        repository.slettMottattXml(xmlId);
        em.flush();
        em.clear();

        ØkonomiXmlMottatt lagret = em.find(ØkonomiXmlMottatt.class, xmlId);
        assertThat(lagret).isNull();
    }

    @Test
    public void skal_oppdatere_med_eksernBehandlingId_og_versjon() {
        String xml1 = "foo1";
        String xml2 = "foo2";
        String saksnummer = "1234345";
        String eksternBehandlingId = "123";
        Long id1 = repository.lagreMottattXml(xml1);
        repository.oppdaterMedHenvisningOgSaksnummer(eksternBehandlingId,saksnummer, id1);
        em.flush();
        em.clear();
        Long id2 = repository.lagreMottattXml(xml2);
        repository.oppdaterMedHenvisningOgSaksnummer(eksternBehandlingId, saksnummer, id2);

        ØkonomiXmlMottatt lagret1 = em.find(ØkonomiXmlMottatt.class, id1);
        ØkonomiXmlMottatt lagret2 = em.find(ØkonomiXmlMottatt.class, id2);
        assertThat(lagret1.getSekvens()).isEqualTo(1);
        assertThat(lagret2.getSekvens()).isEqualTo(2);
        assertThat(lagret1.getEksternBehandlingId()).isEqualTo(eksternBehandlingId);
        assertThat(lagret2.getEksternBehandlingId()).isEqualTo(eksternBehandlingId);
        assertThat(lagret1.getSaksnummer()).isEqualTo(saksnummer);
        assertThat(lagret2.getSaksnummer()).isEqualTo(saksnummer);
    }




}
