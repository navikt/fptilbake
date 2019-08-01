package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.tjeneste;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.TilbakekrevingDataDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class HendelseHåndtererTjenesteTest {

    private static final String SAKSNUMMER = "21435";
    private static final String FAGSAK_YTELSE_TYPE = "FP";
    private static final String AKTØR_ID = "4535353532";
    private static final Long FAGSAK_ID = 1234L;
    private static final Long BEHANDLING_ID = 1535235L;

    private HendelseHåndtererTjeneste hendelseHåndtererTjeneste;

    private ProsessTaskRepository taskRepository = mock(ProsessTaskRepository.class);
    private FpsakKlient restKlient = mock(FpsakKlient.class);

    @Before
    public void setup() {
        hendelseHåndtererTjeneste = new HendelseHåndtererTjeneste(taskRepository, restKlient);
    }

    @Test
    public void skal_opprette_prosesstask_når_relevant_hendelse_er_mottatt() {
        String videreBehandling = VidereBehandling.TILBAKEKREV_I_INFOTRYGD.getKode();
        TilbakekrevingDataDto tbkDataDto = new TilbakekrevingDataDto(SAKSNUMMER, FAGSAK_YTELSE_TYPE, videreBehandling);
        when(restKlient.hentTilbakekrevingData(anyLong())).thenReturn(Optional.of(tbkDataDto));

        hendelseHåndtererTjeneste.håndterHendelse(FAGSAK_ID, BEHANDLING_ID, AKTØR_ID);

        verify(taskRepository, atLeastOnce()).lagre(any(ProsessTaskData.class));
    }

    @Test
    public void skal_ignorere_hendelse_hvis_ikke_relevant() {
        String videreBehandling = VidereBehandling.IGNORER_TILBAKEKREVING.getKode();
        TilbakekrevingDataDto tbkDataDto = new TilbakekrevingDataDto(SAKSNUMMER, FAGSAK_YTELSE_TYPE, videreBehandling);
        when(restKlient.hentTilbakekrevingData(anyLong())).thenReturn(Optional.of(tbkDataDto));

        hendelseHåndtererTjeneste.håndterHendelse(FAGSAK_ID, BEHANDLING_ID, AKTØR_ID);

        verifyZeroInteractions(taskRepository);
    }

}
