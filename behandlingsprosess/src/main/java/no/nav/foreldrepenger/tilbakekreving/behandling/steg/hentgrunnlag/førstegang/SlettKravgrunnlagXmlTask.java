package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagXmlRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(SlettKravgrunnlagXmlTask.TASKTYPE)
public class SlettKravgrunnlagXmlTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(SlettKravgrunnlagXmlTask.class);

    static final String TASKTYPE = "kravgrunnlag.slett.midlertidig.lagret.xml";

    private KravgrunnlagXmlRepository kravgrunnlagXmlRepository;

    SlettKravgrunnlagXmlTask() {
        //for CDI proxy
    }

    @Inject
    public SlettKravgrunnlagXmlTask(KravgrunnlagXmlRepository kravgrunnlagXmlRepository) {
        this.kravgrunnlagXmlRepository = kravgrunnlagXmlRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long kravgrunnlagXmlId = Long.valueOf(prosessTaskData.getPropertyValue(TaskProperty.PROPERTY_KRAVGRUNNLAG_XML_ID));

        kravgrunnlagXmlRepository.slettGrunnlagXml(kravgrunnlagXmlId);

        logger.info("Slettet kravgrunnlag med id={}", kravgrunnlagXmlId);
    }

}
