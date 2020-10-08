package no.nav.foreldrepenger.tilbakekreving.hendelser.felles.task;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse.HendelseTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.hendelse.TaskProperties;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.hendelser.felles.tjeneste.HendelseHåndtererTjeneste;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TilkjentYtelseMelding;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TilkjentYtelseTestOppsett;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class HåndterHendelseTaskTest extends TilkjentYtelseTestOppsett {

    HendelseHåndtererTjeneste hendelseHåndterer = mock(HendelseHåndtererTjeneste.class);
    HåndterHendelseTask håndterHendelseTask;

    @Before
    public void setup() {
        håndterHendelseTask = new HåndterHendelseTask(hendelseHåndterer);
    }

    @Test
    public void test_skal_kalle_hendelseHåndterer() {
        //
        ProsessTaskData prosessTaskData = lagProsessTaskData(opprettTilkjentYtelseMelding());

        // act
        håndterHendelseTask.doTask(prosessTaskData);

        // verify
        verify(hendelseHåndterer, atLeastOnce()).håndterHendelse(any(HendelseTaskDataWrapper.class));
    }

    private ProsessTaskData lagProsessTaskData(TilkjentYtelseMelding melding){
        Henvisning henvisning = Henvisning.fraEksternBehandlingId(melding.getBehandlingId());
        ProsessTaskData td = new ProsessTaskData(HåndterHendelseTask.TASKTYPE);
        td.setAktørId(melding.getAktørId().getId());
        td.setProperty(TaskProperties.EKSTERN_BEHANDLING_UUID, melding.getBehandlingUuid().toString());
        td.setProperty(TaskProperties.EKSTERN_BEHANDLING_ID, henvisning.getVerdi()); //TODO k9-tilbake fjern når transisjon til henvisning er ferdig
        td.setProperty(TaskProperties.HENVISNING, henvisning.getVerdi());
        td.setProperty(TaskProperties.SAKSNUMMER, melding.getSaksnummer().getVerdi());
        td.setProperty(TaskProperties.FAGSAK_YTELSE_TYPE, melding.getFagsakYtelseType());
        return td;
    }

}
