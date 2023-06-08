package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTask;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.FagsystemId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagValidator;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlag;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;

@ApplicationScoped
@ProsessTask("kravgrunnlag.les")
public class LesKravgrunnlagTask extends FellesTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(LesKravgrunnlagTask.class);
    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess");

    private ØkonomiMottattXmlRepository økonomiMottattXmlRepository;
    private KravgrunnlagTjeneste kravgrunnlagTjeneste;
    private KravgrunnlagMapper kravgrunnlagMapper;
    private EksternBehandlingRepository eksternBehandlingRepository;

    LesKravgrunnlagTask() {
        //for CDI proxy
    }

    @Inject
    public LesKravgrunnlagTask(ØkonomiMottattXmlRepository økonomiMottattXmlRepository,
                               KravgrunnlagTjeneste kravgrunnlagTjeneste,
                               KravgrunnlagMapper kravgrunnlagMapper,
                               BehandlingRepositoryProvider repositoryProvider,
                               FagsystemKlient fagsystemKlient) {
        super(repositoryProvider.getBehandlingRepository(), fagsystemKlient);
        this.økonomiMottattXmlRepository = økonomiMottattXmlRepository;
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();

        this.kravgrunnlagTjeneste = kravgrunnlagTjeneste;
        this.kravgrunnlagMapper = kravgrunnlagMapper;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long mottattXmlId = Long.valueOf(prosessTaskData.getPropertyValue(TaskProperty.PROPERTY_MOTTATT_XML_ID));
        LOG_CONTEXT.add("mottattXmlId", mottattXmlId);

        String råXml = økonomiMottattXmlRepository.hentMottattXml(mottattXmlId);
        DetaljertKravgrunnlag kravgrunnlagDto = KravgrunnlagXmlUnmarshaller.unmarshall(mottattXmlId, råXml);
        Henvisning henvisning = kravgrunnlagMapper.finnHenvisning(kravgrunnlagDto);
        String saksnummer = FagsystemId.parse(kravgrunnlagDto.getFagsystemId()).getSaksnummer().getVerdi();
        LOG_CONTEXT.add("henvisning", henvisning.getVerdi());
        LOG_CONTEXT.add("saksnummer", saksnummer);
        //TODO k9-tilbake bytt String->Saksnummer
        Kravgrunnlag431 kravgrunnlag = kravgrunnlagMapper.mapTilDomene(kravgrunnlagDto);
        LOG_CONTEXT.add("kravgrunnlagId", kravgrunnlag.getEksternKravgrunnlagId());

        boolean kravgrunnlagetErGyldig = validerKravgrunnlag(henvisning, saksnummer, kravgrunnlag);
        økonomiMottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(henvisning, saksnummer, mottattXmlId);

        Optional<Behandling> åpenTilbakekrevingBehandling = finnÅpenTilbakekrevingBehandling(saksnummer);
        String kravgrunnlagId = kravgrunnlag.getEksternKravgrunnlagId();
        if (åpenTilbakekrevingBehandling.isPresent()) {
            Behandling behandling = åpenTilbakekrevingBehandling.get();
            long behandlingId = behandling.getId();
            LOG_CONTEXT.add("behandling", behandlingId);
            logger.info("Leste kravgrunnlag {}", kravgrunnlagId);
            kravgrunnlagTjeneste.lagreTilbakekrevingsgrunnlagFraØkonomi(behandlingId, kravgrunnlag, kravgrunnlagetErGyldig);
            økonomiMottattXmlRepository.opprettTilkobling(mottattXmlId);
            logger.info("Behandling {} koblet med kravgrunnlag {}", behandlingId, kravgrunnlagId);
            oppdaterHenvisningFraGrunnlag(behandling, saksnummer, henvisning);
        } else {
            validerBehandlingsEksistens(henvisning, saksnummer);
            logger.info("Ignorerte kravgrunnlag {} for sak {}. Fantes ikke en åpen tilbakekrevingsbehandling", kravgrunnlagId, saksnummer);
        }
    }

    private static boolean validerKravgrunnlag(Henvisning henvisning, String saksnummer, Kravgrunnlag431 kravgrunnlag) {
        validerHenvisning(henvisning);
        try {
            KravgrunnlagValidator.validerGrunnlag(kravgrunnlag);
            return true;
        } catch (KravgrunnlagValidator.UgyldigKravgrunnlagException e) {
            //logger feilen i kravgrunnlaget sammen med metainformasjon slik at feilen kan følges opp
            //prosessen får fortsette, slik at prosessen hopper tilbake hvis den er i fakta-steget eller senere
            logger.warn(String.format("FPT-839288: Mottok et ugyldig kravgrunnlag for sak %s, kravgrunnlagId er %s", saksnummer, kravgrunnlag.getEksternKravgrunnlagId()), e);
            return false;
        }
    }

    private static void validerHenvisning(Henvisning henvisning) {
        if (!Henvisning.erGyldig(henvisning)) {
            throw LesKravgrunnlagTaskFeil.ugyldigHenvisning(henvisning);
        }
    }

    private void validerBehandlingsEksistens(Henvisning henvisning, String saksnummer) {
        if (!finnesYtelsesbehandling(saksnummer, henvisning)) {
            throw LesKravgrunnlagTaskFeil.behandlingFinnesIkkeIFagsaksystemet(saksnummer, henvisning);
        }
    }

    private void oppdaterHenvisningFraGrunnlag(Behandling behandling, String saksnummer, Henvisning grunnlagHenvisning) {
        List<EksternBehandlingsinfoDto> eksternBehandlinger = hentBehandlingerFraFagsystem(saksnummer);
        Optional<EksternBehandlingsinfoDto> eksternBehandlingsinfoDto = eksternBehandlinger.stream()
                .filter(eksternBehandling -> grunnlagHenvisning.equals(eksternBehandling.getHenvisning())).findFirst();
        if (eksternBehandlingsinfoDto.isPresent()) {
            logger.info("Oppdaterer EksternBehandling henvisning={} for behandlingId={}", grunnlagHenvisning, behandling.getId());
            EksternBehandlingsinfoDto eksternBehandlingDto = eksternBehandlingsinfoDto.get();
            EksternBehandling eksternBehandling = new EksternBehandling(behandling, eksternBehandlingDto.getHenvisning(), eksternBehandlingDto.getUuid());
            eksternBehandlingRepository.lagre(eksternBehandling);
        } else {
            throw LesKravgrunnlagTaskFeil.behandlingFinnesIkkeIFagsaksystemet(saksnummer, grunnlagHenvisning);
        }
    }

    private static class LesKravgrunnlagTaskFeil {

        static TekniskException behandlingFinnesIkkeIFagsaksystemet(String saksnummer, Henvisning henvisning) {
            return new TekniskException("FPT-587195", String.format("Mottok et tilbakekrevingsgrunnlag fra Økonomi for en behandling som ikke finnes i Fagsaksystemet for saksnummer=%s, henvisning=%s. Kravgrunnlaget skulle kanskje til et annet system. Si i fra til Økonomi!", saksnummer, henvisning));
        }

        static TekniskException ugyldigHenvisning(Henvisning henvisning) {
            return new TekniskException("FPT-675363", String.format("Mottok et tilbakekrevingsgrunnlag fra Økonomi med henvisning som ikke er i støttet format. henvisning=%s. Kravgrunnlaget skulle kanskje til et annet system. Si i fra til Økonomi!", henvisning));
        }
    }

}
