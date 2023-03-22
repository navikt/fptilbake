package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTask;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.FagsystemId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask("kravvedtakstatus.les")
public class LesKravvedtakStatusTask extends FellesTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(LesKravvedtakStatusTask.class);

    private ØkonomiMottattXmlRepository økonomiMottattXmlRepository;
    private KravVedtakStatusTjeneste kravVedtakStatusTjeneste;
    private KravVedtakStatusMapper statusMapper;


    LesKravvedtakStatusTask() {
        // for CDI proxy
    }

    @Inject
    public LesKravvedtakStatusTask(ØkonomiMottattXmlRepository økonomiMottattXmlRepository,
                                   BehandlingRepository behandlingRepository,
                                   KravVedtakStatusTjeneste kravVedtakStatusTjeneste,
                                   KravVedtakStatusMapper statusMapper,
                                   FagsystemKlient fagsystemKlient) {
        super(behandlingRepository, fagsystemKlient);
        this.økonomiMottattXmlRepository = økonomiMottattXmlRepository;
        this.kravVedtakStatusTjeneste = kravVedtakStatusTjeneste;
        this.statusMapper = statusMapper;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var mottattXmlId = Long.valueOf(prosessTaskData.getPropertyValue(TaskProperty.PROPERTY_MOTTATT_XML_ID));
        var råXml = økonomiMottattXmlRepository.hentMottattXml(mottattXmlId);

        var kravOgVedtakstatus = KravVedtakStatusXmlUnmarshaller.unmarshall(mottattXmlId, råXml);
        var kravVedtakStatus437 = statusMapper.mapTilDomene(kravOgVedtakstatus);
        var saksnummer = FagsystemId.parse(kravOgVedtakstatus.getFagsystemId()).getSaksnummer().getVerdi();
        //TODO k9-tilbake bytt String->Saksnummer
        var henvisning = kravVedtakStatus437.getReferanse();
        validerHenvisning(henvisning);
        økonomiMottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(henvisning, saksnummer, mottattXmlId);

        var åpenTilbakekrevingBehandling = finnÅpenTilbakekrevingBehandling(saksnummer);
        if (åpenTilbakekrevingBehandling.isPresent()) {
            var behandlingId = åpenTilbakekrevingBehandling.get().getId();
            kravVedtakStatusTjeneste.håndteresMottakAvKravVedtakStatus(behandlingId, kravVedtakStatus437);
            økonomiMottattXmlRepository.opprettTilkobling(mottattXmlId);
            LOG.info("Tilkoblet kravVedtakStatus med id={} saksnummer={} behandlingId={}", mottattXmlId, saksnummer, behandlingId);
        } else {
            validerBehandlingsEksistens(henvisning, saksnummer);
            LOG.info("Ignorerte kravVedtakStatus med id={} saksnummer={}. Fantes ikke tilbakekrevingsbehandling", mottattXmlId, saksnummer);
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
