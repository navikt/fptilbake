package no.nav.foreldrepenger.tilbakekreving.avstemming;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat.BeregningsresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.iverksetting.OppdragIverksettingStatusRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.PersonIdent;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;

@ApplicationScoped
public class AvstemFraResultatOgIverksettingStatusTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(AvstemFraResultatOgIverksettingStatusTjeneste.class);

    private BehandlingRepository behandlingRepository;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private BeregningsresultatRepository beregningsresultatRepository;
    private OppdragIverksettingStatusRepository oppdragIverksettingStatusRepository;
    private PersoninfoAdapter aktørConsumer;

    private String avsender;

    AvstemFraResultatOgIverksettingStatusTjeneste() {
        //for CDI proxy
    }

    @Inject
    public AvstemFraResultatOgIverksettingStatusTjeneste(BeregningsresultatRepository beregningsresultatRepository,
                                                         OppdragIverksettingStatusRepository oppdragIverksettingStatusRepository,
                                                         BehandlingRepositoryProvider behandlingRepositoryProvider,
                                                         PersoninfoAdapter aktørConsumer) {
        this(ApplicationName.hvilkenTilbakeAppName(), beregningsresultatRepository, oppdragIverksettingStatusRepository, behandlingRepositoryProvider, aktørConsumer);
    }

    public AvstemFraResultatOgIverksettingStatusTjeneste(String applikasjon,
                                                         BeregningsresultatRepository beregningsresultatRepository,
                                                         OppdragIverksettingStatusRepository oppdragIverksettingStatusRepository,
                                                         BehandlingRepositoryProvider behandlingRepositoryProvider,
                                                         PersoninfoAdapter aktørConsumer) {
        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
        this.behandlingVedtakRepository = behandlingRepositoryProvider.getBehandlingVedtakRepository();
        this.beregningsresultatRepository = beregningsresultatRepository;
        this.oppdragIverksettingStatusRepository = oppdragIverksettingStatusRepository;
        this.aktørConsumer = aktørConsumer;
        this.avsender = applikasjon;
    }

    public Optional<String> oppsummer(LocalDate dato) {
        var avstemmingCsvFormatter = new AvstemmingCsvFormatter();

        leggTilOppsummering(dato, avstemmingCsvFormatter);

        logger.info("Sender {} vedtak til avstemming for {}", avstemmingCsvFormatter.getAntallRader(), dato);

        if (avstemmingCsvFormatter.getAntallRader() == 0) {
            return Optional.empty();
        }
        return Optional.of(avstemmingCsvFormatter.getData());
    }

    public void leggTilOppsummering(LocalDate dato, AvstemmingCsvFormatter avstemmingCsvFormatter) {
        var antallFeilet = 0;
        var antallFørstegangsvedtakUtenTilbakekreving = 0;
        var iverksettingStatuser = oppdragIverksettingStatusRepository.finnForDato(dato);
        for (var iverksettingStatus : iverksettingStatuser) {
            if (!iverksettingStatus.getKvitteringOk()) {
                antallFeilet++;
                continue;
            }
            var behandlingId = iverksettingStatus.getBehandlingId();
            var behandling = behandlingRepository.hentBehandling(behandlingId);
            var beregningsresultat = beregningsresultatRepository.hentHvisEksisterer(behandlingId).orElseThrow();
            var oppsummering = TilbakekrevingsvedtakOppsummering.oppsummer(iverksettingStatus, beregningsresultat);
            if (erFørstegangsvedtakUtenTilbakekreving(behandling, oppsummering)) {
                antallFørstegangsvedtakUtenTilbakekreving++;
                continue;
            }
            leggTilAvstemmingsdataForVedtaket(avstemmingCsvFormatter, behandling, oppsummering);

        }
        if (antallFeilet != 0) {
            logger.warn("{} vedtak har feilet i overføring til OS for {}", antallFeilet, dato);
        }
        if (antallFørstegangsvedtakUtenTilbakekreving != 0) {
            logger.info("{} førstegangsvedtak uten tilbakekreving sendes ikke til avstemming for {}", antallFørstegangsvedtakUtenTilbakekreving, dato);
        }
    }


    private boolean erFørstegangsvedtakUtenTilbakekreving(Behandling behandling, TilbakekrevingsvedtakOppsummering oppsummering) {
        return behandling.getType().equals(BehandlingType.TILBAKEKREVING) && oppsummering.harIngenTilbakekreving();
    }

    private void leggTilAvstemmingsdataForVedtaket(AvstemmingCsvFormatter avstemmingCsvFormatter, Behandling behandling, TilbakekrevingsvedtakOppsummering oppsummering) {
        var behandlingId = behandling.getId();
        var behandlingVedtak = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandlingId).orElseThrow();

        var fnr = aktørConsumer.hentFnrForAktør(behandling.getAktørId()).map(PersonIdent::getIdent)
            .orElseThrow(() -> new IllegalArgumentException("Avstemming feilet, fant ikke ident. Gjelder behandlingId=" + behandlingId));

        avstemmingCsvFormatter.leggTilRad(AvstemmingCsvFormatter.radBuilder()
            .medAvsender(avsender)
            .medVedtakId(oppsummering.getØkonomiVedtakId())
            .medFnr(fnr)
            .medVedtaksdato(behandlingVedtak.getVedtaksdato())
            .medFagsakYtelseType(behandling.getFagsak().getFagsakYtelseType())
            .medTilbakekrevesBruttoUtenRenter(oppsummering.getTilbakekrevesBruttoUtenRenter())
            .medTilbakekrevesNettoUtenRenter(oppsummering.getTilbakekrevesNettoUtenRenter())
            .medSkatt(oppsummering.getSkatt())
            .medRenter(oppsummering.getRenter())
            .medErOmgjøringTilIngenTilbakekreving(erOmgjøringTilIngenTilbakekreving(oppsummering, behandling))
        );
    }

    private boolean erOmgjøringTilIngenTilbakekreving(TilbakekrevingsvedtakOppsummering oppsummering, Behandling behandling) {
        return behandling.getType().equals(BehandlingType.REVURDERING_TILBAKEKREVING) && oppsummering.harIngenTilbakekreving();
    }


}
