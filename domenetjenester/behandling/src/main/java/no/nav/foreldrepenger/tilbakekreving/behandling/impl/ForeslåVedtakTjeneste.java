package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.TilbakekrevingBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.BrevdataRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevOppsummering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.brev.VedtaksbrevPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste.HistorikkTjenesteAdapter;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ForeslåVedtakTjeneste {

    private TilbakekrevingBeregningTjeneste beregningTjeneste;
    private HistorikkTjenesteAdapter historikkTjenesteAdapter;
    private BrevdataRepository brevdataRepository;

    ForeslåVedtakTjeneste() {
        // for CDI
    }

    @Inject
    public ForeslåVedtakTjeneste(TilbakekrevingBeregningTjeneste beregningTjeneste, HistorikkTjenesteAdapter historikkTjenesteAdapter, BrevdataRepository brevdataRepository) {
        this.beregningTjeneste = beregningTjeneste;
        this.historikkTjenesteAdapter = historikkTjenesteAdapter;
        this.brevdataRepository = brevdataRepository;
    }

    public void lagHistorikkInnslagForForeslåVedtak(Long behandlingId) {
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.FORSLAG_VEDTAK);
        historikkinnslag.setBehandlingId(behandlingId);
        historikkinnslag.setAktør(HistorikkAktør.SAKSBEHANDLER);

        HistorikkInnslagTekstBuilder tekstBuilder = historikkTjenesteAdapter.tekstBuilder();

        BeregningResultat beregningResultat = beregningTjeneste.beregn(behandlingId);
        tekstBuilder.medSkjermlenke(SkjermlenkeType.VEDTAK)
            .medResultat(beregningResultat.getVedtakResultatType())
            .medHendelse(HistorikkinnslagType.FORSLAG_VEDTAK)
            .build(historikkinnslag);

        historikkTjenesteAdapter.lagInnslag(historikkinnslag);
    }

    public void lagreFriteksterFraSaksbehandler(
        Long behandlingId,
        Optional<VedtaksbrevOppsummering> vedtaksbrevOppsummeringOpt,
        Optional<List<VedtaksbrevPeriode>> vedtaksbrevPerioderOpt) {

        brevdataRepository.slettOppsummering(behandlingId);
        brevdataRepository.slettPerioderMedFritekster(behandlingId);
        vedtaksbrevPerioderOpt.ifPresent(vedtaksbrevPerioder -> brevdataRepository.lagreVedtakPerioderOgTekster(vedtaksbrevPerioder));
        vedtaksbrevOppsummeringOpt.ifPresent(vedtaksbrevOppsummering -> brevdataRepository.lagreVedtaksbrevOppsummering(vedtaksbrevOppsummering));
    }
}
