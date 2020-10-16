package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagDokumentLink;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.JournalpostId;
import no.nav.foreldrepenger.tilbakekreving.domene.person.TpsAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.person.impl.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.person.impl.TpsAdapterImpl;
import no.nav.foreldrepenger.tilbakekreving.domene.person.impl.TpsOversetter;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkinnslagTjeneste;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.person.PersonConsumer;

public class HistorikkinnslagTjenesteTest extends FellesTestOppsett {

    private static final JournalpostId JOURNALPOST_ID = new JournalpostId("389426448");
    private static final String DOKUMENT_ID = "417743491";
    private AktørConsumerMedCache mockAktørConsumer = mock(AktørConsumerMedCache.class);
    private PersonConsumer mockPersonConsumer = mock(PersonConsumer.class);
    private TpsOversetter mockTpsOversetter = mock(TpsOversetter.class);

    private TpsAdapter tpsAdapter = new TpsAdapterImpl(mockAktørConsumer, mockPersonConsumer, mockTpsOversetter);
    private PersoninfoAdapter personinfoAdapter = new PersoninfoAdapter(tpsAdapter);
    private HistorikkinnslagTjeneste historikkinnslagTjeneste = new HistorikkinnslagTjeneste(historikkRepository, personinfoAdapter);

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
        Fagsak fagsak = fagsakTjeneste.opprettFagsak(saksnummer,aktørId, FagsakYtelseType.FORELDREPENGER);
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
