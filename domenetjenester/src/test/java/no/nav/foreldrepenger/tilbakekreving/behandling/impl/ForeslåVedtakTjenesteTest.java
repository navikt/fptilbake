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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;

class ForeslåVedtakTjenesteTest extends FellesTestOppsett {

    private BeregningsresultatTjeneste beregningsresultatTjeneste;
    private ForeslåVedtakTjeneste foreslåVedtakTjeneste;

    @BeforeEach
    void setUp() {
        beregningsresultatTjeneste = mock(BeregningsresultatTjeneste.class);
        foreslåVedtakTjeneste = new ForeslåVedtakTjeneste(beregningsresultatTjeneste, historikkinnslagRepository);
    }

    @Test
    void lagHistorikkInnslagForForeslåVedtak() {
        BeregningResultat beregningResultat = new BeregningResultat(VedtakResultatType.FULL_TILBAKEBETALING, List.of());
        when(beregningsresultatTjeneste.finnEllerBeregn(internBehandlingId)).thenReturn(beregningResultat);

        foreslåVedtakTjeneste.lagHistorikkInnslagForForeslåVedtak(behandling);

        var historikkInnslager = historikkinnslagRepository.hent(saksnummer);
        assertThat(historikkInnslager).isNotEmpty();
        assertThat(historikkInnslager).hasSize(2);
        assertThat(historikkInnslager.get(0).getBehandlingId()).isEqualTo(internBehandlingId);
        assertThat(historikkInnslager.get(0).getAktør()).isEqualTo(HistorikkAktør.VEDTAKSLØSNINGEN);
        assertThat(historikkInnslager.get(0).getTittel()).isEqualTo("Tilbakekreving opprettet");

        assertThat(historikkInnslager.get(1).getBehandlingId()).isEqualTo(internBehandlingId);
        assertThat(historikkInnslager.get(1).getAktør()).isEqualByComparingTo(HistorikkAktør.SAKSBEHANDLER);
        assertThat(historikkInnslager.get(1).getSkjermlenke()).isEqualTo(SkjermlenkeType.VEDTAK);
        assertThat(historikkInnslager.get(1).getLinjer().getFirst().getTekst()).contains("Vedtak foreslått og sendt til beslutter", beregningResultat.getVedtakResultatType().getNavn());
    }

}
