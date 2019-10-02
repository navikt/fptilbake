package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TilkjentYtelseTestOppsett;

public class OppdaterBehandlingTaskTest extends TilkjentYtelseTestOppsett {

    private BehandlingTjeneste mockBehandlingTjeneste = mock(BehandlingTjeneste.class);
    private OppdaterBehandlingTask oppdaterBehandlingTask = new OppdaterBehandlingTask(mockBehandlingTjeneste);

    @Test
    public void skal_oppdatere_behandling_med_gyldig_data(){
        HendelseTaskDataWrapper hendelseTaskDataWrapper = HendelseTaskDataWrapper.lagWrapperForOppdaterBehandling(EKSTERN_BEHANDLING_UUID.toString(),EKSTERN_BEHANDLING_ID,AKTØR_ID,SAKSNUMMER);
        oppdaterBehandlingTask.doTask(hendelseTaskDataWrapper.getProsessTaskData());

        verify(mockBehandlingTjeneste,atLeastOnce()).oppdaterBehandlingMedEksternReferanse(SAKSNUMMER,EKSTERN_BEHANDLING_ID,EKSTERN_BEHANDLING_UUID);
    }

    @Test(expected = NullPointerException.class)
    public void skal_oppdatere_behandling_med_ugyldig_data(){
        HendelseTaskDataWrapper hendelseTaskDataWrapper = HendelseTaskDataWrapper.lagWrapperForOppdaterBehandling(null,EKSTERN_BEHANDLING_ID,AKTØR_ID,SAKSNUMMER);

        oppdaterBehandlingTask.doTask(hendelseTaskDataWrapper.getProsessTaskData());
    }

}
