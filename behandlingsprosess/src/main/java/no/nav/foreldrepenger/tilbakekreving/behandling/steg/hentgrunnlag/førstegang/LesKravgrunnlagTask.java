package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTask;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

@ApplicationScoped
@ProsessTask(LesKravgrunnlagTask.TASKTYPE)
public class LesKravgrunnlagTask extends FellesTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(LesKravgrunnlagTask.class);

    public static final String TASKTYPE = "kravgrunnlag.les";

    private ØkonomiMottattXmlRepository økonomiMottattXmlRepository;
    private KravgrunnlagTjeneste kravgrunnlagTjeneste;
    private KravgrunnlagMapper kravgrunnlagMapper;
    private EksternBehandlingRepository eksternBehandlingRepository;

    LesKravgrunnlagTask() {
        //for CDI proxy
    }

    @Inject
    public LesKravgrunnlagTask(ØkonomiMottattXmlRepository økonomiMottattXmlRepository, KravgrunnlagTjeneste kravgrunnlagTjeneste,
                               ProsessTaskRepository taskRepository, KravgrunnlagMapper kravgrunnlagMapper,
                               BehandlingRepositoryProvider repositoryProvider, FpsakKlient fpsakKlient) {
        super(taskRepository, repositoryProvider.getGrunnlagRepository(), fpsakKlient);
        this.økonomiMottattXmlRepository = økonomiMottattXmlRepository;
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();

        this.kravgrunnlagTjeneste = kravgrunnlagTjeneste;
        this.kravgrunnlagMapper = kravgrunnlagMapper;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long mottattXmlId = Long.valueOf(prosessTaskData.getPropertyValue(TaskProperty.PROPERTY_MOTTATT_XML_ID));

        String råXml = økonomiMottattXmlRepository.hentMottattXml(mottattXmlId);
        DetaljertKravgrunnlag kravgrunnlagDto = KravgrunnlagXmlUnmarshaller.unmarshall(mottattXmlId, råXml);
        String eksternBehandlingId = kravgrunnlagMapper.finnBehandlngId(kravgrunnlagDto);
        String saksnummer = finnSaksnummer(kravgrunnlagDto.getFagsystemId());
        Kravgrunnlag431 kravgrunnlag = kravgrunnlagMapper.mapTilDomene(kravgrunnlagDto);

        økonomiMottattXmlRepository.oppdaterMedEksternBehandlingId(eksternBehandlingId, mottattXmlId);

        Optional<EksternBehandling> behandlingKobling = hentKoblingTilInternBehandling(eksternBehandlingId);
        if (behandlingKobling.isPresent()) {
            Long internId = behandlingKobling.get().getInternId();
            //TODO gjør bare lagring av grunnlag her, splitt gjennopptagelse etc til egen task
            kravgrunnlagTjeneste.lagreTilbakekrevingsgrunnlagFraØkonomi(internId, kravgrunnlag);
            logger.info("Leste kravgrunnlag med id={} eksternBehandlingId={} internBehandlingId={}", mottattXmlId, eksternBehandlingId, internId);
        } else {
            validerBehandlingsEksistens(eksternBehandlingId, saksnummer);
            logger.info("Ignorerte kravgrunnlag med id={} eksternBehandlingId={}. Fantes ikke tilbakekrevingsbehandling", mottattXmlId, eksternBehandlingId);
        }
    }

    private Optional<EksternBehandling> hentKoblingTilInternBehandling(String referanse) {
        if (erGyldigTall(referanse)) {
            long eksternBehandlingId = Long.parseLong(referanse);
            return eksternBehandlingRepository.hentFraEksternId(eksternBehandlingId);
        }
        return Optional.empty();
    }

    private void validerBehandlingsEksistens(String eksternBehandlingId, String saksnummer) {
        if (!erGyldigTall(eksternBehandlingId)) {
            throw LesKravgrunnlagTaskFeil.FACTORY.behandlingFinnesIkkeIFpsak(eksternBehandlingId).toException();
        }
        if (!erBehandlingFinnesIFpsak(saksnummer,eksternBehandlingId)) {
            throw LesKravgrunnlagTaskFeil.FACTORY.behandlingFinnesIkkeIFpsak(Long.valueOf(eksternBehandlingId)).toException();
        }
    }

    public interface LesKravgrunnlagTaskFeil extends DeklarerteFeil {

        LesKravgrunnlagTaskFeil FACTORY = FeilFactory.create(LesKravgrunnlagTaskFeil.class);

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
