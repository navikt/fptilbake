package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagXmlRepository;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlag;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
@ProsessTask(LesKravgrunnlagTask.TASKTYPE)
public class LesKravgrunnlagTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(LesKravgrunnlagTask.class);

    public static final String TASKTYPE = "kravgrunnlag.les";

    private KravgrunnlagXmlRepository kravgrunnlagXmlRepository;
    private KravgrunnlagTjeneste kravgrunnlagTjeneste;
    private KravgrunnlagMapper kravgrunnlagMapper;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private ProsessTaskRepository prosessTaskRepository;
    private FpsakKlient fpsakKlient;

    LesKravgrunnlagTask() {
        //for CDI proxy
    }

    @Inject
    public LesKravgrunnlagTask(KravgrunnlagXmlRepository kravgrunnlagXmlRepository, KravgrunnlagTjeneste kravgrunnlagTjeneste, KravgrunnlagMapper kravgrunnlagMapper, EksternBehandlingRepository eksternBehandlingRepository, ProsessTaskRepository prosessTaskRepository, FpsakKlient fpsakKlient) {
        this.kravgrunnlagXmlRepository = kravgrunnlagXmlRepository;
        this.kravgrunnlagTjeneste = kravgrunnlagTjeneste;
        this.kravgrunnlagMapper = kravgrunnlagMapper;
        this.eksternBehandlingRepository = eksternBehandlingRepository;
        this.prosessTaskRepository = prosessTaskRepository;
        this.fpsakKlient = fpsakKlient;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long kravgrunnlagXmlId = Long.valueOf(prosessTaskData.getPropertyValue(TaskProperty.PROPERTY_KRAVGRUNNLAG_XML_ID));

        String råXml = kravgrunnlagXmlRepository.hentKravgrunnlagXml(kravgrunnlagXmlId);
        DetaljertKravgrunnlag kravgrunnlagDto = KravgrunnlagXmlUnmarshaller.unmarshall(kravgrunnlagXmlId, råXml);
        String eksternBehandlingId = kravgrunnlagMapper.finnBehandlngId(kravgrunnlagDto);
        Kravgrunnlag431 kravgrunnlag = kravgrunnlagMapper.mapTilDomene(kravgrunnlagDto);

        kravgrunnlagXmlRepository.oppdaterMedEksternBehandlingId(eksternBehandlingId, kravgrunnlagXmlId);

        Optional<EksternBehandling> behandlingKobling = hentKoblingTilInternBehandling(eksternBehandlingId);
        if (behandlingKobling.isPresent()) {
            Long internId = behandlingKobling.get().getInternId();
            //TODO gjør bare lagring av grunnlag her, splitt gjennopptagelse etc til egen task
            kravgrunnlagTjeneste.lagreTilbakekrevingsgrunnlagFraØkonomi(internId, kravgrunnlag);
            logger.info("Leste kravgrunnlag med id={} eksternBehandlingId={} internBehandlingId={}", kravgrunnlagXmlId, eksternBehandlingId, internId);
        } else {
            validerBehandlingsEksistens(eksternBehandlingId);
            logger.info("Ignorerte kravgrunnlag med id={} eksternBehandlingId={}. Fantes ikke tilbakekrevingsbehandling", kravgrunnlagXmlId, eksternBehandlingId);
        }
        opprettProsesstaskForÅSletteXml(kravgrunnlagXmlId);
    }

    private Optional<EksternBehandling> hentKoblingTilInternBehandling(String referanse) {
        if (erGyldigTall(referanse)) {
            long eksternBehandlingId = Long.parseLong(referanse);
            return eksternBehandlingRepository.hentFraEksternId(eksternBehandlingId);
        }
        LesKravgrunnlagTaskFeil.FACTORY.ikkeBehandlingIdIReferanse(referanse).log(logger);
        return Optional.empty();
    }

    private void validerBehandlingsEksistens(String eksternBehandlingId) {
        if (!erGyldigTall(eksternBehandlingId)) {
            throw LesKravgrunnlagTaskFeil.FACTORY.behandlingFinnesIkkeIFpsak(eksternBehandlingId).toException();
        }
        Long fpsakBehandlingId = Long.valueOf(eksternBehandlingId);
        boolean behandlingFinnes = fpsakKlient.finnesBehandlingIFpsak(fpsakBehandlingId);
        if (!behandlingFinnes) {
            throw LesKravgrunnlagTaskFeil.FACTORY.behandlingFinnesIkkeIFpsak(fpsakBehandlingId).toException();
        }
    }

    private void opprettProsesstaskForÅSletteXml(Long kravgrunnlagXmlId) {
        ProsessTaskData slettXmlTask = new ProsessTaskData(SlettKravgrunnlagXmlTask.TASKTYPE);
        slettXmlTask.setNesteKjøringEtter(FPDateUtil.nå().plusMonths(3));
        slettXmlTask.setProperty(TaskProperty.PROPERTY_KRAVGRUNNLAG_XML_ID, Long.toString(kravgrunnlagXmlId));
        prosessTaskRepository.lagre(slettXmlTask);
    }

    private boolean erGyldigTall(String referanse) {
        return referanse != null && referanse.matches("^\\d*$");
    }

    public interface LesKravgrunnlagTaskFeil extends DeklarerteFeil {

        LesKravgrunnlagTaskFeil FACTORY = FeilFactory.create(LesKravgrunnlagTaskFeil.class);

        @TekniskFeil(feilkode = "FPT-107926", feilmelding = "Skulle hatt behandlingId i referanse, men fikk '%s'", logLevel = LogLevel.WARN)
        Feil ikkeBehandlingIdIReferanse(String referanse);

        @TekniskFeil(feilkode = "FPT-587195",
                feilmelding = "Mottok et tilbakekrevingsgrunnlag fra Økonomi for en behandling som ikke finnes i fpsak. behandlingId=%s. Kravgrunnlaget skulle kanskje til et annet system. Si i fra til Økonomi!",
                logLevel = LogLevel.WARN)
        Feil behandlingFinnesIkkeIFpsak(Long behandlingId);

        @TekniskFeil(feilkode = "FPT-675363",
                feilmelding = "Mottok et tilbakekrevingsgrunnlag fra Økonomi med behandlingId som ikke er et tall. behandlingId=%s. Kravgrunnlaget skulle kanskje til et annet system. Si i fra til Økonomi!",
                logLevel = LogLevel.WARN)
        Feil behandlingFinnesIkkeIFpsak(String behandlingId);
    }

}
