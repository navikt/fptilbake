package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevdataRepository;
import org.junit.Test;

import no.nav.foreldrepenger.tilbakekreving.FellesTestOppsett;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.TilbakekrevingBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;

public class ForeslåVedtakTjenesteTest extends FellesTestOppsett {

    private TilbakekrevingBeregningTjeneste beregningTjeneste = mock(TilbakekrevingBeregningTjeneste.class);
    private BrevdataRepository brevdataRepositoryMock = mock(BrevdataRepository.class);
    private ForeslåVedtakTjeneste foreslåVedtakTjeneste = new ForeslåVedtakTjeneste(beregningTjeneste, historikkTjenesteAdapter, brevdataRepositoryMock);

    @Test
    public void lagHistorikkInnslagForForeslåVedtak() {
        BeregningResultat beregningResultat = new BeregningResultat();
        beregningResultat.setVedtakResultatType(VedtakResultatType.FULL_TILBAKEBETALING);
        when(beregningTjeneste.beregn(internBehandlingId)).thenReturn(beregningResultat);

        foreslåVedtakTjeneste.lagHistorikkInnslagForForeslåVedtak(internBehandlingId);

        List<Historikkinnslag> historikkInnslager = historikkRepository.hentHistorikkForSaksnummer(saksnummer);
        assertThat(historikkInnslager).isNotEmpty();
        assertThat(historikkInnslager.size()).isEqualTo(1);
        Historikkinnslag historikkinnslag = historikkInnslager.get(0);
        assertThat(historikkinnslag.getBehandlingId()).isEqualTo(internBehandlingId);
        assertThat(historikkinnslag.getAktør()).isEqualByComparingTo(HistorikkAktør.SAKSBEHANDLER);
        assertThat(historikkinnslag.getType()).isEqualByComparingTo(HistorikkinnslagType.FORSLAG_VEDTAK);

        List<HistorikkinnslagDel> historikkinnslagDeler = historikkinnslag.getHistorikkinnslagDeler();
        assertThat(historikkinnslagDeler.size()).isEqualTo(1);
        HistorikkinnslagDel historikkinnslagDel = historikkinnslagDeler.get(0);
        assertThat(historikkinnslagDel.getSkjermlenke().get()).isEqualTo(SkjermlenkeType.VEDTAK.getKode());
        assertThat(historikkinnslagDel.getResultat().get())
                .isEqualTo(beregningResultat.getVedtakResultatType().getKode());
    }

}
