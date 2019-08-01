package no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.task;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingsvedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.sporing.VedtakXmlRepository;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsvedtakDto;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.jpa.savepoint.RunWithSavepoint;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(SendØkonomiTibakekerevingsVedtakTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class SendØkonomiTibakekerevingsVedtakTask implements ProsessTaskHandler {
    public static final String TASKTYPE = "iverksetteVedtak.sendØkonomiTilbakekrevingsvedtak";

    private static final Logger log = LoggerFactory.getLogger(SendØkonomiTibakekerevingsVedtakTask.class);

    private EntityManager entityManager;
    private TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste;
    private ØkonomiConsumer økonomiConsumer;
    private VedtakXmlRepository vedtakXmlRepository;

    SendØkonomiTibakekerevingsVedtakTask() {
        // CDI krav
    }

    @Inject
    public SendØkonomiTibakekerevingsVedtakTask(TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste, ØkonomiConsumer økonomiConsumer, VedtakXmlRepository vedtakXmlRepository) {
        this.tilbakekrevingsvedtakTjeneste = tilbakekrevingsvedtakTjeneste;
        this.økonomiConsumer = økonomiConsumer;
        this.vedtakXmlRepository = vedtakXmlRepository;
        this.entityManager = vedtakXmlRepository.getEntityManager();
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        long behandlingId = prosessTaskData.getBehandlingId();
        TilbakekrevingsvedtakDto tilbakekrevingsvedtak = tilbakekrevingsvedtakTjeneste.lagTilbakekrevingsvedtak(behandlingId);
        lagreXml(behandlingId, tilbakekrevingsvedtak);
        lagSavepointOgIverksett(behandlingId, tilbakekrevingsvedtak);
    }

    private void lagSavepointOgIverksett(long behandlingId, TilbakekrevingsvedtakDto tilbakekrevingsvedtak) {
        RunWithSavepoint runWithSavepoint = new RunWithSavepoint(entityManager);
        runWithSavepoint.doWork(() -> {
            økonomiConsumer.iverksettTilbakekrevingsvedtak(behandlingId, tilbakekrevingsvedtak);
            log.info("Oversendte tilbakekrevingsvedtak til oppdragsystemet for behandling={}", behandlingId);
            return null;
        });
    }

    private void lagreXml(Long behandlingId, TilbakekrevingsvedtakDto tilbakekrevingsvedtak) {
        String xml =  TilbakekrevingsvedtakMarshaller.marshall(behandlingId, tilbakekrevingsvedtak);
        vedtakXmlRepository.lagre(behandlingId, xml);
        log.info("lagret vedtak-xml for behandling={}", behandlingId);
    }

    public interface SendØkonomiTilbakekrevingVedtakTaskFeil extends DeklarerteFeil {

        SendØkonomiTilbakekrevingVedtakTaskFeil FACTORY = FeilFactory.create(SendØkonomiTilbakekrevingVedtakTaskFeil.class);

        @TekniskFeil(feilkode = "FPT-113616", feilmelding = "Kunne ikke marshalle vedtak. BehandlingId=%s", logLevel = LogLevel.WARN)
        Feil kunneIkkeMarshalleVedtakXml(Long behandlingId, Exception e);

    }


}
