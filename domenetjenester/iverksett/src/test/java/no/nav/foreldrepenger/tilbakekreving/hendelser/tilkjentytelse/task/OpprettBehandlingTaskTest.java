package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

public class OpprettBehandlingTaskTest {

    private static final Saksnummer SAKSNUMMER = new Saksnummer("5354365");
    private static final Long FAGSAK_ID = 1244L;
    private static final Long BEHANDLING_ID = 24533L;
    private static final AktørId AKTØR_ID = new AktørId("83923535");
    private static final FagsakYtelseType FAGSAK_YTELSE = FagsakYtelseType.FORELDREPENGER;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    BehandlingTjeneste mockBehandlingTjeneste = mock(BehandlingTjeneste.class);
    OpprettBehandlingTask opprettBehandlingTask;


    @Before
    public void setup() {
        opprettBehandlingTask = new OpprettBehandlingTask(mockBehandlingTjeneste);
    }

    @Test
    public void test_skal_kalle_opprettBehandlingAutomatisk() {
        HendelseTaskDataWrapper taskDataWrapper = HendelseTaskDataWrapper.lagWrapperForOpprettBehandling(FAGSAK_ID, BEHANDLING_ID, AKTØR_ID.getId());
        taskDataWrapper.setSaksnummer(SAKSNUMMER.getVerdi());
        taskDataWrapper.setFagsakYtelseType(FAGSAK_YTELSE.getKode());
        taskDataWrapper.setBehandlingType(BehandlingType.TILBAKEKREVING.getKode());

        // act
        opprettBehandlingTask.doTask(taskDataWrapper.getProsessTaskData());

        // verify
        verify(mockBehandlingTjeneste).opprettBehandlingAutomatisk(any(Saksnummer.class), anyLong(), anyLong(),
            any(AktørId.class), any(FagsakYtelseType.class), any(BehandlingType.class));
    }

    @Test
    public void test_skal_feile_på_manglende_task_property() {
        HendelseTaskDataWrapper taskDataWrapper = HendelseTaskDataWrapper.lagWrapperForOpprettBehandling(FAGSAK_ID, BEHANDLING_ID, AKTØR_ID.getId());

        expectedException.expect(NullPointerException.class);

        // act
        opprettBehandlingTask.doTask(taskDataWrapper.getProsessTaskData());
    }

}
