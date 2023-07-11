package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.dbstoette.JpaExtension;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;

@ExtendWith(JpaExtension.class)
class ØkonomiXmlMottattRepositoryTest {

    private ØkonomiMottattXmlRepository repository;
    private EntityManager em;

    @BeforeEach
    void setUp(EntityManager em) {
        this.em = em;
        repository = new ØkonomiMottattXmlRepository(em);
    }

    @Test
    void skal_lagre_grunnlag_xml() {
        var xml = "foo";
        var xmlId = repository.lagreMottattXml(xml);

        var lagret = em.find(ØkonomiXmlMottatt.class, xmlId);
        assertThat(lagret.getMottattXml()).isEqualTo("foo");
    }

    @Test
    void skal_slette_grunnlag() {
        var xml = "foo";
        var xmlId = repository.lagreMottattXml(xml);
        repository.slettMottattXml(xmlId);
        em.flush();
        em.clear();

        var lagret = em.find(ØkonomiXmlMottatt.class, xmlId);
        assertThat(lagret).isNull();
    }

    @Test
    void skal_oppdatere_med_eksernBehandlingId_og_versjon() {
        var xml1 = "foo1";
        var xml2 = "foo2";
        var saksnummer = "1234345";
        var henvisning = Henvisning.fraEksternBehandlingId(123L);
        var id1 = repository.lagreMottattXml(xml1);
        repository.oppdaterMedHenvisningOgSaksnummer(henvisning, saksnummer, id1);
        em.flush();
        em.clear();
        var id2 = repository.lagreMottattXml(xml2);
        repository.oppdaterMedHenvisningOgSaksnummer(henvisning, saksnummer, id2);

        var lagret1 = em.find(ØkonomiXmlMottatt.class, id1);
        var lagret2 = em.find(ØkonomiXmlMottatt.class, id2);
        assertThat(lagret1.getSekvens()).isEqualTo(1);
        assertThat(lagret2.getSekvens()).isEqualTo(2);
        assertThat(lagret1.getHenvisning()).isEqualTo(henvisning);
        assertThat(lagret2.getHenvisning()).isEqualTo(henvisning);
        assertThat(lagret1.getSaksnummer()).isEqualTo(saksnummer);
        assertThat(lagret2.getSaksnummer()).isEqualTo(saksnummer);
    }


}
