package no.nav.foreldrepenger.tilbakekreving.hendelser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.abakus.vedtak.ytelse.Aktør;
import no.nav.abakus.vedtak.ytelse.Kildesystem;
import no.nav.abakus.vedtak.ytelse.Ytelser;
import no.nav.abakus.vedtak.ytelse.v1.YtelseV1;
import no.nav.foreldrepenger.tilbakekreving.behandling.task.HendelseTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

class HåndterHendelseTaskTest {

    private static final AktørId AKTØR_ID = new AktørId("1234567898765");
    private static final UUID EKSTERN_BEHANDLING_UUID = UUID.randomUUID();

    private final HendelseHåndtererTjeneste hendelseHåndterer = mock(HendelseHåndtererTjeneste.class);
    private final HåndterVedtakFattetTask håndterHendelseTask = new HåndterVedtakFattetTask(hendelseHåndterer);


    @Test
    void test_skal_kalle_hendelseHåndterer() {
        //
        ProsessTaskData prosessTaskData = lagProsessTaskData(opprettTilkjentYtelseMelding());
        when(hendelseHåndterer.hentHenvisning(any(UUID.class))).thenReturn(new Henvisning("123"));

        // act
        håndterHendelseTask.doTask(prosessTaskData);

        // verify
        verify(hendelseHåndterer, atLeastOnce()).håndterHendelse(any(HendelseTaskDataWrapper.class), any(Henvisning.class), any(String.class));
    }

    private ProsessTaskData lagProsessTaskData(YtelseV1 melding) {
        ProsessTaskData td = ProsessTaskData.forProsessTask(HåndterVedtakFattetTask.class);
        td.setAktørId(melding.getAktør().getVerdi());
        td.setProperty(TaskProperties.EKSTERN_BEHANDLING_UUID, melding.getVedtakReferanse());
        td.setProperty(TaskProperties.SAKSNUMMER, melding.getSaksnummer());
        td.setProperty(TaskProperties.FAGSAK_YTELSE_TYPE, FagsakYtelseType.FORELDREPENGER.getKode());
        return td;
    }

    static YtelseV1 opprettTilkjentYtelseMelding() {
        var melding = new YtelseV1();
        var aktør = new Aktør();
        aktør.setVerdi(AKTØR_ID.getId());
        melding.setAktør(aktør);
        melding.setVedtakReferanse(EKSTERN_BEHANDLING_UUID.toString());
        melding.setVedtattTidspunkt(LocalDateTime.now().minusSeconds(2));
        melding.setKildesystem(Kildesystem.FPSAK);
        melding.setYtelse(Ytelser.FORELDREPENGER);
        melding.setSaksnummer("123456789");
        return melding;
    }

}
