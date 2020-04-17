package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag;

import static no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty.ROOT_ELEMENT_KRAVGRUNNLAG_XML;
import static no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty.ROOT_ELEMENT_KRAV_VEDTAK_STATUS_XML;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.LesKravgrunnlagTask;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status.LesKravvedtakStatusTask;
import no.nav.foreldrepenger.tilbakekreving.kravgrunnlag.queue.consumer.XmlMottattEvent;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
@ActivateRequestContext
@Transactional
public class XmlMottattObserver {

    private static final Logger log = LoggerFactory.getLogger(XmlMottattObserver.class);

    private ØkonomiMottattXmlRepository økonomiMottattXmlRepository;
    private ProsessTaskRepository prosessTaskRepository;

    XmlMottattObserver() {
        //for CDI proxy
    }

    @Inject
    public XmlMottattObserver(ØkonomiMottattXmlRepository økonomiMottattXmlRepository, ProsessTaskRepository prosessTaskRepository) {
        this.økonomiMottattXmlRepository = økonomiMottattXmlRepository;
        this.prosessTaskRepository = prosessTaskRepository;
    }

    public void observer(@Observes XmlMottattEvent event) {
        String innhold = event.getMottattXml();
        Long mottattXmlId = økonomiMottattXmlRepository.lagreMottattXml(innhold);

        if (innhold.contains(ROOT_ELEMENT_KRAVGRUNNLAG_XML)) {
            lagreProsesTask(mottattXmlId, LesKravgrunnlagTask.TASKTYPE);
        } else if (innhold.contains(ROOT_ELEMENT_KRAV_VEDTAK_STATUS_XML)) {
            lagreProsesTask(mottattXmlId, LesKravvedtakStatusTask.TASKTYPE);
        } else {
            log.error("Mottok XML som ikke ble forstått, mottattXmlId={}", mottattXmlId);
        }

    }

    private void lagreProsesTask(Long mottattXmlId, String taskType) {
        ProsessTaskData lesXmlTask = new ProsessTaskData(taskType);
        lesXmlTask.setProperty(TaskProperty.PROPERTY_MOTTATT_XML_ID, Long.toString(mottattXmlId));
        prosessTaskRepository.lagre(lesXmlTask);
    }

}
