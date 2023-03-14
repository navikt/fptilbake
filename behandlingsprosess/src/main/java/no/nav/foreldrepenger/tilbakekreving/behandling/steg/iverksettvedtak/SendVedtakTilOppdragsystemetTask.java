package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.UkjentKvitteringFraOSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.iverksett.TilbakekrevingVedtakDTO;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.ØkonomiProxyKlient;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.iverksetting.OppdragIverksettingStatusRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingsvedtakTjeneste;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.jpa.savepoint.RunWithSavepoint;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask("iverksetteVedtak.sendVedtakTilOppdragsystemet")
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class SendVedtakTilOppdragsystemetTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(SendVedtakTilOppdragsystemetTask.class);

    private EntityManager entityManager;
    private OppdragIverksettingStatusRepository oppdragIverksettingStatusRepository;
    private TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste;
    private BeregningsresultatTjeneste beregningsresultatTjeneste;
    private ØkonomiProxyKlient økonomiProxyKlient;
    SendVedtakTilOppdragsystemetTask() {
        // CDI krav
    }

    @Inject
    public SendVedtakTilOppdragsystemetTask(EntityManager entityManager,
                                            OppdragIverksettingStatusRepository oppdragIverksettingStatusRepository,
                                            BeregningsresultatTjeneste beregningsresultatTjeneste,
                                            TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste,
                                            ØkonomiProxyKlient økonomiProxyKlient) {
        this.entityManager = entityManager;
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
        oppdragIverksettingStatusRepository.registrerStarterIverksetting(behandlingId, tilbakekrevingsvedtak.vedtakId().toString());
        lagSavepointOgIverksettViaFpWsProxy(behandlingId, tilbakekrevingsvedtak);
    }

    private void lagSavepointOgIverksettViaFpWsProxy(long behandlingId, TilbakekrevingVedtakDTO tilbakekrevingsvedtak) {
        var runWithSavepoint = new RunWithSavepoint(entityManager);
        runWithSavepoint.doWork(() -> {
            //setter før kall til OS, slik at kvittering-status blir lagret selv om kallet feiler
            try {
                økonomiProxyKlient.iverksettTilbakekrevingsvedtak(tilbakekrevingsvedtak);
                logger.info("Tilbakekrevingsvedtak sendt OK til oppdragsystemet via fpwsproxy. BehandlingId={}", behandlingId);
                oppdragIverksettingStatusRepository.registrerKvittering(behandlingId, true);
            } catch (UkjentKvitteringFraOSException e) {
                oppdragIverksettingStatusRepository.registrerKvittering(behandlingId, false);
                var rwsp = new RunWithSavepoint(entityManager);
                rwsp.doWork(() -> {
                    // Kaster feil for å feile prosesstasken, samt trigge logging.
                    // Setter savepoint før feilen kastes, slik at kvittering-status blir lagret, selv om det blir exception
                    throw new IntegrasjonException("FPT-609912", String.format("Fikk feil fra OS ved iverksetting av behandlingId=%s. Sjekk loggen til fpwsproxy for mer info.", behandlingId));
                });
            }
            return null;
        });
    }
}
