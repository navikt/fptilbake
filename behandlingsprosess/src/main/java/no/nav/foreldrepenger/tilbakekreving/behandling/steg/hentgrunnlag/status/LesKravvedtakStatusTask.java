package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTask;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatus437;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.ØkonomiMottattXmlRepository;
import no.nav.tilbakekreving.status.v1.KravOgVedtakstatus;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
@ProsessTask(LesKravvedtakStatusTask.TASKTYPE)
public class LesKravvedtakStatusTask extends FellesTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(LesKravvedtakStatusTask.class);

    public static final String TASKTYPE = "kravvedtakstatus.les";

    private ØkonomiMottattXmlRepository økonomiMottattXmlRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;

    private KravVedtakStatusTjeneste kravVedtakStatusTjeneste;
    private KravVedtakStatusMapper statusMapper;


    LesKravvedtakStatusTask() {
        // for CDI proxy
    }

    @Inject
    public LesKravvedtakStatusTask(ØkonomiMottattXmlRepository økonomiMottattXmlRepository, EksternBehandlingRepository eksternBehandlingRepository,
                                   ProsessTaskRepository prosessTaskRepository, KravVedtakStatusTjeneste kravVedtakStatusTjeneste,
                                   KravVedtakStatusMapper statusMapper, FpsakKlient fpsakKlient) {
        super(prosessTaskRepository, fpsakKlient);
        this.økonomiMottattXmlRepository = økonomiMottattXmlRepository;
        this.eksternBehandlingRepository = eksternBehandlingRepository;

        this.kravVedtakStatusTjeneste = kravVedtakStatusTjeneste;
        this.statusMapper = statusMapper;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long mottattXmlId = Long.valueOf(prosessTaskData.getPropertyValue(TaskProperty.PROPERTY_MOTTATT_XML_ID));
        String råXml = økonomiMottattXmlRepository.hentMottattXml(mottattXmlId);

        KravOgVedtakstatus kravOgVedtakstatus = KravVedtakStatusXmlUnmarshaller.unmarshall(mottattXmlId, råXml);
        KravVedtakStatus437 kravVedtakStatus437 = statusMapper.mapTilDomene(kravOgVedtakstatus);
        String saksnummer = finnSaksnummer(kravOgVedtakstatus.getFagsystemId());

        String eksternBehandlingId = statusMapper.finnBehandlngId(kravOgVedtakstatus);
        økonomiMottattXmlRepository.oppdaterMedEksternBehandlingId(eksternBehandlingId, mottattXmlId);

        Optional<EksternBehandling> behandlingKobling = hentKoblingTilInternBehandling(eksternBehandlingId);
        if (behandlingKobling.isPresent()) {
            Long internId = behandlingKobling.get().getInternId();
            kravVedtakStatusTjeneste.håndteresMottakAvKravVedtakStatus(internId, kravVedtakStatus437);
            logger.info("Leste kravVedtakStatus med id={} eksternBehandlingId={} internBehandlingId={}", mottattXmlId, eksternBehandlingId, internId);
        } else {
            validerBehandlingsEksistens(eksternBehandlingId, saksnummer);
            logger.info("Ignorerte kravVedtakStatus med id={} eksternBehandlingId={}. Fantes ikke tilbakekrevingsbehandling", mottattXmlId, eksternBehandlingId);
        }

        opprettProsesstaskForÅSletteXml(mottattXmlId);
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
            throw LesKravvedtakStatusTask.LesKravvedtakStatusTaskFeil.FACTORY.behandlingFinnesIkkeIFpsak(eksternBehandlingId).toException();
        }
        if (!erBehandlingFinnesIFpsak(saksnummer)) {
            throw LesKravvedtakStatusTask.LesKravvedtakStatusTaskFeil.FACTORY.behandlingFinnesIkkeIFpsak(Long.valueOf(eksternBehandlingId)).toException();
        }
    }

    public interface LesKravvedtakStatusTaskFeil extends DeklarerteFeil {

        LesKravvedtakStatusTask.LesKravvedtakStatusTaskFeil FACTORY = FeilFactory.create(LesKravvedtakStatusTask.LesKravvedtakStatusTaskFeil.class);

        @TekniskFeil(feilkode = "FPT-587196",
            feilmelding = "Mottok et tilbakekrevingsgrunnlag fra Økonomi for en behandling som ikke finnes i fpsak. behandlingId=%s. Kravgrunnlaget skulle kanskje til et annet system. Si i fra til Økonomi!",
            logLevel = LogLevel.WARN)
        Feil behandlingFinnesIkkeIFpsak(Long behandlingId);

        @TekniskFeil(feilkode = "FPT-675364",
            feilmelding = "Mottok et kravOgVedtakStatus fra Økonomi med behandlingId som ikke er et tall. behandlingId=%s. KravOgVedtakStatus skulle kanskje til et annet system. Si i fra til Økonomi!",
            logLevel = LogLevel.WARN)
        Feil behandlingFinnesIkkeIFpsak(String behandlingId);
    }


}
