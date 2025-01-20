package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;

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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagOld;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagOldDokumentLink;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;

class HistorikkinnslagOldTjenesteTest extends FellesTestOppsett {

    private static final JournalpostId JOURNALPOST_ID = new JournalpostId("389426448");
    private static final String DOKUMENT_ID = "417743491";

    private HistorikkinnslagTjeneste historikkinnslagTjeneste;

    @BeforeEach
    void setUp() {
        historikkinnslagTjeneste = new HistorikkinnslagTjeneste(historikkRepository);
    }

    @Test
    void skal_opprette_historikkinnslag_for_utsendt_brev() {
        historikkinnslagTjeneste.opprettHistorikkinnslagForBrevsending(behandling, JOURNALPOST_ID, DOKUMENT_ID, "Vedtaksbrev");

        List<HistorikkinnslagOld> historikkinnslagene = historikkRepository.hentHistorikk(behandling.getId());
        assertThat(historikkinnslagene).isNotEmpty();

        HistorikkinnslagOld historikkinnslag = historikkinnslagene.get(0);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.VEDTAKSLØSNINGEN);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.BREV_SENT);
        assertThat(historikkinnslag.getDokumentLinker()).isNotEmpty();
        HistorikkinnslagOldDokumentLink historikkinnslagDokumentLink = historikkinnslag.getDokumentLinker().get(0);
        assertThat(historikkinnslagDokumentLink.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
    }

    @Test
    void opprettHistorikkinnslagForOpprettetTilbakekreving() {
        historikkinnslagTjeneste.opprettHistorikkinnslagForOpprettetBehandling(behandling);

        List<HistorikkinnslagOld> historikkinnslagene = historikkRepository.hentHistorikk(behandling.getId());
        assertThat(historikkinnslagene).isNotEmpty();

        HistorikkinnslagOld historikkinnslag = historikkinnslagene.get(0);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.VEDTAKSLØSNINGEN);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.TBK_OPPR);
        assertThat(historikkinnslag.getDokumentLinker()).isEmpty();
    }

    @Test
    void opprettHistorikkinnslagForOpprettetTilbakekreving_med_manuelt() {
        Fagsak fagsak = fagsakTjeneste.opprettFagsak(saksnummer, aktørId, FagsakYtelseType.FORELDREPENGER, Språkkode.DEFAULT);
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).medManueltOpprettet(true).build();
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);

        historikkinnslagTjeneste.opprettHistorikkinnslagForOpprettetBehandling(behandling);

        List<HistorikkinnslagOld> historikkinnslagene = historikkRepository.hentHistorikk(behandling.getId());
        assertThat(historikkinnslagene).isNotEmpty();

        HistorikkinnslagOld historikkinnslag = historikkinnslagene.get(0);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.SAKSBEHANDLER);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.TBK_OPPR);
        assertThat(historikkinnslag.getDokumentLinker()).isEmpty();
    }

}
