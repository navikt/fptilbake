package no.nav.foreldrepenger.tilbakekreving.behandling.task;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse.HendelseTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

public class OpprettBehandlingTaskTest {
    private static final Long EKSTERN_BEHANDLING_ID = 123L;
    private static final Henvisning HENVISNING = Henvisning.fraEksternBehandlingId(EKSTERN_BEHANDLING_ID);
    private static final Saksnummer SAKSNUMMER = new Saksnummer("1234");
    private static final AktørId AKTØR_ID = new AktørId("1234567898765");
    private static final UUID EKSTERN_BEHANDLING_UUID = UUID.randomUUID();
    private static final FagsakYtelseType FAGSAK_YTELSE_TYPE = FagsakYtelseType.FORELDREPENGER;

    BehandlingTjeneste mockBehandlingTjeneste = mock(BehandlingTjeneste.class);
    OpprettBehandlingTask opprettBehandlingTask;

    @Before
    public void setup() {
        opprettBehandlingTask = new OpprettBehandlingTask(mockBehandlingTjeneste);
    }

    @Test
    public void test_skal_kalle_opprettBehandlingAutomatisk() {
        HendelseTaskDataWrapper taskDataWrapper = HendelseTaskDataWrapper.lagWrapperForOpprettBehandling(EKSTERN_BEHANDLING_UUID.toString(), HENVISNING,
            AKTØR_ID, SAKSNUMMER);
        taskDataWrapper.setFagsakYtelseType(FAGSAK_YTELSE_TYPE);
        taskDataWrapper.setBehandlingType(BehandlingType.TILBAKEKREVING);

        // act
        opprettBehandlingTask.doTask(taskDataWrapper.getProsessTaskData());

        // verify
        verify(mockBehandlingTjeneste).opprettBehandlingAutomatisk(any(Saksnummer.class), any(UUID.class), any(Henvisning.class),
            any(AktørId.class), any(FagsakYtelseType.class), any(BehandlingType.class));
    }

    @Test
    public void test_skal_feile_på_manglende_task_property() {
        HendelseTaskDataWrapper taskDataWrapper = HendelseTaskDataWrapper.lagWrapperForOpprettBehandling(EKSTERN_BEHANDLING_UUID.toString(), HENVISNING,
            AKTØR_ID, SAKSNUMMER);

        // act
        assertThrows(NullPointerException.class, () -> opprettBehandlingTask.doTask(taskDataWrapper.getProsessTaskData()));
    }

}
