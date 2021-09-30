package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.TilbakekrevingsvedtakMarshaller;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumerFeil;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiKvitteringTolk;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiResponsMarshaller;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingsvedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.MeldingType;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiSendtXmlRepository;
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
@ProsessTask("iverksetteVedtak.sendØkonomiTilbakekrevingsvedtak")
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class SendØkonomiTibakekerevingsVedtakTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(SendØkonomiTibakekerevingsVedtakTask.class);

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
        long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        TilbakekrevingsvedtakDto tilbakekrevingsvedtak = tilbakekrevingsvedtakTjeneste.lagTilbakekrevingsvedtak(behandlingId);
        TilbakekrevingsvedtakRequest request = lagRequest(tilbakekrevingsvedtak);
        Long sendtXmlId = lagreXml(behandlingId, request);
        lagSavepointOgIverksett(behandlingId, sendtXmlId, request);
    }

    private void lagSavepointOgIverksett(long behandlingId, long sendtXmlId, TilbakekrevingsvedtakRequest tilbakekrevingsvedtak) {
        RunWithSavepoint runWithSavepoint = new RunWithSavepoint(entityManager);
        runWithSavepoint.doWork(() -> {
            //setter før kall til OS, slik at requesten blir lagret selv om kallet feiler
            TilbakekrevingsvedtakResponse respons = økonomiConsumer.iverksettTilbakekrevingsvedtak(behandlingId, tilbakekrevingsvedtak);
            lagreRespons(behandlingId, sendtXmlId, respons);
            MmelDto kvittering = respons.getMmel();
            if (ØkonomiKvitteringTolk.erKvitteringOK(kvittering)) {
                logger.info("Tilbakekrevingsvedtak sendt til oppdragsystemet. BehandlingId={} Alvorlighetsgrad='{}' infomelding='{}'", behandlingId, kvittering.getAlvorlighetsgrad(), kvittering.getBeskrMelding());
            } else {
                RunWithSavepoint rwsp = new RunWithSavepoint(entityManager);
                rwsp.doWork(() -> {
                    //setter savepoint før feilen kastes, slik at kvitteringen blir lagret. Kaster feil for å feile prosesstasken, samt trigge logging
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

    private Long lagreXml(Long behandlingId, TilbakekrevingsvedtakRequest request) {
        String xml = TilbakekrevingsvedtakMarshaller.marshall(behandlingId, request);
        Long sendtXmlId = økonomiSendtXmlRepository.lagre(behandlingId, xml, MeldingType.VEDTAK);
        logger.info("lagret vedtak-xml for behandling={}", behandlingId);
        return sendtXmlId;
    }

    private void lagreRespons(long behandlingId, long sendtXmlId, TilbakekrevingsvedtakResponse respons) {
        String responsXml = ØkonomiResponsMarshaller.marshall(respons, behandlingId);
        økonomiSendtXmlRepository.oppdatereKvittering(sendtXmlId, responsXml);
        logger.info("oppdatert respons-xml for behandling={}", behandlingId);
    }
}
