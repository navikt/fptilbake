package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;

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


class DokumentBehandlingHistorikkTjenesteTest extends FellesTestOppsett {

    private BehandlingHistorikkTjeneste behandlingHistorikkTjeneste;

    @BeforeEach
    public void setup() {
        behandlingHistorikkTjeneste = new BehandlingHistorikkTjeneste(historikkinnslagRepository, historikkTjeneste);
    }


    @Test
    void opprettHistorikkinnslagForOpprettetTilbakekreving() {
        behandlingHistorikkTjeneste.opprettHistorikkinnslagForOpprettetBehandling(behandling);

        var historikkinnslagene = historikkinnslagRepository.hent(behandling.getId());
        assertThat(historikkinnslagene).isNotEmpty();

        var historikkinnslag = historikkinnslagene.get(0);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.VEDTAKSLØSNINGEN);
        assertThat(historikkinnslag.getTittel()).isEqualTo("Tilbakekreving opprettet");
        assertThat(historikkinnslag.getDokumentLinker()).isEmpty();
    }

    @Test
    void opprettHistorikkinnslagForOpprettetTilbakekreving_med_manuelt() {
        Fagsak fagsak = fagsakTjeneste.opprettFagsak(saksnummer, aktørId, FagsakYtelseType.FORELDREPENGER, Språkkode.DEFAULT);
        Behandling behandling = Behandling.nyBehandlingFor(fagsak, BehandlingType.TILBAKEKREVING).medManueltOpprettet(true).build();
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);

        behandlingHistorikkTjeneste.opprettHistorikkinnslagForOpprettetBehandling(behandling);

        var historikkinnslagene = historikkinnslagRepository.hent(behandling.getId());
        assertThat(historikkinnslagene).isNotEmpty();

        var historikkinnslag = historikkinnslagene.get(0);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.SAKSBEHANDLER);
        assertThat(historikkinnslag.getTittel()).isEqualTo("Tilbakekreving opprettet");
        assertThat(historikkinnslag.getDokumentLinker()).isEmpty();
    }
}
