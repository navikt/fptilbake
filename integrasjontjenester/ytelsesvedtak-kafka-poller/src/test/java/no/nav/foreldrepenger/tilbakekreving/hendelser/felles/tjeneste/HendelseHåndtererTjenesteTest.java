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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse.TaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.hendelser.ProsessTaskRepositoryMock;
import no.nav.foreldrepenger.tilbakekreving.hendelser.felles.task.HåndterHendelseTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class HendelseHåndtererTjenesteTest {
    private static final Saksnummer SAKSNUMMER = new Saksnummer("1234");
    private static final AktørId AKTØR_ID = new AktørId("1234567898765");
    private static final Long EKSTERN_BEHANDLING_ID = 123L;
    private static final UUID EKSTERN_BEHANDLING_UUID = UUID.randomUUID();

    private FagsystemKlient restKlient = mock(FagsystemKlient.class);
    private ProsessTaskRepository prosessTaskRepository = new ProsessTaskRepositoryMock();
    private HendelseTaskDataWrapper hendelseTaskDataWrapper = lagHendelseTask();
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

    private static HendelseTaskDataWrapper lagHendelseTask() {

        ProsessTaskData prosessTaskData = new ProsessTaskData(HåndterHendelseTask.TASKTYPE);

        prosessTaskData.setProperty(TaskProperties.EKSTERN_BEHANDLING_UUID, EKSTERN_BEHANDLING_UUID.toString());
        prosessTaskData.setProperty(TaskProperties.EKSTERN_BEHANDLING_ID, String.valueOf(EKSTERN_BEHANDLING_ID));
        prosessTaskData.setAktørId(AKTØR_ID.getId());
        prosessTaskData.setProperty(TaskProperties.SAKSNUMMER, SAKSNUMMER.getVerdi());
        prosessTaskData.setProperty(TaskProperties.FAGSAK_YTELSE_TYPE, FagsakYtelseType.FORELDREPENGER.getKode());
        return new HendelseTaskDataWrapper(prosessTaskData);
    }
}
