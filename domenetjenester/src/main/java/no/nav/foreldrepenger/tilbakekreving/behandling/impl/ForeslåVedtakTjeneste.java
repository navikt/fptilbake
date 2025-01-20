package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;

@ApplicationScoped
public class ForeslåVedtakTjeneste {

    private BeregningsresultatTjeneste beregningsresultatTjeneste;
    private HistorikkinnslagRepository historikkRepository;

    ForeslåVedtakTjeneste() {
        // for CDI
    }

    @Inject
    public ForeslåVedtakTjeneste(BeregningsresultatTjeneste beregningsresultatTjeneste, HistorikkinnslagRepository historikkRepository) {
        this.beregningsresultatTjeneste = beregningsresultatTjeneste;
        this.historikkRepository = historikkRepository;
    }

    public void lagHistorikkInnslagForForeslåVedtak(Behandling behandling) {
        var beregningResultat = beregningsresultatTjeneste.finnEllerBeregn(behandling.getId());
        var historikkinnslag = new Historikkinnslag.Builder()
            .medAktør(HistorikkAktør.SAKSBEHANDLER)
            .medFagsakId(behandling.getFagsakId())
            .medBehandlingId(behandling.getId())
            .medTittel(SkjermlenkeType.VEDTAK)
            .addLinje(String.format("Vedtak foreslått og sendt til beslutter: %s", beregningResultat.getVedtakResultatType().getNavn()))
            .build();
        historikkRepository.lagre(historikkinnslag);
    }
}
