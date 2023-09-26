package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.enterprise.inject.spi.CDI;
import no.nav.foreldrepenger.tilbakekreving.avstemming.AvstemFraResultatOgIverksettingStatusTjeneste;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.SakshendelserEventObserver;
import no.nav.foreldrepenger.tilbakekreving.kravgrunnlag.queue.consumer.KravgrunnlagAsyncJmsConsumer;
import no.nav.foreldrepenger.tilbakekreving.los.klient.observer.FpLosEventObserver;
import no.nav.foreldrepenger.tilbakekreving.los.klient.observer.K9LosEventObserver;
import no.nav.vedtak.felles.testutilities.cdi.WeldContext;

class AvhengigheterTest {

    static {
        WeldContext.getInstance(); // init cdi container
    }

    @Test
    void skal_ha_følgende_tjenester_på_classpath_slik_at_de_kjører() {
        //har test for dette, siden dersom avhengighetene blir borte, er det ikke sikkert at det umiddelbart oppdages

        Assertions.assertThat(CDI.current().select(SakshendelserEventObserver.class).isResolvable()).isTrue();
        Assertions.assertThat(CDI.current().select(FpLosEventObserver.class).isResolvable()).isTrue();
        Assertions.assertThat(CDI.current().select(K9LosEventObserver.class).isResolvable()).isTrue();
        Assertions.assertThat(CDI.current().select(KravgrunnlagAsyncJmsConsumer.class).isResolvable()).isTrue();
        Assertions.assertThat(CDI.current().select(AvstemFraResultatOgIverksettingStatusTjeneste.class).isResolvable()).isTrue();
    }
}
