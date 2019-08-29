package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TilkjentYtelseTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.tjeneste.HendelseHåndtererTjeneste;

public class HåndterHendelseTaskTest extends TilkjentYtelseTestOppsett {

    HendelseHåndtererTjeneste hendelseHåndterer = mock(HendelseHåndtererTjeneste.class);
    HåndterHendelseTask håndterHendelseTask;

    @Before
    public void setup() {
        håndterHendelseTask = new HåndterHendelseTask(hendelseHåndterer);
    }

    @Test
    public void test_skal_kalle_hendelseHåndterer() {
        //
        HendelseTaskDataWrapper taskDataWrapper = HendelseTaskDataWrapper.lagWrapperForHendelseHåndtering(opprettTilkjentYtelseMelding());

        // act
        håndterHendelseTask.doTask(taskDataWrapper.getProsessTaskData());

        // verify
        verify(hendelseHåndterer, atLeastOnce()).håndterHendelse(any(HendelseTaskDataWrapper.class));
    }

}
