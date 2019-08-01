package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;

public class KravgrunnlagXmlRepositoryTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private EntityManager em = repoRule.getEntityManager();

    private KravgrunnlagXmlRepository repository = new KravgrunnlagXmlRepository(em);

    @Test
    public void skal_lagre_grunnlag_xml() {
        String xml = "foo";
        Long xmlId = repository.lagreKravgrunnlagXml(xml);

        KravgrunnlagXml lagret = em.find(KravgrunnlagXml.class, xmlId);
        assertThat(lagret.getKravgrunnlagXml()).isEqualTo("foo");
    }

    @Test
    public void skal_slette_grunnlag() {
        String xml = "foo";
        Long xmlId = repository.lagreKravgrunnlagXml(xml);
        repository.slettGrunnlagXml(xmlId);
        em.flush();
        em.clear();

        KravgrunnlagXml lagret = em.find(KravgrunnlagXml.class, xmlId);
        assertThat(lagret).isNull();
    }

    @Test
    public void skal_oppdatere_med_eksernBehandlingId_og_versjon() {
        String xml1 = "foo1";
        String xml2 = "foo2";
        Long id1 = repository.lagreKravgrunnlagXml(xml1);
        repository.oppdaterMedEksternBehandlingId("123", id1);
        em.flush();
        em.clear();
        Long id2 = repository.lagreKravgrunnlagXml(xml2);
        repository.oppdaterMedEksternBehandlingId("123", id2);

        KravgrunnlagXml lagret1 = em.find(KravgrunnlagXml.class, id1);
        KravgrunnlagXml lagret2 = em.find(KravgrunnlagXml.class, id2);
        assertThat(lagret1.getSekvens()).isEqualTo(1);
        assertThat(lagret2.getSekvens()).isEqualTo(2);
        assertThat(lagret1.getEksternBehandlingId()).isEqualTo("123");
        assertThat(lagret2.getEksternBehandlingId()).isEqualTo("123");
    }




}