package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag;

import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.util.FPDateUtil;

public abstract class FellesTask {

    private ProsessTaskRepository prosessTaskRepository;
    private FpsakKlient fpsakKlient;

    protected FellesTask(){
        // for CDI proxy
    }

    public FellesTask(ProsessTaskRepository taskRepository,FpsakKlient fpsakKlient) {
        this.prosessTaskRepository = taskRepository;
        this.fpsakKlient = fpsakKlient;
    }


    public void opprettProsesstaskForÅSletteXml(Long mottattXmlId) {
        ProsessTaskData slettXmlTask = new ProsessTaskData(SlettMottattXmlTask.TASKTYPE);
        slettXmlTask.setNesteKjøringEtter(FPDateUtil.nå().plusMonths(3));
        slettXmlTask.setProperty(TaskProperty.PROPERTY_MOTTATT_XML_ID, Long.toString(mottattXmlId));
        prosessTaskRepository.lagre(slettXmlTask);
    }

    public boolean erGyldigTall(String referanse) {
        return referanse != null && referanse.matches("^\\d*$");
    }

    public boolean erBehandlingFinnesIFpsak(String eksternBehandlingId) {
        return fpsakKlient.finnesBehandlingIFpsak(Long.valueOf(eksternBehandlingId));
    }

}
