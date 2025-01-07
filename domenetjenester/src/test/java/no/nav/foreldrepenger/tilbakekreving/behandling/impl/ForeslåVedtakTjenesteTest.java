package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;

class ForeslåVedtakTjenesteTest extends FellesTestOppsett {

    private BeregningsresultatTjeneste beregningsresultatTjeneste;
    private ForeslåVedtakTjeneste foreslåVedtakTjeneste;

    @BeforeEach
    void setUp() {
        beregningsresultatTjeneste = mock(BeregningsresultatTjeneste.class);
        foreslåVedtakTjeneste = new ForeslåVedtakTjeneste(beregningsresultatTjeneste, historikkRepositoryTeamAware);
    }

    @Test
    void lagHistorikkInnslagForForeslåVedtak() {
        BeregningResultat beregningResultat = new BeregningResultat(VedtakResultatType.FULL_TILBAKEBETALING, List.of());
        when(beregningsresultatTjeneste.finnEllerBeregn(internBehandlingId)).thenReturn(beregningResultat);

        foreslåVedtakTjeneste.lagHistorikkInnslagForForeslåVedtak(behandling);

        List<Historikkinnslag> historikkInnslager = historikkRepository.hentHistorikkForSaksnummer(saksnummer);
        assertThat(historikkInnslager).isNotEmpty();
        assertThat(historikkInnslager).hasSize(2);
        assertThat(historikkInnslager.get(0).getBehandlingId()).isEqualTo(internBehandlingId);
        assertThat(historikkInnslager.get(0).getAktør()).isEqualByComparingTo(HistorikkAktør.VEDTAKSLØSNINGEN);
        assertThat(historikkInnslager.get(0).getType()).isEqualByComparingTo(HistorikkinnslagType.TBK_OPPR);

        assertThat(historikkInnslager.get(1).getBehandlingId()).isEqualTo(internBehandlingId);
        assertThat(historikkInnslager.get(1).getAktør()).isEqualByComparingTo(HistorikkAktør.SAKSBEHANDLER);
        assertThat(historikkInnslager.get(1).getType()).isEqualByComparingTo(HistorikkinnslagType.FORSLAG_VEDTAK);
        List<HistorikkinnslagDel> historikkinnslagDeler = historikkInnslager.get(1).getHistorikkinnslagDeler();
        assertThat(historikkinnslagDeler.size()).isEqualTo(1);
        HistorikkinnslagDel historikkinnslagDel = historikkinnslagDeler.get(0);
        assertThat(historikkinnslagDel.getSkjermlenke().get()).isEqualTo(SkjermlenkeType.VEDTAK.getKode());
        assertThat(historikkinnslagDel.getResultat().get()).isEqualTo(beregningResultat.getVedtakResultatType().getKode());
    }

}
