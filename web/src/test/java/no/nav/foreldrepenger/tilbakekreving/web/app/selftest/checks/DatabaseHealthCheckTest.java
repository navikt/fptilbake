package no.nav.foreldrepenger.tilbakekreving.web.app.selftest.checks;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.tilbakekreving.dbstoette.FptilbakeEntityManagerAwareExtension;

@ExtendWith(FptilbakeEntityManagerAwareExtension.class)
public class DatabaseHealthCheckTest {


    @Test
    public void test_working_query() {
        assertThat(new DatabaseHealthCheck().isOK()).isTrue();
    }


}
