package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.tilbakekreving.kafka.poller.PostTransactionHandler;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.task.HendelseTaskDataWrapper;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class TilkjentYtelseReaderTest {

    private static final Long BEHANDLING_ID = 123L;
    private static final Long FAGSAK_ID = 1L;
    private static final String AKTØR_ID = "1234567898765";
    private static final String IV_SYSTEM = "FPSAK";

    @Rule
    public RepositoryRule repoRule = new UnittestRepositoryRule();

    private TilkjentYtelseMeldingConsumer meldingConsumer = mock(TilkjentYtelseMeldingConsumer.class);
    private ProsessTaskRepository prosessTaskRepository = spy(new ProsessTaskRepositoryImpl(repoRule.getEntityManager(), null));
    private TilkjentYtelseReader tilkjentYtelseReader = new TilkjentYtelseReader(meldingConsumer, prosessTaskRepository);

    @Test
    public void skal_hente_og_behandle_meldinger() {
        //Arrange
        TilkjentYtelseMelding tilkjentYtelseMelding = opprettTilkjentYtelseMelding();
        when(meldingConsumer.lesMeldinger()).thenReturn(Collections.singletonList(tilkjentYtelseMelding));

        //Act
        PostTransactionHandler postTransactionHandler = tilkjentYtelseReader.hentOgBehandleMeldinger();

        //Assert
        List<ProsessTaskData> prosessTaskDataList = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(prosessTaskDataList).hasSize(1);
        ProsessTaskData prosessTaskData = prosessTaskDataList.get(0);
        verify(prosessTaskRepository).lagre(prosessTaskData);

        HendelseTaskDataWrapper taskDataWrapper = new HendelseTaskDataWrapper(prosessTaskData);
        assertThat(taskDataWrapper.getBehandlingId()).isEqualTo(BEHANDLING_ID);
        assertThat(taskDataWrapper.getAktørId().getId()).isEqualTo(AKTØR_ID);
        assertThat(taskDataWrapper.getFagsakId()).isEqualTo(FAGSAK_ID);
    }

    @Test
    public void skal_vente_med_commit_sync_til_transaksjonen_er_ferdig() {
        TilkjentYtelseMelding tilkjentYtelseMelding = opprettTilkjentYtelseMelding();
        when(meldingConsumer.lesMeldinger()).thenReturn(Collections.singletonList(tilkjentYtelseMelding));
        PostTransactionHandler postTransactionHandler = tilkjentYtelseReader.hentOgBehandleMeldinger();

        verify(meldingConsumer, times(0)).manualCommitSync();

        postTransactionHandler.doAfterTransaction();;
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

    private TilkjentYtelseMelding opprettTilkjentYtelseMelding() {
        TilkjentYtelseMelding melding = new TilkjentYtelseMelding();
        melding.setAktørId(AKTØR_ID);
        melding.setBehandlingId(BEHANDLING_ID);
        melding.setFagsakId(FAGSAK_ID);
        melding.setIverksettingSystem(IV_SYSTEM);

        return melding;
    }
}