package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.iverksetting.OppdragIverksettingStatusRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumerFeil;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiKvitteringTolk;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingsvedtakTjeneste;
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakRequest;
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakResponse;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsvedtakDto;
import no.nav.tilbakekreving.typer.v1.MmelDto;
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
    private ØkonomiConsumer økonomiConsumer;
    private boolean lansertLagringBeregningsresultat;

    SendVedtakTilOppdragsystemetTask() {
        // CDI krav
    }

    @Inject
    public SendVedtakTilOppdragsystemetTask(EntityManager entityManager,
                                            OppdragIverksettingStatusRepository oppdragIverksettingStatusRepository,
                                            BeregningsresultatTjeneste beregningsresultatTjeneste,
                                            TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste,
                                            ØkonomiConsumer økonomiConsumer,
                                            @KonfigVerdi(value = "toggle.enable.lagre.beregningsresultat", defaultVerdi = "false") boolean lansertLagringBeregningsresultat) {
        this.entityManager = entityManager;
        this.oppdragIverksettingStatusRepository = oppdragIverksettingStatusRepository;
        this.beregningsresultatTjeneste = beregningsresultatTjeneste;
        this.tilbakekrevingsvedtakTjeneste = tilbakekrevingsvedtakTjeneste;
        this.økonomiConsumer = økonomiConsumer;
        this.lansertLagringBeregningsresultat = lansertLagringBeregningsresultat;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        if (!lansertLagringBeregningsresultat) {
            throw new IllegalArgumentException("Toggle ble skrudd av etter at task ble opprettet. Tasken må fjernes og en " + SendØkonomiTibakekerevingsVedtakTask.class + "-task opprettes idstedet");
        }
        long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();

        beregningsresultatTjeneste.beregnOgLagre(behandlingId); //midlertidig her for å raskere kunne fase ut håndtering av lagret XML

        TilbakekrevingsvedtakDto tilbakekrevingsvedtak = tilbakekrevingsvedtakTjeneste.lagTilbakekrevingsvedtak(behandlingId);
        oppdragIverksettingStatusRepository.registrerStarterIverksetting(behandlingId, tilbakekrevingsvedtak.getVedtakId().toString());

        TilbakekrevingsvedtakRequest request = lagRequest(tilbakekrevingsvedtak);
        lagSavepointOgIverksett(behandlingId, request);
    }

    private void lagSavepointOgIverksett(long behandlingId, TilbakekrevingsvedtakRequest tilbakekrevingsvedtak) {
        RunWithSavepoint runWithSavepoint = new RunWithSavepoint(entityManager);
        runWithSavepoint.doWork(() -> {
            //setter før kall til OS, slik at kvittering-status blir lagret selv om kallet feiler
            TilbakekrevingsvedtakResponse respons = økonomiConsumer.iverksettTilbakekrevingsvedtak(behandlingId, tilbakekrevingsvedtak);
            MmelDto kvittering = respons.getMmel();
            boolean kvitteringOk = ØkonomiKvitteringTolk.erKvitteringOK(kvittering);
            oppdragIverksettingStatusRepository.registrerKvittering(behandlingId, kvitteringOk);
            if (kvitteringOk) {
                logger.info("Tilbakekrevingsvedtak sendt OK til oppdragsystemet. BehandlingId={}", behandlingId);
            } else {
                RunWithSavepoint rwsp = new RunWithSavepoint(entityManager);
                rwsp.doWork(() -> {
                    // Kaster feil for å feile prosesstasken, samt trigge logging
                    // Setter savepoint før feilen kastes, slik at kvittering-status blir lagret, selv om det blir exception
                    String detaljer = kvittering != null ? ØkonomiConsumerFeil.formaterKvittering(kvittering) : " Fikk ikke kvittering fra OS";
                    throw new IntegrasjonException("FPT-609912", String.format("Fikk feil fra OS ved iverksetting av behandlingId=%s.%s", behandlingId, detaljer));
                });
            }
            return null;
        });
    }

    private TilbakekrevingsvedtakRequest lagRequest(TilbakekrevingsvedtakDto tilbakekrevingsvedtak) {
        TilbakekrevingsvedtakRequest request = new TilbakekrevingsvedtakRequest();
        request.setTilbakekrevingsvedtak(tilbakekrevingsvedtak);
        return request;
    }
}
