package no.nav.foreldrepenger.tilbakekreving.hendelser.felles.task;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.behandling.task.HendelseTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.hendelser.felles.tjeneste.HendelseHåndtererTjeneste;
import no.nav.foreldrepenger.tilbakekreving.hendelser.tilkjentytelse.TilkjentYtelseMelding;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class HåndterHendelseTaskTest {

    private static final AktørId AKTØR_ID = new AktørId("1234567898765");
    private static final Long EKSTERN_BEHANDLING_ID = 123L;
    private static final UUID EKSTERN_BEHANDLING_UUID = UUID.randomUUID();

    private final HendelseHåndtererTjeneste hendelseHåndterer = mock(HendelseHåndtererTjeneste.class);
    private final HåndterHendelseTask håndterHendelseTask = new HåndterHendelseTask(hendelseHåndterer);


    @Test
    public void test_skal_kalle_hendelseHåndterer() {
        //
        ProsessTaskData prosessTaskData = lagProsessTaskData(opprettTilkjentYtelseMelding());

        // act
        håndterHendelseTask.doTask(prosessTaskData);

        // verify
        verify(hendelseHåndterer, atLeastOnce()).håndterHendelse(any(HendelseTaskDataWrapper.class), any(Henvisning.class), any(String.class));
    }

    private ProsessTaskData lagProsessTaskData(TilkjentYtelseMelding melding) {
        Henvisning henvisning = Henvisning.fraEksternBehandlingId(melding.getBehandlingId());
        ProsessTaskData td = ProsessTaskData.forProsessTask(HåndterHendelseTask.class);
        td.setAktørId(melding.getAktørId().getId());
        td.setProperty(TaskProperties.EKSTERN_BEHANDLING_UUID, melding.getBehandlingUuid().toString());
        td.setProperty(TaskProperties.EKSTERN_BEHANDLING_ID, henvisning.getVerdi()); //TODO k9-tilbake fjern når transisjon til henvisning er ferdig
        td.setProperty(TaskProperties.HENVISNING, henvisning.getVerdi());
        td.setProperty(TaskProperties.SAKSNUMMER, melding.getSaksnummer().getVerdi());
        td.setProperty(TaskProperties.FAGSAK_YTELSE_TYPE, melding.getFagsakYtelseType());
        return td;
    }

    public static TilkjentYtelseMelding opprettTilkjentYtelseMelding() {
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
