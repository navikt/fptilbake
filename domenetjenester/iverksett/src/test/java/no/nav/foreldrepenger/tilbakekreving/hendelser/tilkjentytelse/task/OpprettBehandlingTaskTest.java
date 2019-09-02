package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TilkjentYtelseTestOppsett;

public class OpprettBehandlingTaskTest extends TilkjentYtelseTestOppsett {

    BehandlingTjeneste mockBehandlingTjeneste = mock(BehandlingTjeneste.class);
    OpprettBehandlingTask opprettBehandlingTask;


    @Before
    public void setup() {
        opprettBehandlingTask = new OpprettBehandlingTask(mockBehandlingTjeneste);
    }

    @Test
    public void test_skal_kalle_opprettBehandlingAutomatisk() {
        HendelseTaskDataWrapper taskDataWrapper = HendelseTaskDataWrapper.lagWrapperForOpprettBehandling(EKSTERN_BEHANDLING_UUID.toString(), EKSTERN_BEHANDLING_ID, AKTØR_ID);
        taskDataWrapper.setSaksnummer(SAKSNUMMER);
        taskDataWrapper.setFagsakYtelseType(FAGSAK_YTELSE_TYPE);
        taskDataWrapper.setBehandlingType(BehandlingType.TILBAKEKREVING);

        // act
        opprettBehandlingTask.doTask(taskDataWrapper.getProsessTaskData());

        // verify
        verify(mockBehandlingTjeneste).opprettBehandlingAutomatisk(any(Saksnummer.class), any(UUID.class), anyLong(),
            any(AktørId.class), any(FagsakYtelseType.class), any(BehandlingType.class));
    }

    @Test
    public void test_skal_feile_på_manglende_task_property() {
        HendelseTaskDataWrapper taskDataWrapper = HendelseTaskDataWrapper.lagWrapperForOpprettBehandling(EKSTERN_BEHANDLING_UUID.toString(), EKSTERN_BEHANDLING_ID, AKTØR_ID);

        expectedException.expect(NullPointerException.class);

        // act
        opprettBehandlingTask.doTask(taskDataWrapper.getProsessTaskData());
    }

}
