package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.tjeneste.HendelseHåndtererTjeneste;

public class HåndterHendelseTaskTest {

    private static final Long FAGSAK_ID = 1244L;
    private static final Long BEHANDLING_ID = 24533L;
    private static final String AKTØR_ID = "83923535";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    HendelseHåndtererTjeneste hendelseHåndterer = mock(HendelseHåndtererTjeneste.class);
    HåndterHendelseTask håndterHendelseTask;

    @Before
    public void setup() {
        håndterHendelseTask = new HåndterHendelseTask(hendelseHåndterer);
    }

    @Test
    public void test_skal_kalle_hendelseHåndterer() {
        //
        HendelseTaskDataWrapper taskDataWrapper = HendelseTaskDataWrapper.lagWrapperForHendelseHåndtering(FAGSAK_ID, BEHANDLING_ID, AKTØR_ID);

        // act
        håndterHendelseTask.doTask(taskDataWrapper.getProsessTaskData());

        // verify
        verify(hendelseHåndterer, atLeastOnce()).håndterHendelse(FAGSAK_ID, BEHANDLING_ID, AKTØR_ID);
    }

}
