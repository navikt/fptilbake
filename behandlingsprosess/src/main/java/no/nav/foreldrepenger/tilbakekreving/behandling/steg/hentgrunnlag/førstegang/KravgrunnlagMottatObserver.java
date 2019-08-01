package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagXmlRepository;
import no.nav.foreldrepenger.tilbakekreving.kravgrunnlag.queue.consumer.KravgrunnlagMottattEvent;
import no.nav.vedtak.felles.AktiverContextOgTransaksjon;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
@AktiverContextOgTransaksjon
public class KravgrunnlagMottatObserver {

    private KravgrunnlagXmlRepository kravgrunnlagXmlRepository;
    private ProsessTaskRepository prosessTaskRepository;

    KravgrunnlagMottatObserver() {
        //for CDI proxy
    }

    @Inject
    public KravgrunnlagMottatObserver(KravgrunnlagXmlRepository kravgrunnlagXmlRepository, ProsessTaskRepository prosessTaskRepository) {
        this.kravgrunnlagXmlRepository = kravgrunnlagXmlRepository;
        this.prosessTaskRepository = prosessTaskRepository;
    }

    public void observer(@Observes KravgrunnlagMottattEvent event) {
        String innhold = event.getKravgrunnlagXml();
        Long kravgrunnlagXmlId = kravgrunnlagXmlRepository.lagreKravgrunnlagXml(innhold);

        ProsessTaskData lesXmlTask = new ProsessTaskData(LesKravgrunnlagTask.TASKTYPE);
        lesXmlTask.setProperty(TaskProperty.PROPERTY_KRAVGRUNNLAG_XML_ID, Long.toString(kravgrunnlagXmlId));
        prosessTaskRepository.lagre(lesXmlTask);
    }

}
