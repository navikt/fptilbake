package no.nav.foreldrepenger.tilbakekreving.behandling.task;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse.HendelseTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

public class OppdaterBehandlingTaskTest {

    private static final Long EKSTERN_BEHANDLING_ID = 123L;
    private static final Henvisning HENVISNING = Henvisning.fraEksternBehandlingId(EKSTERN_BEHANDLING_ID);
    private static final Saksnummer SAKSNUMMER = new Saksnummer("1234");
    private static final AktørId AKTØR_ID = new AktørId("1234567898765");
    private static final UUID EKSTERN_BEHANDLING_UUID = UUID.randomUUID();

    private BehandlingTjeneste mockBehandlingTjeneste = mock(BehandlingTjeneste.class);
    private OppdaterBehandlingTask oppdaterBehandlingTask = new OppdaterBehandlingTask(mockBehandlingTjeneste);

    @Test
    public void skal_oppdatere_behandling_med_gyldig_data() {
        HendelseTaskDataWrapper hendelseTaskDataWrapper = HendelseTaskDataWrapper.lagWrapperForOppdaterBehandling(EKSTERN_BEHANDLING_UUID.toString(), HENVISNING,
            AKTØR_ID, SAKSNUMMER);
        oppdaterBehandlingTask.doTask(hendelseTaskDataWrapper.getProsessTaskData());

        verify(mockBehandlingTjeneste, atLeastOnce()).oppdaterBehandlingMedEksternReferanse(SAKSNUMMER, HENVISNING, EKSTERN_BEHANDLING_UUID);
    }

    @Test(expected = NullPointerException.class)
    public void skal_oppdatere_behandling_med_ugyldig_data() {
        HendelseTaskDataWrapper hendelseTaskDataWrapper = HendelseTaskDataWrapper.lagWrapperForOppdaterBehandling(null, HENVISNING,
            AKTØR_ID, SAKSNUMMER);

        oppdaterBehandlingTask.doTask(hendelseTaskDataWrapper.getProsessTaskData());
    }

}
