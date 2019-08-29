package no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.tjeneste;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TilkjentYtelseTestOppsett;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class HendelseHåndtererTjenesteTest extends TilkjentYtelseTestOppsett {

    private HendelseHåndtererTjeneste hendelseHåndtererTjeneste;

    private ProsessTaskRepository taskRepository = mock(ProsessTaskRepository.class);
    private FpsakKlient restKlient = mock(FpsakKlient.class);

    @Before
    public void setup() {
        hendelseHåndtererTjeneste = new HendelseHåndtererTjeneste(taskRepository, restKlient);
    }

    @Test
    public void skal_opprette_prosesstask_når_relevant_hendelse_er_mottatt() {
        VidereBehandling videreBehandling = VidereBehandling.TILBAKEKREV_I_INFOTRYGD;
        TilbakekrevingValgDto tbkDataDto = new TilbakekrevingValgDto(videreBehandling);
        when(restKlient.hentTilbakekrevingValg(anyString())).thenReturn(Optional.of(tbkDataDto));

        hendelseHåndtererTjeneste.håndterHendelse(hendelseTaskDataWrapper);

        verify(taskRepository, atLeastOnce()).lagre(any(ProsessTaskData.class));
    }

    @Test
    public void skal_ignorere_hendelse_hvis_ikke_relevant() {
        VidereBehandling videreBehandling = VidereBehandling.IGNORER_TILBAKEKREVING;
        TilbakekrevingValgDto tbkDataDto = new TilbakekrevingValgDto(videreBehandling);
        when(restKlient.hentTilbakekrevingValg(anyString())).thenReturn(Optional.of(tbkDataDto));

        hendelseHåndtererTjeneste.håndterHendelse(hendelseTaskDataWrapper);

        verifyZeroInteractions(taskRepository);
    }

}
