package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Period;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.konfig.KonfigVerdi;

@RunWith(CdiRunner.class)
public class DatabaseKonfigVerdiProviderTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    @KonfigVerdi(value = "behandling.venter.frist.lengde")
    private Period behandlingFrist;

    @Test
    public void skal_ha_injisert_kjent_konfig_verdi_fra_databasen() {
        assertThat(behandlingFrist).isNotNull();
    }

}