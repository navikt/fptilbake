package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TilkjentYtelseTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task.OppdaterBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task.OpprettBehandlingTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class HendelseHåndtererTjenesteTest extends TilkjentYtelseTestOppsett {

    private HendelseHåndtererTjeneste hendelseHåndtererTjeneste;

    @Before
    public void setup() {
        hendelseHåndtererTjeneste = new HendelseHåndtererTjeneste(prosessTaskRepository);
    }

    @Test
    public void skal_opprette_prosesstask_når_relevant_hendelse_er_mottatt() {
        hendelseTaskDataWrapper.setTilbakekrevingValg(VidereBehandling.TILBAKEKREV_I_INFOTRYGD.getKode());
        hendelseHåndtererTjeneste.håndterHendelse(hendelseTaskDataWrapper);

        verify(prosessTaskRepository, atLeastOnce()).lagre(any(ProsessTaskData.class));
        List<ProsessTaskData> prosesser = prosessTaskRepository.finnIkkeStartet();
        assertThat(prosesser).isNotEmpty();
        assertThat(erTaskFinnes(OpprettBehandlingTask.TASKTYPE, prosesser)).isTrue();
    }

    @Test
    public void skal_opprette_prosesstask_når_tilbakekreving_oppdater_er_motatt_som_hendelse() {
        hendelseTaskDataWrapper.setTilbakekrevingValg(VidereBehandling.TILBAKEKR_OPPDATER.getKode());
        hendelseHåndtererTjeneste.håndterHendelse(hendelseTaskDataWrapper);

        verify(prosessTaskRepository, atLeastOnce()).lagre(any(ProsessTaskData.class));
        List<ProsessTaskData> prosesser = prosessTaskRepository.finnIkkeStartet();
        assertThat(prosesser).isNotEmpty();
        assertThat(erTaskFinnes(OppdaterBehandlingTask.TASKTYPE, prosesser)).isTrue();
    }

    @Test
    public void skal_ignorere_hendelse_hvis_ikke_relevant() {
        hendelseTaskDataWrapper.setTilbakekrevingValg(VidereBehandling.INNTREKK.getKode());
        hendelseHåndtererTjeneste.håndterHendelse(hendelseTaskDataWrapper);

        verifyZeroInteractions(prosessTaskRepository);
    }

    private boolean erTaskFinnes(String taskType, List<ProsessTaskData> prosesser) {
        return prosesser.stream()
            .anyMatch(prosess -> taskType.equals(prosess.getTaskType()));
    }

}
