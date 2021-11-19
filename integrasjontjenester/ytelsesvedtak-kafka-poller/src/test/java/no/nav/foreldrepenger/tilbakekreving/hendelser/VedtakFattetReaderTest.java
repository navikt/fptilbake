package no.nav.foreldrepenger.tilbakekreving.hendelser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.collect.Lists;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.vedtak.ytelse.Aktør;
import no.nav.abakus.vedtak.ytelse.Periode;
import no.nav.abakus.vedtak.ytelse.v1.YtelseV1;
import no.nav.foreldrepenger.tilbakekreving.behandling.task.HendelseTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.kafka.poller.PostTransactionHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

public class VedtakFattetReaderTest {

    private static final String AKTØR_ID = "1000100010001";
    private static final String SAKSNUMMER_K9 = "B34C2";
    private static final String SAKSNUMMER_FP = "123456789";
    private static final UUID BEHANDLING_UUID = UUID.randomUUID();

    private ProsessTaskTjeneste taskTjeneste = mock(ProsessTaskTjeneste.class);

    private VedtakFattetMeldingConsumer meldingConsumer = mock(VedtakFattetMeldingConsumer.class);


    @Test
    public void skal_hente_og_behandle_meldinger() {
        //Arrange
        var reader = new VedtakFattetReader(meldingConsumer, taskTjeneste, no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem.FPTILBAKE);
        var melding = opprettYtelseMelding(Fagsystem.FPSAK, YtelseType.FORELDREPENGER, SAKSNUMMER_FP);
        when(meldingConsumer.lesMeldinger()).thenReturn(Collections.singletonList(melding));

        //Act
        reader.hentOgBehandleMeldinger();

        //Assert
        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        var prosessTaskData = captor.getValue();

        HendelseTaskDataWrapper taskDataWrapper = new HendelseTaskDataWrapper(prosessTaskData);
        assertThat(prosessTaskData.taskType()).isEqualTo(TaskType.forProsessTask(HåndterVedtakFattetTask.class));
        assertThat(taskDataWrapper.getAktørId().getId()).isEqualTo(AKTØR_ID);
        assertThat(taskDataWrapper.getBehandlingUuid()).isEqualTo(BEHANDLING_UUID);
        assertThat(taskDataWrapper.getFagsakYtelseType()).isEqualByComparingTo(FagsakYtelseType.FORELDREPENGER);
        assertThat(taskDataWrapper.getSaksnummer().getVerdi()).isEqualTo(SAKSNUMMER_FP);
    }

    @Test
    public void skal_vente_med_commit_sync_til_transaksjonen_er_ferdig() {
        var reader = new VedtakFattetReader(meldingConsumer, taskTjeneste, no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem.FPTILBAKE);
        var melding = opprettYtelseMelding(Fagsystem.FPSAK, YtelseType.FORELDREPENGER, SAKSNUMMER_FP);
        when(meldingConsumer.lesMeldinger()).thenReturn(Collections.singletonList(melding));
        PostTransactionHandler postTransactionHandler = reader.hentOgBehandleMeldinger();

        verify(meldingConsumer, times(0)).manualCommitSync();

        postTransactionHandler.doAfterTransaction();
        verify(meldingConsumer).manualCommitSync();
    }

    @Test
    public void skal_ikke_opprette_prosess_task_når_det_ikke_finnes_meldinger_lest() {
        //Arrange
        var reader = new VedtakFattetReader(meldingConsumer, taskTjeneste, no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem.FPTILBAKE);
        when(meldingConsumer.lesMeldinger()).thenReturn(Collections.emptyList());

        //Act
        reader.hentOgBehandleMeldinger();

        //Assert
        verifyNoInteractions(taskTjeneste);
        verify(meldingConsumer, never()).manualCommitSync();
    }


    @Test
    public void skal_lese_og_håndtere_k9_vedtak_hendelser() {
        var reader = new VedtakFattetReader(meldingConsumer, taskTjeneste, no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem.K9TILBAKE);
        when(meldingConsumer.lesMeldinger()).thenReturn(Lists.newArrayList(opprettYtelseMelding(Fagsystem.K9SAK, YtelseType.PLEIEPENGER_SYKT_BARN, SAKSNUMMER_K9)));
        reader.hentOgBehandleMeldinger();
        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        var taskData = captor.getValue();
        assertThat(taskData.taskType()).isEqualTo(TaskType.forProsessTask(HåndterVedtakFattetTask.class));
        assertThat(taskData.getAktørId()).isEqualTo(AKTØR_ID);
        assertThat(taskData.getSaksnummer()).isEqualTo(SAKSNUMMER_K9);
        assertThat(taskData.getPropertyValue(TaskProperties.FAGSAK_YTELSE_TYPE)).isEqualTo(FagsakYtelseType.PLEIEPENGER_SYKT_BARN.getKode());
        assertThat(taskData.getPropertyValue(TaskProperties.EKSTERN_BEHANDLING_UUID)).isEqualTo(BEHANDLING_UUID.toString());
    }

    private static YtelseV1 opprettYtelseMelding(Fagsystem system, YtelseType ytelseType, String saksnummer) {
        var aktør = new Aktør();
        aktør.setVerdi(AKTØR_ID);
        var periode = new Periode();
        periode.setFom(LocalDate.now().minusDays(1));
        periode.setTom(LocalDate.now().plusMonths(1));
        var melding = new YtelseV1();
        melding.setAktør(aktør);
        melding.setFagsystem(system);
        melding.setVedtakReferanse(BEHANDLING_UUID.toString());
        melding.setVedtattTidspunkt(LocalDateTime.now().minusSeconds(10));
        melding.setType(ytelseType);
        melding.setStatus(YtelseStatus.LØPENDE);
        melding.setPeriode(periode);
        melding.setSaksnummer(saksnummer);
        return melding;
    }
}
