package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravVedtakStatusTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.henleggelse.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTask;
import no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.FagsystemId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;

@ApplicationScoped
@ProsessTask(value = "kravvedtakstatus.les", prioritet = 2)
public class LesKravvedtakStatusTask extends FellesTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(LesKravvedtakStatusTask.class);
    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess");

    private ØkonomiMottattXmlRepository økonomiMottattXmlRepository;
    private KravVedtakStatusTjeneste kravVedtakStatusTjeneste;
    private KravVedtakStatusMapper statusMapper;
    private HenleggBehandlingTjeneste henleggBehandlingTjeneste;


    LesKravvedtakStatusTask() {
        // for CDI proxy
    }

    @Inject
    public LesKravvedtakStatusTask(ØkonomiMottattXmlRepository økonomiMottattXmlRepository,
                                   BehandlingRepository behandlingRepository,
                                   KravVedtakStatusTjeneste kravVedtakStatusTjeneste,
                                   KravVedtakStatusMapper statusMapper,
                                   FagsystemKlient fagsystemKlient,
                                   HenleggBehandlingTjeneste henleggBehandlingTjeneste) {
        super(behandlingRepository, fagsystemKlient);
        this.økonomiMottattXmlRepository = økonomiMottattXmlRepository;
        this.kravVedtakStatusTjeneste = kravVedtakStatusTjeneste;
        this.statusMapper = statusMapper;
        this.henleggBehandlingTjeneste = henleggBehandlingTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var mottattXmlId = Long.valueOf(prosessTaskData.getPropertyValue(TaskProperties.PROPERTY_MOTTATT_XML_ID));
        LOG_CONTEXT.add("mottattXmlId", mottattXmlId);
        var råXml = økonomiMottattXmlRepository.hentMottattXml(mottattXmlId);

        var kravOgVedtakstatus = KravVedtakStatusXmlUnmarshaller.unmarshall(mottattXmlId, råXml);
        var kravVedtakStatus437 = statusMapper.mapTilDomene(kravOgVedtakstatus);
        //TODO k9-tilbake bytt String->Saksnummer
        var saksnummer = FagsystemId.parse(kravOgVedtakstatus.getFagsystemId()).getSaksnummer().getVerdi();
        LOG_CONTEXT.add("saksnummer", saksnummer);

        var henvisning = kravVedtakStatus437.getReferanse();
        validerHenvisning(henvisning);
        LOG_CONTEXT.add("henvisning", henvisning);
        økonomiMottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(henvisning, saksnummer, mottattXmlId);

        var åpenTilbakekrevingBehandling = finnÅpenTilbakekrevingBehandling(saksnummer);
        if (åpenTilbakekrevingBehandling.isPresent()) {
            var behandlingId = åpenTilbakekrevingBehandling.get().getId();
            LOG_CONTEXT.add("behandling", behandlingId);
            kravVedtakStatusTjeneste.håndteresMottakAvKravVedtakStatus(behandlingId, kravVedtakStatus437);
            if (KravStatusKode.AVSLUTTET.equals(kravVedtakStatus437.getKravStatusKode())) {
                henleggBehandlingTjeneste.henleggBehandling(behandlingId, BehandlingResultatType.HENLAGT_KRAVGRUNNLAG_NULLSTILT);
            }
            økonomiMottattXmlRepository.opprettTilkobling(mottattXmlId);
            LOG.info("Koblet statusmelding til behandling {}", behandlingId);
        } else {
            validerBehandlingsEksistens(henvisning, saksnummer);
            LOG.info("Ignorerte statusmelding for sak {}. Fantes ikke tilbakekrevingsbehandling", saksnummer);
        }
    }

    private void validerHenvisning(Henvisning henvisning) {
        if (!Henvisning.erGyldig(henvisning)) {
            throw new TekniskException("FPT-675364",
                    String.format("Mottok en statusmelding fra Økonomi med henvisning i ikke-støttet format, henvisning=%s. Statusmeldingen skulle kanskje til et annet system. Si i fra til Økonomi!",
                            henvisning));
        }
    }

    private void validerBehandlingsEksistens(Henvisning henvisning, String saksnummer) {
        if (!finnesYtelsesbehandling(saksnummer, henvisning)) {
            throw new TekniskException("FPT-587196",
                    String.format("Mottok en statusmelding fra Økonomi for en behandling som ikke finnes i k9/fpsak. henvisning=%s. Statusmeldingen skulle kanskje til et annet system. Si i fra til Økonomi!", henvisning));
        }
    }
}
