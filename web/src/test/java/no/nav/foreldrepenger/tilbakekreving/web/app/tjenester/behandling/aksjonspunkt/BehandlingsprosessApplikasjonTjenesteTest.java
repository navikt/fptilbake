package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.AsyncPollingStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

class BehandlingsprosessApplikasjonTjenesteTest {

    private static final String GRUPPE_1 = "gruppe1";

    private final ProsessTaskData taskData = ProsessTaskData.forTaskType(new TaskType("taskType1"));
    private final Behandling behandling;

    BehandlingsprosessApplikasjonTjenesteTest() {
        this.taskData.setGruppe(GRUPPE_1);
        this.behandling = ScenarioSimple.simple().lagMocked();
    }

    @Test
    void skal_returnere_gruppe_når_ikke_er_kjørt() {

        BehandlingsprosessApplikasjonTjenesteImpl sut = initSut(GRUPPE_1, taskData);
        Optional<AsyncPollingStatus> status = sut.sjekkProsessTaskPågårForBehandling(behandling, null);
        assertThat(status.get().getStatus()).isEqualTo(AsyncPollingStatus.Status.PENDING);

        status = sut.sjekkProsessTaskPågårForBehandling(behandling, GRUPPE_1);
        assertThat(status.get().getStatus()).isEqualTo(AsyncPollingStatus.Status.PENDING);
    }

    @Test
    void skal_ikke_returnere_gruppe_når_er_kjørt() {
        markerFerdig(taskData);

        BehandlingsprosessApplikasjonTjenesteImpl sut = initSut(GRUPPE_1, taskData);
        Optional<AsyncPollingStatus> status = sut.sjekkProsessTaskPågårForBehandling(behandling, null);
        assertThat(status).isEmpty();

        status = sut.sjekkProsessTaskPågårForBehandling(behandling, GRUPPE_1);
        assertThat(status).isEmpty();

    }

    @Test
    void skal_kaste_exception_når_task_har_feilet_null_gruppe() {
        markerFeilet(taskData);

        BehandlingsprosessApplikasjonTjenesteImpl sut = initSut(GRUPPE_1, taskData);
        Optional<AsyncPollingStatus> status = sut.sjekkProsessTaskPågårForBehandling(behandling, null);

        assertThat(status.get().getStatus()).isEqualTo(AsyncPollingStatus.Status.HALTED);
    }

    @Test
    void skal_kaste_exception_når_task_har_feilet_angitt_gruppe() {
        markerFeilet(taskData);

        BehandlingsprosessApplikasjonTjenesteImpl sut = initSut(GRUPPE_1, taskData);

        Optional<AsyncPollingStatus> status = sut.sjekkProsessTaskPågårForBehandling(behandling, GRUPPE_1);

        assertThat(status.get().getStatus()).isEqualTo(AsyncPollingStatus.Status.HALTED);
    }

    @Test
    void skal_kaste_exception_når_task_neste_kjøring_er_utsatt() {
        taskData.medNesteKjøringEtter(LocalDateTime.now().plusHours(1));

        BehandlingsprosessApplikasjonTjenesteImpl sut = initSut(GRUPPE_1, taskData);
        Optional<AsyncPollingStatus> status = sut.sjekkProsessTaskPågårForBehandling(behandling, GRUPPE_1);

        assertThat(status.get().getStatus()).isEqualTo(AsyncPollingStatus.Status.DELAYED);

    }


    private void markerFeilet(ProsessTaskData pt) {
        pt.setStatus(ProsessTaskStatus.FEILET);
        pt.setAntallFeiledeForsøk(pt.getAntallFeiledeForsøk() + 1);
        pt.setNesteKjøringEtter(null);
        pt.setSistKjørt(LocalDateTime.now());
    }

    private void markerFerdig(ProsessTaskData pt) {
        pt.setStatus(ProsessTaskStatus.FERDIG);
        pt.setNesteKjøringEtter(null);
        pt.setSistKjørt(LocalDateTime.now());
    }

    private BehandlingsprosessApplikasjonTjenesteImpl initSut(String gruppe, ProsessTaskData taskData) {
        BehandlingskontrollAsynkTjeneste tjeneste = Mockito.mock(BehandlingskontrollAsynkTjeneste.class);

        Map<String, ProsessTaskData> data = new HashMap<>();
        data.put(gruppe, taskData);

        Mockito.when(tjeneste.sjekkProsessTaskPågårForBehandling(Mockito.any(), Mockito.any())).thenReturn(data);
        BehandlingsprosessApplikasjonTjenesteImpl sut = new BehandlingsprosessApplikasjonTjenesteImpl(tjeneste);
        return sut;
    }
}
