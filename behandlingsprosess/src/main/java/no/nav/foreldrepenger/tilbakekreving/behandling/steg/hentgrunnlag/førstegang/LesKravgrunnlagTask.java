package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTask;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.FagsystemId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagValidator;
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

@ApplicationScoped
@ProsessTask(LesKravgrunnlagTask.TASKTYPE)
public class LesKravgrunnlagTask extends FellesTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(LesKravgrunnlagTask.class);

    public static final String TASKTYPE = "kravgrunnlag.les";

    private ØkonomiMottattXmlRepository økonomiMottattXmlRepository;
    private KravgrunnlagTjeneste kravgrunnlagTjeneste;
    private KravgrunnlagMapper kravgrunnlagMapper;
    private BehandlingRepository behandlingRepository;
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
        super(repositoryProvider.getGrunnlagRepository(), fagsystemKlient);
        this.økonomiMottattXmlRepository = økonomiMottattXmlRepository;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();

        this.kravgrunnlagTjeneste = kravgrunnlagTjeneste;
        this.kravgrunnlagMapper = kravgrunnlagMapper;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long mottattXmlId = Long.valueOf(prosessTaskData.getPropertyValue(TaskProperty.PROPERTY_MOTTATT_XML_ID));

        String råXml = økonomiMottattXmlRepository.hentMottattXml(mottattXmlId);
        DetaljertKravgrunnlag kravgrunnlagDto = KravgrunnlagXmlUnmarshaller.unmarshall(mottattXmlId, råXml);
        Henvisning henvisning = kravgrunnlagMapper.finnHenvisning(kravgrunnlagDto);
        String saksnummer = FagsystemId.parse(kravgrunnlagDto.getFagsystemId()).getSaksnummer().getVerdi();
        //TODO k9-tilbake bytt String->Saksnummer
        Kravgrunnlag431 kravgrunnlag = kravgrunnlagMapper.mapTilDomene(kravgrunnlagDto);

        boolean kravgrunnlagetErGyldig = validerKravgrunnlag(mottattXmlId, henvisning, saksnummer, kravgrunnlag);
        økonomiMottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(henvisning, saksnummer, mottattXmlId);

        Optional<Behandling> åpenTilbakekrevingBehandling = finnÅpenTilbakekrevingBehandling(saksnummer);
        if (åpenTilbakekrevingBehandling.isPresent()) {
            Behandling behandling = åpenTilbakekrevingBehandling.get();
            long behandlingId = behandling.getId();
            logger.info("Leste kravgrunnlag med id={} saksnummer={} internBehandlingId={}", mottattXmlId, saksnummer, behandlingId);
            kravgrunnlagTjeneste.lagreTilbakekrevingsgrunnlagFraØkonomi(behandlingId, kravgrunnlag, kravgrunnlagetErGyldig);
            økonomiMottattXmlRepository.opprettTilkobling(mottattXmlId);
            logger.info("Behandling med internBehandlingId={} koblet med grunnlag id={}", behandlingId, mottattXmlId);
            oppdaterHenvisningFraGrunnlag(behandling, saksnummer, henvisning);
        } else {
            validerBehandlingsEksistens(henvisning, saksnummer);
            logger.info("Ignorerte kravgrunnlag med id={} saksnummer={}. Fantes ikke en åpen tilbakekrevingsbehandling", mottattXmlId, saksnummer);
        }
    }

    private static boolean validerKravgrunnlag(Long mottattXmlId, Henvisning henvisning, String saksnummer, Kravgrunnlag431 kravgrunnlag) {
        validerHenvisning(henvisning);
        try {
            KravgrunnlagValidator.validerGrunnlag(kravgrunnlag);
            return true;
        } catch (KravgrunnlagValidator.UgyldigKravgrunnlagException e) {
            //logger feilen i kravgrunnlaget sammen med metainformasjon slik at feilen kan følges opp
            //prosessen får fortsette, slik at prosessen hopper tilbake hvis den er i fakta-steget eller senere
            LesKravgrunnlagTaskFeil.FACTORY.ugyldigKravgrunnlag(saksnummer, henvisning, mottattXmlId, e).log(logger);
            return false;
        }
    }

    private static void validerHenvisning(Henvisning henvisning){
        if (!Henvisning.erGyldig(henvisning)) {
            throw LesKravgrunnlagTaskFeil.FACTORY.ugyldigHenvisning(henvisning).toException();
        }
    }

    private List<Behandling> hentBehandlingerForSaksnummer(String saksnummer) {
        return behandlingRepository.hentAlleBehandlingerForSaksnummer(new Saksnummer(saksnummer));
    }

    private Optional<Behandling> finnÅpenTilbakekrevingBehandling(String saksnummer){
        List<Behandling> behandlinger = hentBehandlingerForSaksnummer(saksnummer);
        List<Behandling> åpneBehandlinger = behandlinger.stream()
            .filter(beh -> BehandlingType.TILBAKEKREVING.equals(beh.getType()))
            .filter(beh -> !beh.erAvsluttet()).collect(Collectors.toList());
        if(åpneBehandlinger.size() > 1){
            throw new IllegalArgumentException("Utvikler feil: Kan ikke ha flere åpne behandling for saksnummer="+ saksnummer);
        }
        return åpneBehandlinger.stream().findAny();
    }

    private void validerBehandlingsEksistens(Henvisning henvisning, String saksnummer) {
        if (!finnesYtelsesbehandling(saksnummer, henvisning)) {
            throw LesKravgrunnlagTaskFeil.FACTORY.behandlingFinnesIkkeIFagsaksystemet(saksnummer, henvisning).toException();
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
            throw LesKravgrunnlagTaskFeil.FACTORY.behandlingFinnesIkkeIFagsaksystemet(saksnummer, grunnlagHenvisning).toException();
        }
    }

    public interface LesKravgrunnlagTaskFeil extends DeklarerteFeil {

        LesKravgrunnlagTaskFeil FACTORY = FeilFactory.create(LesKravgrunnlagTaskFeil.class);

        @TekniskFeil(feilkode = "FPT-587195",
            feilmelding = "Mottok et tilbakekrevingsgrunnlag fra Økonomi for en behandling som ikke finnes i Fagsaksystemet for saksnummer=%s, henvisning=%s. Kravgrunnlaget skulle kanskje til et annet system. Si i fra til Økonomi!",
            logLevel = LogLevel.WARN)
        Feil behandlingFinnesIkkeIFagsaksystemet(String saksnummer, Henvisning henvisning);

        @TekniskFeil(feilkode = "FPT-675363",
            feilmelding = "Mottok et tilbakekrevingsgrunnlag fra Økonomi med henvisning som ikke er i støttet format. henvisning=%s. Kravgrunnlaget skulle kanskje til et annet system. Si i fra til Økonomi!",
            logLevel = LogLevel.WARN)
        Feil ugyldigHenvisning(Henvisning henvisning);

        @TekniskFeil(feilkode = "FPT-839288",
            feilmelding = "Mottok et ugyldig kravgrunnlag for saksnummer=%s henvisning=%s mottattXmlId=%s",
            logLevel = LogLevel.WARN)
        Feil ugyldigKravgrunnlag(String saksnummer, Henvisning henvisning, Long mottattXmlId, KravgrunnlagValidator.UgyldigKravgrunnlagException cause);

    }

}
