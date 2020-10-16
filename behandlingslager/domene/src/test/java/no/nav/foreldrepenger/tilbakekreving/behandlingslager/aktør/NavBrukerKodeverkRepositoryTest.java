package no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.NavBrukerKjønn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;

public class NavBrukerKodeverkRepositoryTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());
    private NavBrukerKodeverkRepository repo = new NavBrukerKodeverkRepository(kodeverkRepository);

    @Test
    public void skal_hente_bruker_kjønn_kodeverk_for_kode() {
        assertThat(repo.finnBrukerKjønn("M")).isEqualTo(NavBrukerKjønn.MANN);
        assertThat(repo.finnBrukerKjønn("K")).isEqualTo(NavBrukerKjønn.KVINNE);
    }


}
