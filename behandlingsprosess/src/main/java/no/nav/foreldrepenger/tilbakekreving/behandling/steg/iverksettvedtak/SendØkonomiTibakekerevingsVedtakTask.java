package no.nav.foreldrepenger.tilbakekreving.behandling.steg.iverksettvedtak;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiResponsMarshaller;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingsvedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.MeldingType;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiSendtXmlRepository;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsvedtakDto;
import no.nav.tilbakekreving.typer.v1.MmelDto;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.jpa.savepoint.RunWithSavepoint;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
@ProsessTask(SendØkonomiTibakekerevingsVedtakTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class SendØkonomiTibakekerevingsVedtakTask extends FellesTask implements ProsessTaskHandler {
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
    public SendØkonomiTibakekerevingsVedtakTask(TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste, ØkonomiConsumer økonomiConsumer,
                                                ØkonomiSendtXmlRepository økonomiSendtXmlRepository, ProsessTaskRepository prosessTaskRepository) {
        super(prosessTaskRepository, null, null);
        this.tilbakekrevingsvedtakTjeneste = tilbakekrevingsvedtakTjeneste;
        this.økonomiConsumer = økonomiConsumer;
        this.økonomiSendtXmlRepository = økonomiSendtXmlRepository;
        this.entityManager = økonomiSendtXmlRepository.getEntityManager();
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        long behandlingId = prosessTaskData.getBehandlingId();
        TilbakekrevingsvedtakDto tilbakekrevingsvedtak = tilbakekrevingsvedtakTjeneste.lagTilbakekrevingsvedtak(behandlingId);
        Long sendtXmlId = lagreXml(behandlingId, tilbakekrevingsvedtak);
        opprettProsesstaskForÅSletteSendtXml(sendtXmlId);
        lagSavepointOgIverksett(behandlingId, sendtXmlId, tilbakekrevingsvedtak);
    }

    private void lagSavepointOgIverksett(long behandlingId, long sendtXmlId, TilbakekrevingsvedtakDto tilbakekrevingsvedtak) {
        RunWithSavepoint runWithSavepoint = new RunWithSavepoint(entityManager);
        runWithSavepoint.doWork(() -> {
            MmelDto respons = økonomiConsumer.iverksettTilbakekrevingsvedtak(behandlingId, tilbakekrevingsvedtak);
            log.info("Oversendte tilbakekrevingsvedtak til oppdragsystemet for behandling={}", behandlingId);
            oppdatereRespons(behandlingId, sendtXmlId, respons);
            return null;
        });
    }

    private Long lagreXml(Long behandlingId, TilbakekrevingsvedtakDto tilbakekrevingsvedtak) {
        String xml = TilbakekrevingsvedtakMarshaller.marshall(behandlingId, tilbakekrevingsvedtak);
        Long sendtXmlId = økonomiSendtXmlRepository.lagre(behandlingId, xml, MeldingType.VEDTAK);
        log.info("lagret vedtak-xml for behandling={}", behandlingId);
        return sendtXmlId;
    }

    private void oppdatereRespons(long behandlingId, long sendtXmlId, MmelDto respons) {
        String responsXml = ØkonomiResponsMarshaller.marshall(behandlingId, respons);
        økonomiSendtXmlRepository.oppdatereKvittering(sendtXmlId, responsXml);
        log.info("oppdatert respons-xml for behandling={}", behandlingId);
    }

    public interface SendØkonomiTilbakekrevingVedtakTaskFeil extends DeklarerteFeil {

        SendØkonomiTilbakekrevingVedtakTaskFeil FACTORY = FeilFactory.create(SendØkonomiTilbakekrevingVedtakTaskFeil.class);

        @TekniskFeil(feilkode = "FPT-113616", feilmelding = "Kunne ikke marshalle vedtak. BehandlingId=%s", logLevel = LogLevel.WARN)
        Feil kunneIkkeMarshalleVedtakXml(Long behandlingId, Exception e);

    }


}
