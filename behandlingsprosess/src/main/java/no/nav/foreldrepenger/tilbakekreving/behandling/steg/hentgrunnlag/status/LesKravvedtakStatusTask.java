package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTask;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.FagsystemId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatus437;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.tilbakekreving.status.v1.KravOgVedtakstatus;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(LesKravvedtakStatusTask.TASKTYPE)
public class LesKravvedtakStatusTask extends FellesTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(LesKravvedtakStatusTask.class);

    public static final String TASKTYPE = "kravvedtakstatus.les";

    private ØkonomiMottattXmlRepository økonomiMottattXmlRepository;
    private KravgrunnlagRepository kravgrunnlagRepository;

    private KravVedtakStatusTjeneste kravVedtakStatusTjeneste;
    private KravVedtakStatusMapper statusMapper;


    LesKravvedtakStatusTask() {
        // for CDI proxy
    }

    @Inject
    public LesKravvedtakStatusTask(ØkonomiMottattXmlRepository økonomiMottattXmlRepository,
                                   BehandlingRepositoryProvider repositoryProvider,
                                   KravVedtakStatusTjeneste kravVedtakStatusTjeneste,
                                   KravVedtakStatusMapper statusMapper,
                                   FagsystemKlient fagsystemKlient) {
        super(repositoryProvider.getBehandlingRepository(), fagsystemKlient);
        this.økonomiMottattXmlRepository = økonomiMottattXmlRepository;
        this.kravgrunnlagRepository = repositoryProvider.getGrunnlagRepository();

        this.kravVedtakStatusTjeneste = kravVedtakStatusTjeneste;
        this.statusMapper = statusMapper;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long mottattXmlId = Long.valueOf(prosessTaskData.getPropertyValue(TaskProperty.PROPERTY_MOTTATT_XML_ID));
        String råXml = økonomiMottattXmlRepository.hentMottattXml(mottattXmlId);

        KravOgVedtakstatus kravOgVedtakstatus = KravVedtakStatusXmlUnmarshaller.unmarshall(mottattXmlId, råXml);
        KravVedtakStatus437 kravVedtakStatus437 = statusMapper.mapTilDomene(kravOgVedtakstatus);
        String saksnummer = FagsystemId.parse(kravOgVedtakstatus.getFagsystemId()).getSaksnummer().getVerdi();
        long vedtakId = kravOgVedtakstatus.getVedtakId().longValue();
        //TODO k9-tilbake bytt String->Saksnummer
        Henvisning henvisning = kravVedtakStatus437.getReferanse();
        validerHenvisning(henvisning);
        økonomiMottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(henvisning, saksnummer, mottattXmlId);

        Optional<Behandling> åpenTilbakekrevingBehandling = finnÅpenTilbakekrevingBehandling(saksnummer);
        if (åpenTilbakekrevingBehandling.isPresent()) {
            Long behandlingId = åpenTilbakekrevingBehandling.get().getId();
            if (harTilkobletGrunnlag(vedtakId)) {
                kravVedtakStatusTjeneste.håndteresMottakAvKravVedtakStatus(behandlingId, kravVedtakStatus437);
                økonomiMottattXmlRepository.opprettTilkobling(mottattXmlId);
                logger.info("Tilkoblet kravVedtakStatus med id={} saksnummer={} behandlingId={}", mottattXmlId, saksnummer, behandlingId);
            } else {
                throw LesKravvedtakStatusTaskFeil.FACTORY.ugyldigVedtakId(vedtakId, mottattXmlId).toException();
            }

        } else {
            validerBehandlingsEksistens(henvisning, saksnummer);
            logger.info("Ignorerte kravVedtakStatus med id={} saksnummer={}. Fantes ikke tilbakekrevingsbehandling", mottattXmlId, saksnummer);
        }

    }

    private boolean harTilkobletGrunnlag(long vedtakId) {
        return kravgrunnlagRepository.finnGrunnlagForVedtakId(vedtakId).isPresent();
    }

    private void validerHenvisning(Henvisning henvisning) {
        if (!Henvisning.erGyldig(henvisning)) {
            throw LesKravvedtakStatusTaskFeil.FACTORY.ugyldigHenvisning(henvisning).toException();
        }
    }

    private void validerBehandlingsEksistens(Henvisning henvisning, String saksnummer) {
        if (!finnesYtelsesbehandling(saksnummer, henvisning)) {
            throw LesKravvedtakStatusTaskFeil.FACTORY.behandlingFinnesIkkeIFpsak(henvisning).toException();
        }
    }

    public interface LesKravvedtakStatusTaskFeil extends DeklarerteFeil {

        LesKravvedtakStatusTask.LesKravvedtakStatusTaskFeil FACTORY = FeilFactory.create(LesKravvedtakStatusTask.LesKravvedtakStatusTaskFeil.class);

        @TekniskFeil(feilkode = "FPT-587196",
            feilmelding = "Mottok et kravOgVedtakStatus fra Økonomi for en behandling som ikke finnes i fpsak. henvisning=%s. Kravgrunnlaget skulle kanskje til et annet system. Si i fra til Økonomi!",
            logLevel = LogLevel.WARN)
        Feil behandlingFinnesIkkeIFpsak(Henvisning henvisning);

        @TekniskFeil(feilkode = "FPT-675364",
            feilmelding = "Mottok et kravOgVedtakStatus fra Økonomi med henvisning i ikke-støttet format, henvisning=%s. KravOgVedtakStatus skulle kanskje til et annet system. Si i fra til Økonomi!",
            logLevel = LogLevel.WARN)
        Feil ugyldigHenvisning(Henvisning henvisning);

        @TekniskFeil(feilkode = "FPT-675365",
            feilmelding = "Mottok et kravOgVedtakStatus fra Økonomi med vedtakId som ikke finnes, vedtakId=%s, mottattXmlId=%s. " +
                "KravOgVedtakStatus skulle kanskje til et annet system. Si i fra til Økonomi!",
            logLevel = LogLevel.WARN)
        Feil ugyldigVedtakId(long vedtakId, long mottattXmlId);

    }


}
