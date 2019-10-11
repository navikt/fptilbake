package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiSendtXmlRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(SlettSendtXmlTask.TASKTYPE)
public class SlettSendtXmlTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "slett.midlertidig.lagret.xml";

    private ØkonomiSendtXmlRepository økonomiSendtXmlRepository;

    SlettSendtXmlTask() {
        //for CDI proxy
    }

    @Inject
    public SlettSendtXmlTask(ØkonomiSendtXmlRepository økonomiSendtXmlRepository) {
        this.økonomiSendtXmlRepository = økonomiSendtXmlRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long sendtXmlId = Long.valueOf(prosessTaskData.getPropertyValue(TaskProperty.PROPERTY_SENDT_XML_ID));

        økonomiSendtXmlRepository.slettSendtXml(sendtXmlId);

    }

}
