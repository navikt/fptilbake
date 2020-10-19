package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTask;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.FagsystemId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatus437;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagAggregate;
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
    private EksternBehandlingRepository eksternBehandlingRepository;
    private BehandlingRepository behandlingRepository;

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
        super(repositoryProvider.getGrunnlagRepository(), fagsystemKlient);
        this.økonomiMottattXmlRepository = økonomiMottattXmlRepository;
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();

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
        //TODO k9-tilbake bytt String->Saksnummer
        Henvisning henvisning = kravVedtakStatus437.getReferanse();
        validerHenvisning(henvisning);
        økonomiMottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(henvisning, saksnummer, mottattXmlId);

        long vedtakId = statusMapper.finnVedtakId(kravOgVedtakstatus);
        oppdatereEksternBehandling(vedtakId, henvisning);

        Optional<EksternBehandling> behandlingKobling = hentKoblingTilInternBehandling(henvisning);
        if (behandlingKobling.isPresent()) {
            Long internId = behandlingKobling.get().getInternId();
            kravVedtakStatusTjeneste.håndteresMottakAvKravVedtakStatus(internId, kravVedtakStatus437);
            økonomiMottattXmlRepository.opprettTilkobling(mottattXmlId);
            logger.info("Tilkoblet kravVedtakStatus med id={} henvisning={} internBehandlingId={}", mottattXmlId, henvisning, internId);
        } else {
            validerBehandlingsEksistens(henvisning, saksnummer);
            logger.info("Ignorerte kravVedtakStatus med id={} henvisning={}. Fantes ikke tilbakekrevingsbehandling", mottattXmlId, henvisning);
        }

    }

    private Optional<EksternBehandling> hentKoblingTilInternBehandling(Henvisning referanse) {
        return eksternBehandlingRepository.hentFraHenvisning(referanse);
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

    private void oppdatereEksternBehandling(long vedtakId, Henvisning henvisning) {
        Optional<KravgrunnlagAggregate> aggregateOpt = finnGrunnlagForVedtakId(vedtakId);
        if (aggregateOpt.isPresent()) {
            logger.info("Grunnlag finnes allerede for vedtakId={}", vedtakId);
            KravgrunnlagAggregate aggregate = aggregateOpt.get();
            Henvisning referanse = aggregate.getGrunnlagØkonomi().getReferanse();
            Long behandlingId = aggregate.getBehandlingId();
            Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
            if (behandling.erAvsluttet()) {
                logger.info("Behandling {} er avsluttet og kan ikke tilkobles med meldingen", behandling.getId());
                return;
            }
            boolean eksternBehandlingFinnes = eksternBehandlingRepository.finnesEksternBehandling(behandlingId, henvisning);
            if (!referanse.equals(henvisning) && !eksternBehandlingFinnes) {
                UUID eksternUUID = hentUUIDFraEksternBehandling(behandlingId);
                logger.info("Oppdaterer eksternBehandling for behandlingId={} med ny henvisning={}", behandlingId, henvisning);
                EksternBehandling eksternBehandling = new EksternBehandling(behandling, henvisning, eksternUUID);
                eksternBehandlingRepository.lagre(eksternBehandling);
            } else {
                logger.info("henvisning={} finnes allerede. Oppdaterer ikke eksternBehandling", henvisning);
            }

        }
    }

    private UUID hentUUIDFraEksternBehandling(long behandlingId) {
        EksternBehandling forrigeEksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);
        return forrigeEksternBehandling.getEksternUuid();
    }

    public interface LesKravvedtakStatusTaskFeil extends DeklarerteFeil {

        LesKravvedtakStatusTask.LesKravvedtakStatusTaskFeil FACTORY = FeilFactory.create(LesKravvedtakStatusTask.LesKravvedtakStatusTaskFeil.class);

        @TekniskFeil(feilkode = "FPT-587196",
            feilmelding = "Mottok et tilbakekrevingsgrunnlag fra Økonomi for en behandling som ikke finnes i fpsak. henvisning=%s. Kravgrunnlaget skulle kanskje til et annet system. Si i fra til Økonomi!",
            logLevel = LogLevel.WARN)
        Feil behandlingFinnesIkkeIFpsak(Henvisning henvisning);

        @TekniskFeil(feilkode = "FPT-675364",
            feilmelding = "Mottok et kravOgVedtakStatus fra Økonomi med henvisning i ikke-støttet format, henvisning=%s. KravOgVedtakStatus skulle kanskje til et annet system. Si i fra til Økonomi!",
            logLevel = LogLevel.WARN)
        Feil ugyldigHenvisning(Henvisning henvisning);

    }


}
