package no.nav.foreldrepenger.tilbakekreving.hendelser.felles.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.task.OppdaterBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.behandling.task.OpprettBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse.HendelseTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.hendelser.ProsessTaskRepositoryMock;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TilkkjentYtelseMeldingTestUtil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class HendelseHåndtererTjenesteTest {

    private FagsystemKlient restKlient = mock(FagsystemKlient.class);
    private ProsessTaskRepository prosessTaskRepository = new ProsessTaskRepositoryMock();
    private HendelseTaskDataWrapper hendelseTaskDataWrapper = TilkkjentYtelseMeldingTestUtil.lagHendelseTask();
    private HendelseHåndtererTjeneste hendelseHåndtererTjeneste = new HendelseHåndtererTjeneste(prosessTaskRepository, restKlient);

    @Test
    public void skal_opprette_prosesstask_når_relevant_hendelse_er_mottatt() {
        VidereBehandling videreBehandling = VidereBehandling.TILBAKEKREV_I_INFOTRYGD;
        TilbakekrevingValgDto tbkDataDto = new TilbakekrevingValgDto(videreBehandling);
        when(restKlient.hentTilbakekrevingValg(any(UUID.class))).thenReturn(Optional.of(tbkDataDto));

        hendelseHåndtererTjeneste.håndterHendelse(hendelseTaskDataWrapper);

        List<ProsessTaskData> prosesser = prosessTaskRepository.finnIkkeStartet();
        assertThat(prosesser).isNotEmpty();
        assertThat(erTaskFinnes(OpprettBehandlingTask.TASKTYPE, prosesser)).isTrue();
    }

    @Test
    public void skal_opprette_prosesstask_når_tilbakekr_opprett_hendelse_er_mottatt() {
        VidereBehandling videreBehandling = VidereBehandling.TILBAKEKR_OPPRETT;
        TilbakekrevingValgDto tbkDataDto = new TilbakekrevingValgDto(videreBehandling);
        when(restKlient.hentTilbakekrevingValg(any(UUID.class))).thenReturn(Optional.of(tbkDataDto));

        hendelseHåndtererTjeneste.håndterHendelse(hendelseTaskDataWrapper);

        List<ProsessTaskData> prosesser = prosessTaskRepository.finnIkkeStartet();
        assertThat(prosesser).isNotEmpty();
        assertThat(erTaskFinnes(OpprettBehandlingTask.TASKTYPE, prosesser)).isTrue();
    }

    @Test
    public void skal_opprette_prosesstask_når_tilbakekreving_oppdater_er_motatt_som_hendelse() {
        VidereBehandling videreBehandling = VidereBehandling.TILBAKEKR_OPPDATER;
        TilbakekrevingValgDto tbkDataDto = new TilbakekrevingValgDto(videreBehandling);
        when(restKlient.hentTilbakekrevingValg(any(UUID.class))).thenReturn(Optional.of(tbkDataDto));

        hendelseHåndtererTjeneste.håndterHendelse(hendelseTaskDataWrapper);

        List<ProsessTaskData> prosesser = prosessTaskRepository.finnIkkeStartet();
        assertThat(prosesser).isNotEmpty();
        assertThat(erTaskFinnes(OppdaterBehandlingTask.TASKTYPE, prosesser)).isTrue();
    }

    @Test
    public void skal_ignorere_hendelse_hvis_ikke_relevant() {
        VidereBehandling videreBehandling = VidereBehandling.IGNORER_TILBAKEKREVING;
        TilbakekrevingValgDto tbkDataDto = new TilbakekrevingValgDto(videreBehandling);
        when(restKlient.hentTilbakekrevingValg(any(UUID.class))).thenReturn(Optional.of(tbkDataDto));

        hendelseHåndtererTjeneste.håndterHendelse(hendelseTaskDataWrapper);

        assertThat(prosessTaskRepository.finnIkkeStartet()).isEmpty();
    }

    private boolean erTaskFinnes(String taskType, List<ProsessTaskData> prosesser) {
        return prosesser.stream()
            .anyMatch(prosess -> taskType.equals(prosess.getTaskType()));
    }

}
