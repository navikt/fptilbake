package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(SlettMottattXmlTask.TASKTYPE)
public class SlettMottattXmlTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(SlettMottattXmlTask.class);

    public static final String TASKTYPE = "kravgrunnlag.slett.midlertidig.lagret.xml";

    private ØkonomiMottattXmlRepository økonomiMottattXmlRepository;

    SlettMottattXmlTask() {
        //for CDI proxy
    }

    @Inject
    public SlettMottattXmlTask(ØkonomiMottattXmlRepository økonomiMottattXmlRepository) {
        this.økonomiMottattXmlRepository = økonomiMottattXmlRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long mottattXmlId = Long.valueOf(prosessTaskData.getPropertyValue(TaskProperty.PROPERTY_MOTTATT_XML_ID));

        økonomiMottattXmlRepository.slettMottattXml(mottattXmlId);

        logger.info("Slettet kravgrunnlag med id={}", mottattXmlId);
    }

}
