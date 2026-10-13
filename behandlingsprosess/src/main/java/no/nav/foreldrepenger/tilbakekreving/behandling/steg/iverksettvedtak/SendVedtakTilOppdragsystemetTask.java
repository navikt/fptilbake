package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import java.util.function.BooleanSupplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.ØkonomiProxyKlient;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.iverksetting.OppdragIverksettingStatusRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingsvedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.sokos.SokosTilbakekrevingKlient;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(value = "iverksetteVedtak.sendVedtakTilOppdragsystemet", prioritet = 2)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class SendVedtakTilOppdragsystemetTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SendVedtakTilOppdragsystemetTask.class);
    private static final Fagsystem HVILKEN_TILBAKE = ApplicationName.hvilkenTilbake();
    private static final Environment ENV = Environment.current();

    private OppdragIverksettingStatusRepository oppdragIverksettingStatusRepository;
    private TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste;
    private BeregningsresultatTjeneste beregningsresultatTjeneste;
    private ØkonomiProxyKlient økonomiProxyKlient;
    private SokosTilbakekrevingKlient sokosTilbakekrevingKlient;
    private BooleanSupplier erDev;


    SendVedtakTilOppdragsystemetTask() {
        // CDI krav
    }

    @Inject
    public SendVedtakTilOppdragsystemetTask(OppdragIverksettingStatusRepository oppdragIverksettingStatusRepository,
                                            BeregningsresultatTjeneste beregningsresultatTjeneste,
                                            TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste,
                                            ØkonomiProxyKlient økonomiProxyKlient,
                                            SokosTilbakekrevingKlient sokosTilbakekrevingKlient) {
        this(oppdragIverksettingStatusRepository, beregningsresultatTjeneste, tilbakekrevingsvedtakTjeneste,
            økonomiProxyKlient, sokosTilbakekrevingKlient, ENV::isDev);
    }

    SendVedtakTilOppdragsystemetTask(OppdragIverksettingStatusRepository oppdragIverksettingStatusRepository,
                                     BeregningsresultatTjeneste beregningsresultatTjeneste,
                                     TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste,
                                     ØkonomiProxyKlient økonomiProxyKlient,
                                     SokosTilbakekrevingKlient sokosTilbakekrevingKlient,
                                     BooleanSupplier erDev) {
        this.oppdragIverksettingStatusRepository = oppdragIverksettingStatusRepository;
        this.beregningsresultatTjeneste = beregningsresultatTjeneste;
        this.tilbakekrevingsvedtakTjeneste = tilbakekrevingsvedtakTjeneste;
        this.økonomiProxyKlient = økonomiProxyKlient;
        this.sokosTilbakekrevingKlient = sokosTilbakekrevingKlient;
        this.erDev = erDev;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        beregningsresultatTjeneste.beregnOgLagre(behandlingId); //midlertidig her for å raskere kunne fase ut håndtering av lagret XML
        if (erDev.getAsBoolean() && Fagsystem.FPTILBAKE.equals(HVILKEN_TILBAKE)) {
            var request = tilbakekrevingsvedtakTjeneste.lagSokosTilbakekrevingsvedtak(behandlingId);
            sokosTilbakekrevingKlient.iverksettTilbakekrevingsvedtak(request);
            oppdragIverksettingStatusRepository.registrerKvittertVedtak(behandlingId, request.vedtakId().toString());
            LOG.info("Tilbakekrevingsvedtak sendt OK til oppdragsystemet via Sokos. BehandlingId={}", behandlingId);
        } else {
            var tilbakekrevingsvedtak = tilbakekrevingsvedtakTjeneste.lagTilbakekrevingsvedtak(behandlingId);
            økonomiProxyKlient.iverksettTilbakekrevingsvedtak(tilbakekrevingsvedtak);
            oppdragIverksettingStatusRepository.registrerKvittertVedtak(behandlingId, tilbakekrevingsvedtak.vedtakId().toString());
            LOG.info("Tilbakekrevingsvedtak sendt OK til oppdragsystemet via fpwsproxy. BehandlingId={}", behandlingId);
        }
    }

}
