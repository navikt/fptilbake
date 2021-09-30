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
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask("kravvedtakstatus.les")
public class LesKravvedtakStatusTask extends FellesTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(LesKravvedtakStatusTask.class);

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
            kravVedtakStatusTjeneste.håndteresMottakAvKravVedtakStatus(behandlingId, kravVedtakStatus437);
            økonomiMottattXmlRepository.opprettTilkobling(mottattXmlId);
            logger.info("Tilkoblet kravVedtakStatus med id={} saksnummer={} behandlingId={}", mottattXmlId, saksnummer, behandlingId);
        } else {
            validerBehandlingsEksistens(henvisning, saksnummer);
            logger.info("Ignorerte kravVedtakStatus med id={} saksnummer={}. Fantes ikke tilbakekrevingsbehandling", mottattXmlId, saksnummer);
        }

    }

    private void validerHenvisning(Henvisning henvisning) {
        if (!Henvisning.erGyldig(henvisning)) {
            throw new TekniskException("FPT-675364",
                String.format("Mottok et kravOgVedtakStatus fra Økonomi med henvisning i ikke-støttet format, henvisning=%s. KravOgVedtakStatus skulle kanskje til et annet system. Si i fra til Økonomi!",
                henvisning));
        }
    }

    private void validerBehandlingsEksistens(Henvisning henvisning, String saksnummer) {
        if (!finnesYtelsesbehandling(saksnummer, henvisning)) {
            throw new TekniskException("FPT-587196",
                String.format("Mottok et kravOgVedtakStatus fra Økonomi for en behandling som ikke finnes i fpsak. henvisning=%s. Kravgrunnlaget skulle kanskje til et annet system. Si i fra til Økonomi!", henvisning));
        }
    }


}
