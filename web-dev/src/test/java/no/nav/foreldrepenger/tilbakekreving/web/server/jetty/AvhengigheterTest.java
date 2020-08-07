package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

import javax.enterprise.inject.spi.CDI;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.tilbakekreving.avstemming.batch.AvstemmingBatchTjeneste;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.SakshendelserEventObserver;
import no.nav.foreldrepenger.tilbakekreving.fplos.klient.observer.FplosEventObserver;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TilkjentYtelseKafkaPoller;
import no.nav.foreldrepenger.tilbakekreving.kravgrunnlag.queue.consumer.KravgrunnlagAsyncJmsConsumer;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class AvhengigheterTest {

    @Test
    public void skal_ha_følgende_tjenester_på_classpath_slik_at_de_kjører() {
        //har test for dette, siden dersom avhengighetene blir borte, er det ikke sikkert at det umiddelbart oppdages

        Assertions.assertThat(CDI.current().select(SakshendelserEventObserver.class).isResolvable()).isTrue();
        Assertions.assertThat(CDI.current().select(FplosEventObserver.class).isResolvable()).isTrue();
        Assertions.assertThat(CDI.current().select(KravgrunnlagAsyncJmsConsumer.class).isResolvable()).isTrue();
        Assertions.assertThat(CDI.current().select(TilkjentYtelseKafkaPoller.class).isResolvable()).isTrue();
        Assertions.assertThat(CDI.current().select(AvstemmingBatchTjeneste.class).isResolvable()).isTrue();
    }
}
