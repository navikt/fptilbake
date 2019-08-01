package no.nav.foreldrepenger.tilbakekreving.behandlingslager.aktør;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.NavBrukerKjønn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.PersonstatusType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;

public class NavBrukerKodeverkRepositoryImplTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());
    private NavBrukerKodeverkRepository repo = new NavBrukerKodeverkRepositoryImpl(kodeverkRepository);

    @Test
    public void skal_hente_bruker_kjønn_kodeverk_for_kode() {
        assertThat(repo.finnBrukerKjønn("M")).isEqualTo(NavBrukerKjønn.MANN);
        assertThat(repo.finnBrukerKjønn("K")).isEqualTo(NavBrukerKjønn.KVINNE);
    }

    @Test
    public void skal_hente_bruker_status_kodeverk_for_kode() {
        assertThat(repo.finnPersonstatus("ABNR")).isEqualTo(PersonstatusType.ABNR);
        assertThat(repo.finnPersonstatus("ADNR")).isEqualTo(PersonstatusType.ADNR);
        assertThat(repo.finnPersonstatus("BOSA")).isEqualTo(PersonstatusType.BOSA);
        assertThat(repo.finnPersonstatus("DØD")).isEqualTo(PersonstatusType.DØD);
        assertThat(repo.finnPersonstatus("FOSV")).isEqualTo(PersonstatusType.FOSV);
        assertThat(repo.finnPersonstatus("FØDR")).isEqualTo(PersonstatusType.FØDR);
        assertThat(repo.finnPersonstatus("UFUL")).isEqualTo(PersonstatusType.UFUL);
        assertThat(repo.finnPersonstatus("UREG")).isEqualTo(PersonstatusType.UREG);
        assertThat(repo.finnPersonstatus("UTAN")).isEqualTo(PersonstatusType.UTAN);
        assertThat(repo.finnPersonstatus("UTPE")).isEqualTo(PersonstatusType.UTPE);
        assertThat(repo.finnPersonstatus("UTVA")).isEqualTo(PersonstatusType.UTVA);
    }

}
