package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.ØkonomiProxyKlient;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.iverksetting.OppdragIverksettingStatusRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingsvedtakTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(value = "iverksetteVedtak.sendVedtakTilOppdragsystemet", prioritet = 2)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class SendVedtakTilOppdragsystemetTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SendVedtakTilOppdragsystemetTask.class);

    private OppdragIverksettingStatusRepository oppdragIverksettingStatusRepository;
    private TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste;
    private BeregningsresultatTjeneste beregningsresultatTjeneste;
    private ØkonomiProxyKlient økonomiProxyKlient;


    SendVedtakTilOppdragsystemetTask() {
        // CDI krav
    }

    @Inject
    public SendVedtakTilOppdragsystemetTask(OppdragIverksettingStatusRepository oppdragIverksettingStatusRepository,
                                            BeregningsresultatTjeneste beregningsresultatTjeneste,
                                            TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste,
                                            ØkonomiProxyKlient økonomiProxyKlient) {
        this.oppdragIverksettingStatusRepository = oppdragIverksettingStatusRepository;
        this.beregningsresultatTjeneste = beregningsresultatTjeneste;
        this.tilbakekrevingsvedtakTjeneste = tilbakekrevingsvedtakTjeneste;
        this.økonomiProxyKlient = økonomiProxyKlient;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        beregningsresultatTjeneste.beregnOgLagre(behandlingId); //midlertidig her for å raskere kunne fase ut håndtering av lagret XML
        var tilbakekrevingsvedtak = tilbakekrevingsvedtakTjeneste.lagTilbakekrevingsvedtak(behandlingId);
        økonomiProxyKlient.iverksettTilbakekrevingsvedtak(tilbakekrevingsvedtak);
        LOG.info("Tilbakekrevingsvedtak sendt OK til oppdragsystemet via fpwsproxy. BehandlingId={}", behandlingId);
        oppdragIverksettingStatusRepository.registrerKvittertVedtak(behandlingId, tilbakekrevingsvedtak.vedtakId().toString());
    }

}
