package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse.HendelseTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.kafka.poller.PostTransactionHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

public class TilkjentYtelseReaderTest {
    private static final Saksnummer SAKSNUMMER = new Saksnummer("1234");
    private static final AktørId AKTØR_ID = new AktørId("1234567898765");
    private static final Long EKSTERN_BEHANDLING_ID = 123L;
    private static final UUID EKSTERN_BEHANDLING_UUID = UUID.randomUUID();
    private static final Henvisning HENVISNING = Henvisning.fraEksternBehandlingId(EKSTERN_BEHANDLING_ID);

    private TilkjentYtelseMeldingConsumer meldingConsumer = mock(TilkjentYtelseMeldingConsumer.class);
    private ProsessTaskTjeneste taskTjeneste = mock(ProsessTaskTjeneste.class);
    private TilkjentYtelseReader tilkjentYtelseReader = new TilkjentYtelseReader(meldingConsumer, taskTjeneste);

    @Test
    public void skal_hente_og_behandle_meldinger() {
        //Arrange
        TilkjentYtelseMelding tilkjentYtelseMelding = opprettTilkjentYtelseMelding();
        when(meldingConsumer.lesMeldinger()).thenReturn(Collections.singletonList(tilkjentYtelseMelding));

        //Act
        tilkjentYtelseReader.hentOgBehandleMeldinger();

        //Assert
        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        var prosessTaskData = captor.getValue();

        HendelseTaskDataWrapper taskDataWrapper = new HendelseTaskDataWrapper(prosessTaskData);
        assertThat(taskDataWrapper.getHenvisning()).isEqualTo(HENVISNING);
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
        verifyNoInteractions(taskTjeneste);
        verify(meldingConsumer, never()).manualCommitSync();
    }

    private static TilkjentYtelseMelding opprettTilkjentYtelseMelding() {
        TilkjentYtelseMelding melding = new TilkjentYtelseMelding();
        melding.setAktørId(AKTØR_ID.getId());
        melding.setBehandlingId(EKSTERN_BEHANDLING_ID);
        melding.setIverksettingSystem("FPSAK");
        melding.setBehandlingUuid(EKSTERN_BEHANDLING_UUID);
        melding.setFagsakYtelseType(FagsakYtelseType.FORELDREPENGER.getKode());
        melding.setSaksnummer("1234");

        return melding;
    }


}
