package no.nav.foreldrepenger.tilbakekreving.hendelser.k9vedtak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.collect.Lists;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse.TaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.hendelser.felles.task.HåndterHendelseTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

public class VedtakHendelseReaderTest {

    private static final String AKTØR_ID = "1234";
    private static final String SAKSNUMMER = "1232423432";
    private static final UUID BEHANDLING_UUID = UUID.randomUUID();

    private VedtakHendelseMeldingConsumer meldingConsumerMock = mock(VedtakHendelseMeldingConsumer.class);
    private ProsessTaskTjeneste taskTjeneste = mock(ProsessTaskTjeneste.class);

    private VedtakHendelseReader vedtakHendelseReader = new VedtakHendelseReader(meldingConsumerMock, taskTjeneste);

    @Test
    public void skal_lese_og_håndtere_k9_vedtak_hendelser() {
        when(meldingConsumerMock.lesMeldinger()).thenReturn(Lists.newArrayList(lagVedtakHendelse()));
        vedtakHendelseReader.hentOgBehandleMeldinger();
        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        var taskData = captor.getValue();
        assertThat(taskData.taskType()).isEqualTo(TaskType.forProsessTask(HåndterHendelseTask.class));
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
        verifyNoInteractions(taskTjeneste);
    }

    @Test
    public void skal_lese_men_ikke_håndtere_historiske_k9_vedtak_hendelser() {
        VedtakHendelse vedtakHendelse = lagVedtakHendelse();
        vedtakHendelse.setVedtattTidspunkt(LocalDateTime.of(LocalDate.of(2020, 10, 5), LocalTime.MIDNIGHT));
        when(meldingConsumerMock.lesMeldinger()).thenReturn(Lists.newArrayList(vedtakHendelse));
        verifyNoInteractions(taskTjeneste);
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
