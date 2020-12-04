package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagDokumentLink;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;

public class HistorikkinnslagTjenesteTest extends FellesTestOppsett {

    private static final JournalpostId JOURNALPOST_ID = new JournalpostId("389426448");
    private static final String DOKUMENT_ID = "417743491";

    private final PersoninfoAdapter personinfoAdapter = mock(PersoninfoAdapter.class);
    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    @BeforeEach
    void setUp() {
        historikkinnslagTjeneste = new HistorikkinnslagTjeneste(historikkRepository, personinfoAdapter);
    }

    @Test
    public void skal_opprette_historikkinnslag_for_utsendt_brev() {
        historikkinnslagTjeneste.opprettHistorikkinnslagForBrevsending(behandling, JOURNALPOST_ID, DOKUMENT_ID, "Vedtaksbrev");

        List<Historikkinnslag> historikkinnslagene = historikkRepository.hentHistorikk(behandling.getId());
        assertThat(historikkinnslagene).isNotEmpty();

        Historikkinnslag historikkinnslag = historikkinnslagene.get(0);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.VEDTAKSLØSNINGEN);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.BREV_SENT);
        assertThat(historikkinnslag.getDokumentLinker()).isNotEmpty();
        HistorikkinnslagDokumentLink historikkinnslagDokumentLink = historikkinnslag.getDokumentLinker().get(0);
        assertThat(historikkinnslagDokumentLink.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
    }

    @Test
    public void opprettHistorikkinnslagForOpprettetTilbakekreving() {
        historikkinnslagTjeneste.opprettHistorikkinnslagForOpprettetBehandling(behandling);

        List<Historikkinnslag> historikkinnslagene = historikkRepository.hentHistorikk(behandling.getId());
        assertThat(historikkinnslagene).isNotEmpty();

        Historikkinnslag historikkinnslag = historikkinnslagene.get(0);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.VEDTAKSLØSNINGEN);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.TBK_OPPR);
        assertThat(historikkinnslag.getDokumentLinker()).isEmpty();
    }

    @Test
    public void opprettHistorikkinnslagForOpprettetTilbakekreving_med_manuelt() {
        Fagsak fagsak = fagsakTjeneste.opprettFagsak(saksnummer,aktørId, FagsakYtelseType.FORELDREPENGER, Språkkode.DEFAULT);
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).medManueltOpprettet(true).build();
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling,behandlingLås);

        historikkinnslagTjeneste.opprettHistorikkinnslagForOpprettetBehandling(behandling);

        List<Historikkinnslag> historikkinnslagene = historikkRepository.hentHistorikk(behandling.getId());
        assertThat(historikkinnslagene).isNotEmpty();

        Historikkinnslag historikkinnslag = historikkinnslagene.get(0);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.SAKSBEHANDLER);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.TBK_OPPR);
        assertThat(historikkinnslag.getDokumentLinker()).isEmpty();
    }

}
