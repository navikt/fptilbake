package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepositoryTeamAware;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag2;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;

@ApplicationScoped
public class ForeslåVedtakTjeneste {

    private BeregningsresultatTjeneste beregningsresultatTjeneste;
    private HistorikkRepositoryTeamAware historikkRepository;

    ForeslåVedtakTjeneste() {
        // for CDI
    }

    @Inject
    public ForeslåVedtakTjeneste(BeregningsresultatTjeneste beregningsresultatTjeneste,
                                 HistorikkRepositoryTeamAware historikkRepository) {
        this.beregningsresultatTjeneste = beregningsresultatTjeneste;
        this.historikkRepository = historikkRepository;
    }

    public void lagHistorikkInnslagForForeslåVedtak(Behandling behandling) {
        var beregningResultat = beregningsresultatTjeneste.finnEllerBeregn(behandling.getId());
        var historikkinnslag = lagHistorikkinnslag(behandling, beregningResultat);
        var historikk2innslag = lagHistorikk2innslag(behandling, beregningResultat);
        historikkRepository.lagre(historikkinnslag, historikk2innslag);
    }

    private static Historikkinnslag2 lagHistorikk2innslag(Behandling behandling, BeregningResultat beregningResultat) {
        return new Historikkinnslag2.Builder()
            .medAktør(HistorikkAktør.SAKSBEHANDLER)
            .medFagsakId(behandling.getFagsakId())
            .medBehandlingId(behandling.getId())
            .medTittel(SkjermlenkeType.VEDTAK)
            .addLinje(String.format("Vedtak foreslått og sendt til beslutter: %s", beregningResultat.getVedtakResultatType().getNavn()))
            .build();
    }

    private static Historikkinnslag lagHistorikkinnslag(Behandling behandling, BeregningResultat beregningResultat) {
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.FORSLAG_VEDTAK);
        historikkinnslag.setBehandlingId(behandling.getId());
        historikkinnslag.setAktør(HistorikkAktør.SAKSBEHANDLER);

        new HistorikkInnslagTekstBuilder()
            .medSkjermlenke(SkjermlenkeType.VEDTAK)
            .medResultat(beregningResultat.getVedtakResultatType())
            .medHendelse(HistorikkinnslagType.FORSLAG_VEDTAK)
            .build(historikkinnslag);
        return historikkinnslag;
    }

}
