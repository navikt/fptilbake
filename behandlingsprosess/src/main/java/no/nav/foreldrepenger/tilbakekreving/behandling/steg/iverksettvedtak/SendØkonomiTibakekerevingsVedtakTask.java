package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.TilbakekrevingsvedtakMarshaller;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiResponsMarshaller;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingsvedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.MeldingType;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiSendtXmlRepository;
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakRequest;
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakResponse;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsvedtakDto;
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
    private ØkonomiSendtXmlRepository økonomiSendtXmlRepository;

    SendØkonomiTibakekerevingsVedtakTask() {
        // CDI krav
    }

    @Inject
    public SendØkonomiTibakekerevingsVedtakTask(TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste,
                                                ØkonomiConsumer økonomiConsumer,
                                                ØkonomiSendtXmlRepository økonomiSendtXmlRepository) {
        this.tilbakekrevingsvedtakTjeneste = tilbakekrevingsvedtakTjeneste;
        this.økonomiConsumer = økonomiConsumer;
        this.økonomiSendtXmlRepository = økonomiSendtXmlRepository;
        this.entityManager = økonomiSendtXmlRepository.getEntityManager();
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        long behandlingId = prosessTaskData.getBehandlingId();
        TilbakekrevingsvedtakDto tilbakekrevingsvedtak = tilbakekrevingsvedtakTjeneste.lagTilbakekrevingsvedtak(behandlingId);
        TilbakekrevingsvedtakRequest request = new TilbakekrevingsvedtakRequest();
        request.setTilbakekrevingsvedtak(tilbakekrevingsvedtak);
        Long sendtXmlId = lagreXml(behandlingId, request);
        lagSavepointOgIverksett(behandlingId, sendtXmlId, request);
    }

    private void lagSavepointOgIverksett(long behandlingId, long sendtXmlId, TilbakekrevingsvedtakRequest tilbakekrevingsvedtak) {
        RunWithSavepoint runWithSavepoint = new RunWithSavepoint(entityManager);
        runWithSavepoint.doWork(() -> {
            TilbakekrevingsvedtakResponse respons = økonomiConsumer.iverksettTilbakekrevingsvedtak(behandlingId, tilbakekrevingsvedtak);
            log.info("Oversendte tilbakekrevingsvedtak til oppdragsystemet for behandling={}", behandlingId);
            oppdatereRespons(behandlingId, sendtXmlId, respons);
            return null;
        });
    }

    private Long lagreXml(Long behandlingId, TilbakekrevingsvedtakRequest request) {
        String xml = TilbakekrevingsvedtakMarshaller.marshall(behandlingId, request);
        Long sendtXmlId = økonomiSendtXmlRepository.lagre(behandlingId, xml, MeldingType.VEDTAK);
        log.info("lagret vedtak-xml for behandling={}", behandlingId);
        return sendtXmlId;
    }

    private void oppdatereRespons(long behandlingId, long sendtXmlId, TilbakekrevingsvedtakResponse respons) {
        String responsXml = ØkonomiResponsMarshaller.marshall(respons, behandlingId);
        økonomiSendtXmlRepository.oppdatereKvittering(sendtXmlId, responsXml);
        log.info("oppdatert respons-xml for behandling={}", behandlingId);
    }


}
