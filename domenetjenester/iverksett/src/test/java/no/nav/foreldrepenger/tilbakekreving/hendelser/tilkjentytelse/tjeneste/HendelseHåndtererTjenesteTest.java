package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TilkjentYtelseTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task.OppdaterBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task.OpprettBehandlingTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class HendelseHåndtererTjenesteTest extends TilkjentYtelseTestOppsett {

    private HendelseHåndtererTjeneste hendelseHåndtererTjeneste;

    private FpsakKlient restKlient = mock(FpsakKlient.class);

    @Before
    public void setup() {
        hendelseHåndtererTjeneste = new HendelseHåndtererTjeneste(prosessTaskRepository, restKlient);
    }

    @Test
    public void skal_opprette_prosesstask_når_relevant_hendelse_er_mottatt() {
        VidereBehandling videreBehandling = VidereBehandling.TILBAKEKREV_I_INFOTRYGD;
        TilbakekrevingValgDto tbkDataDto = new TilbakekrevingValgDto(videreBehandling);
        when(restKlient.hentTilbakekrevingValg(any(UUID.class))).thenReturn(Optional.of(tbkDataDto));

        hendelseHåndtererTjeneste.håndterHendelse(hendelseTaskDataWrapper);

        verify(prosessTaskRepository, atLeastOnce()).lagre(any(ProsessTaskData.class));
        List<ProsessTaskData> prosesser = prosessTaskRepository.finnIkkeStartet();
        assertThat(prosesser).isNotEmpty();
        assertThat(erTaskFinnes(OpprettBehandlingTask.TASKTYPE,prosesser)).isTrue();
    }

    @Test
    public void skal_opprette_prosesstask_når_tilbakekreving_oppdater_er_motatt_som_hendelse(){
        VidereBehandling videreBehandling = VidereBehandling.TILBAKEKR_OPPDATER;
        TilbakekrevingValgDto tbkDataDto = new TilbakekrevingValgDto(videreBehandling);
        when(restKlient.hentTilbakekrevingValg(any(UUID.class))).thenReturn(Optional.of(tbkDataDto));

        hendelseHåndtererTjeneste.håndterHendelse(hendelseTaskDataWrapper);

        verify(prosessTaskRepository, atLeastOnce()).lagre(any(ProsessTaskData.class));
        List<ProsessTaskData> prosesser = prosessTaskRepository.finnIkkeStartet();
        assertThat(prosesser).isNotEmpty();
        assertThat(erTaskFinnes(OppdaterBehandlingTask.TASKTYPE,prosesser)).isTrue();
    }

    @Test
    public void skal_ignorere_hendelse_hvis_ikke_relevant() {
        VidereBehandling videreBehandling = VidereBehandling.IGNORER_TILBAKEKREVING;
        TilbakekrevingValgDto tbkDataDto = new TilbakekrevingValgDto(videreBehandling);
        when(restKlient.hentTilbakekrevingValg(any(UUID.class))).thenReturn(Optional.of(tbkDataDto));

        hendelseHåndtererTjeneste.håndterHendelse(hendelseTaskDataWrapper);

        verifyZeroInteractions(prosessTaskRepository);
    }

    private boolean erTaskFinnes(String taskType,List<ProsessTaskData> prosesser){
        return prosesser.stream()
            .anyMatch(prosess -> taskType.equals(prosess.getTaskType()));
    }

}
