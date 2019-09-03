package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagAggregate;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.util.FPDateUtil;

public abstract class FellesTask {

    private ProsessTaskRepository prosessTaskRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private FpsakKlient fpsakKlient;

    protected FellesTask() {
        // for CDI proxy
    }

    public FellesTask(ProsessTaskRepository taskRepository,KravgrunnlagRepository grunnlagRepository, FpsakKlient fpsakKlient) {
        this.prosessTaskRepository = taskRepository;
        this.grunnlagRepository = grunnlagRepository;
        this.fpsakKlient = fpsakKlient;
    }

    protected void opprettProsesstaskForÅSletteXml(Long mottattXmlId) {
        ProsessTaskData slettXmlTask = new ProsessTaskData(SlettMottattXmlTask.TASKTYPE);
        slettXmlTask.setNesteKjøringEtter(FPDateUtil.nå().plusMonths(3));
        slettXmlTask.setProperty(TaskProperty.PROPERTY_MOTTATT_XML_ID, Long.toString(mottattXmlId));
        prosessTaskRepository.lagre(slettXmlTask);
    }

    protected boolean erGyldigTall(String referanse) {
        return referanse != null && referanse.matches("^\\d*$");
    }

    protected boolean erBehandlingFinnesIFpsak(String saksnummer, String eksternBehandlingId) {
        return fpsakKlient.finnesBehandlingIFpsak(saksnummer,Long.valueOf(eksternBehandlingId));
    }

    protected String finnSaksnummer(String fagsystemId) {
        return fagsystemId.substring(0, fagsystemId.length() - 3);
    }

    protected Optional<KravgrunnlagAggregate> finnGrunnlagForVedtakId(long vedtakId) {
        return grunnlagRepository.finnGrunnlagForVedtakId(vedtakId);
    }

}
