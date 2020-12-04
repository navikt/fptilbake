package no.nav.foreldrepenger.tilbakekreving.hendelser.k9vedtak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse.TaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.hendelser.ProsessTaskRepositoryMock;
import no.nav.foreldrepenger.tilbakekreving.hendelser.felles.task.HåndterHendelseTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;

public class VedtakHendelseReaderTest {

    private static final String AKTØR_ID = "1234";
    private static final String SAKSNUMMER = "1232423432";
    private static final UUID BEHANDLING_UUID = UUID.randomUUID();

    private VedtakHendelseMeldingConsumer meldingConsumerMock = mock(VedtakHendelseMeldingConsumer.class);
    private ProsessTaskRepository taskRepository = new ProsessTaskRepositoryMock();

    private VedtakHendelseReader vedtakHendelseReader = new VedtakHendelseReader(meldingConsumerMock, taskRepository);

    @Test
    public void skal_lese_og_håndtere_k9_vedtak_hendelser() {
        when(meldingConsumerMock.lesMeldinger()).thenReturn(Lists.newArrayList(lagVedtakHendelse()));
        vedtakHendelseReader.hentOgBehandleMeldinger();
        List<ProsessTaskData> tasker = taskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(tasker).isNotEmpty().hasSize(1);
        ProsessTaskData taskData = tasker.get(0);
        assertThat(taskData.getTaskType()).isEqualTo(HåndterHendelseTask.TASKTYPE);
        assertThat(taskData.getAktørId()).isEqualTo(AKTØR_ID);
        assertThat(taskData.getSaksnummer()).isEqualTo(SAKSNUMMER);
        assertThat(taskData.getPropertyValue(TaskProperties.FAGSAK_YTELSE_TYPE)).isEqualTo(FagsakYtelseType.FRISINN.getKode());
        assertThat(taskData.getPropertyValue(TaskProperties.EKSTERN_BEHANDLING_UUID)).isEqualTo(BEHANDLING_UUID.toString());
        assertThat(taskData.getPropertyValue(TaskProperties.HENVISNING)).isNotNull();
    }

    @Test
    public void skal_lese_men_ikke_håndtere_k9_vedtak_hendelser_når_den_mangler_påkrevd_behandling_uuid() {
        VedtakHendelse vedtakHendelse = lagVedtakHendelse();
        vedtakHendelse.setBehandlingId(null);
        when(meldingConsumerMock.lesMeldinger()).thenReturn(Lists.newArrayList(vedtakHendelse));
        assertThrows(NullPointerException.class, () -> vedtakHendelseReader.hentOgBehandleMeldinger());
    }

    @Test
    public void skal_lese_men_ikke_håndtere_k9_vedtak_hendelser_for_omsorgspenger() {
        VedtakHendelse vedtakHendelse = lagVedtakHendelse();
        vedtakHendelse.setFagsakYtelseType(FagsakYtelseType.OMSORGSPENGER);
        when(meldingConsumerMock.lesMeldinger()).thenReturn(Lists.newArrayList(vedtakHendelse));
        List<ProsessTaskData> tasker = taskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(tasker).isEmpty();
    }

    @Test
    public void skal_lese_men_ikke_håndtere_historiske_k9_vedtak_hendelser() {
        VedtakHendelse vedtakHendelse = lagVedtakHendelse();
        vedtakHendelse.setVedtattTidspunkt(LocalDateTime.of(LocalDate.of(2020, 10, 5), LocalTime.MIDNIGHT));
        when(meldingConsumerMock.lesMeldinger()).thenReturn(Lists.newArrayList(vedtakHendelse));
        List<ProsessTaskData> tasker = taskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(tasker).isEmpty();
    }

    public VedtakHendelse lagVedtakHendelse() {
        VedtakHendelse vedtakHendelse = new VedtakHendelse();
        vedtakHendelse.setAktør(new AktørId(AKTØR_ID));
        vedtakHendelse.setBehandlingId(BEHANDLING_UUID);
        vedtakHendelse.setFagsakYtelseType(FagsakYtelseType.FRISINN);
        vedtakHendelse.setSaksnummer(SAKSNUMMER);
        vedtakHendelse.setVedtattTidspunkt(LocalDateTime.now().plusDays(3));
        return vedtakHendelse;
    }
}
