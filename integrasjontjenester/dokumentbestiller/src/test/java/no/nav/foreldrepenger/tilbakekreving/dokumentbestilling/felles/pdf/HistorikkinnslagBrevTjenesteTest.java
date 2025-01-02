package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.pdf;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.DetaljertBrevType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepositoryTeamAware;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag2Repository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagDokumentLink;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.testutilities.kodeverk.ScenarioSimple;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.fritekstbrev.JournalpostIdOgDokumentId;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class HistorikkinnslagBrevTjenesteTest {

    private static final JournalpostId JOURNALPOST_ID = new JournalpostId("389426448");
    private static final String DOKUMENT_ID = "417743491";


    // TODO
    @Test
    void skal_opprette_historikkinnslag_for_utsendt_brev() {
        var historikkRepository = mock(HistorikkRepository.class);
        var historikkinnslag2Repository = mock(Historikkinnslag2Repository.class);
        var historikkinnslagRepo = new HistorikkRepositoryTeamAware(historikkRepository, historikkinnslag2Repository);
        var simpleScenario = ScenarioSimple.simple();
        var behandlingid = simpleScenario.getBehandling().getId();
        var behandlingRepository = simpleScenario.mockBehandlingRepository();
        var historikkinnslagBrevTjeneste = new HistorikkinnslagBrevTjeneste(historikkinnslagRepo, behandlingRepository);

        historikkinnslagBrevTjeneste.opprettHistorikkinnslagBrevSendt(behandlingid, new JournalpostIdOgDokumentId(JOURNALPOST_ID, DOKUMENT_ID), DetaljertBrevType.VEDTAK, BrevMottaker.BRUKER, "Vedtaksbrev");

//        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
//        verify(historikkinnslagBrevTjeneste, times(1)).(captor.capture());
//        var prosessTasker = captor.getAllValues();
//
//        List<Historikkinnslag> historikkinnslagene = historikkRepository.hentHistorikk(behandling.getId());
//        assertThat(historikkinnslagene).isNotEmpty();
//
//        Historikkinnslag historikkinnslag = historikkinnslagene.get(0);
//        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.VEDTAKSLØSNINGEN);
//        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.BREV_SENT);
//        assertThat(historikkinnslag.getDokumentLinker()).isNotEmpty();
//        HistorikkinnslagDokumentLink historikkinnslagDokumentLink = historikkinnslag.getDokumentLinker().get(0);
//        assertThat(historikkinnslagDokumentLink.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
    }
}
