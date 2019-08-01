package no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;

public class SpråkKodeverkRepositoryImplTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());
    private SpråkKodeverkRepository repo = new SpråkKodeverkRepositoryImpl(kodeverkRepository);

    @Test
    public void skal_hente_språkKodeverk_for_språkkode() {
        assertThat(repo.finnSpråkMedKodeverkEiersKode("NB")).hasValue(Språkkode.nb);
        assertThat(repo.finnSpråkMedKodeverkEiersKode("NN")).hasValue(Språkkode.nn);
        assertThat(repo.finnSpråkMedKodeverkEiersKode("EN")).hasValue(Språkkode.en);
    }
}
