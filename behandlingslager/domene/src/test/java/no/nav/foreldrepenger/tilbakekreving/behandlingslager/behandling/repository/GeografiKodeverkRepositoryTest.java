package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.personopplysning.PersonstatusType;
import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;

public class GeografiKodeverkRepositoryTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private GeografiKodeverkRepository repo = new GeografiKodeverkRepository(repoRule.getEntityManager());

    @Test
    public void skal_verifisere_kodeverk_som_mottas_fra_regelmotor() {
        assertThat(repo.personstatusTyperFortsattBehandling()).contains(PersonstatusType.DØD);
    }
}
