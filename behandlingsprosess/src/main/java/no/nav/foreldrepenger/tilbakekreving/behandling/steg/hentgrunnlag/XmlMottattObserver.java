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
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

@ApplicationScoped
@ActivateRequestContext
@Transactional
public class XmlMottattObserver {

    private static final Logger log = LoggerFactory.getLogger(XmlMottattObserver.class);

    private ØkonomiMottattXmlRepository økonomiMottattXmlRepository;
    private ProsessTaskTjeneste taskTjeneste;

    XmlMottattObserver() {
        //for CDI proxy
    }

    @Inject
    public XmlMottattObserver(ØkonomiMottattXmlRepository økonomiMottattXmlRepository, ProsessTaskTjeneste taskTjeneste) {
        this.økonomiMottattXmlRepository = økonomiMottattXmlRepository;
        this.taskTjeneste = taskTjeneste;
    }

    public void observer(@Observes XmlMottattEvent event) {
        String innhold = event.mottattXml();
        Long mottattXmlId = økonomiMottattXmlRepository.lagreMottattXml(innhold);

        if (innhold.contains(ROOT_ELEMENT_KRAVGRUNNLAG_XML)) {
            lagreProsesTask(mottattXmlId, TaskType.forProsessTask(LesKravgrunnlagTask.class));
        } else if (innhold.contains(ROOT_ELEMENT_KRAV_VEDTAK_STATUS_XML)) {
            lagreProsesTask(mottattXmlId, TaskType.forProsessTask(LesKravvedtakStatusTask.class));
        } else {
            log.error("Mottok XML som ikke ble forstått, mottattXmlId={}", mottattXmlId);
        }

    }

    private void lagreProsesTask(Long mottattXmlId, TaskType taskType) {
        ProsessTaskData lesXmlTask = ProsessTaskData.forTaskType(taskType);
        lesXmlTask.setProperty(TaskProperty.PROPERTY_MOTTATT_XML_ID, Long.toString(mottattXmlId));
        taskTjeneste.lagre(lesXmlTask);
    }

}
