package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task.HendelseTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.kafka.poller.PostTransactionHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;

public class TilkjentYtelseReaderTest extends TilkjentYtelseTestOppsett {

    private TilkjentYtelseMeldingConsumer meldingConsumer = mock(TilkjentYtelseMeldingConsumer.class);
    private TilkjentYtelseReader tilkjentYtelseReader = new TilkjentYtelseReader(meldingConsumer, prosessTaskRepository);

    @Test
    public void skal_hente_og_behandle_meldinger() {
        //Arrange
        TilkjentYtelseMelding tilkjentYtelseMelding = opprettTilkjentYtelseMelding();
        when(meldingConsumer.lesMeldinger()).thenReturn(Collections.singletonList(tilkjentYtelseMelding));

        //Act
        tilkjentYtelseReader.hentOgBehandleMeldinger();

        //Assert
        List<ProsessTaskData> prosessTaskDataList = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTaskDataList).hasSize(1);
        ProsessTaskData prosessTaskData = prosessTaskDataList.get(0);
        verify(prosessTaskRepository).lagre(prosessTaskData);

        HendelseTaskDataWrapper taskDataWrapper = new HendelseTaskDataWrapper(prosessTaskData);
        assertThat(taskDataWrapper.getEksternBehandlingId()).isEqualTo(EKSTERN_BEHANDLING_ID);
        assertThat(taskDataWrapper.getAktørId()).isEqualTo(AKTØR_ID);
        assertThat(taskDataWrapper.getBehandlingUuid()).isEqualTo(EKSTERN_BEHANDLING_UUID.toString());
        assertThat(taskDataWrapper.getFagsakYtelseType()).isEqualByComparingTo(FagsakYtelseType.FORELDREPENGER);
        assertThat(taskDataWrapper.getSaksnummer()).isEqualTo(SAKSNUMMER);
    }

    @Test
    public void skal_vente_med_commit_sync_til_transaksjonen_er_ferdig() {
        TilkjentYtelseMelding tilkjentYtelseMelding = opprettTilkjentYtelseMelding();
        when(meldingConsumer.lesMeldinger()).thenReturn(Collections.singletonList(tilkjentYtelseMelding));
        PostTransactionHandler postTransactionHandler = tilkjentYtelseReader.hentOgBehandleMeldinger();

        verify(meldingConsumer, times(0)).manualCommitSync();

        postTransactionHandler.doAfterTransaction();
        verify(meldingConsumer).manualCommitSync();
    }

    @Test
    public void skal_ikke_opprette_prosess_task_når_det_ikke_finnes_meldinger_lest() {
        //Arrange
        when(meldingConsumer.lesMeldinger()).thenReturn(Collections.emptyList());

        //Act
        tilkjentYtelseReader.hentOgBehandleMeldinger();

        //Assert
        List<ProsessTaskData> prosessTaskDataList = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTaskDataList).isEmpty();
        verify(meldingConsumer, never()).manualCommitSync();
    }


}
