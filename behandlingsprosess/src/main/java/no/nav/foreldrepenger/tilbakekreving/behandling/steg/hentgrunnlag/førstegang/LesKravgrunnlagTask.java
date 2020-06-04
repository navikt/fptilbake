package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.FellesTask;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
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
    private EksternBehandlingRepository eksternBehandlingRepository;
    private BehandlingRepository behandlingRepository;

    LesKravgrunnlagTask() {
        //for CDI proxy
    }

    @Inject
    public LesKravgrunnlagTask(ØkonomiMottattXmlRepository økonomiMottattXmlRepository,
                               KravgrunnlagTjeneste kravgrunnlagTjeneste,
                               KravgrunnlagMapper kravgrunnlagMapper,
                               BehandlingRepositoryProvider repositoryProvider,
                               FpsakKlient fpsakKlient) {
        super(repositoryProvider.getGrunnlagRepository(), fpsakKlient);
        this.økonomiMottattXmlRepository = økonomiMottattXmlRepository;
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();

        this.kravgrunnlagTjeneste = kravgrunnlagTjeneste;
        this.kravgrunnlagMapper = kravgrunnlagMapper;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long mottattXmlId = Long.valueOf(prosessTaskData.getPropertyValue(TaskProperty.PROPERTY_MOTTATT_XML_ID));

        String råXml = økonomiMottattXmlRepository.hentMottattXml(mottattXmlId);
        DetaljertKravgrunnlag kravgrunnlagDto = KravgrunnlagXmlUnmarshaller.unmarshall(mottattXmlId, råXml);
        String henvisning = kravgrunnlagMapper.finnBehandlngId(kravgrunnlagDto);
        String saksnummer = finnSaksnummer(kravgrunnlagDto.getFagsystemId());
        Kravgrunnlag431 kravgrunnlag = kravgrunnlagMapper.mapTilDomene(kravgrunnlagDto);

        boolean kravgrunnlagetErGyldig = validerKravgrunnlag(mottattXmlId, henvisning, saksnummer, kravgrunnlag);
        økonomiMottattXmlRepository.oppdaterMedHenvisningOgSaksnummer(henvisning, saksnummer, mottattXmlId);

        Optional<EksternBehandling> behandlingKobling = hentKoblingTilInternBehandling(henvisning);
        if (behandlingKobling.isPresent()) {
            Long internId = behandlingKobling.get().getInternId();
            logger.info("Leste kravgrunnlag med id={} henvisning={} internBehandlingId={}", mottattXmlId, henvisning, internId);

            Behandling behandling = behandlingRepository.hentBehandling(internId);
            if (!behandling.erAvsluttet()) {
                kravgrunnlagTjeneste.lagreTilbakekrevingsgrunnlagFraØkonomi(internId, kravgrunnlag, kravgrunnlagetErGyldig);
                økonomiMottattXmlRepository.opprettTilkobling(mottattXmlId);
                logger.info("Behandling med internBehandlingId={} koblet med grunnlag id={}", internId, mottattXmlId);
            } else {
                logger.info("Behandling med internBehandlingId={} og henvisning={} er avsluttet, ikke koblet grunnlag med behandling", internId, henvisning);
            }

        } else {
            validerBehandlingsEksistens(henvisning, saksnummer);
            logger.info("Ignorerte kravgrunnlag med id={} henvisning={}. Fantes ikke tilbakekrevingsbehandling", mottattXmlId, henvisning);
        }

    }

    private static boolean validerKravgrunnlag(Long mottattXmlId, String henvisning, String saksnummer, Kravgrunnlag431 kravgrunnlag) {
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

    private Optional<EksternBehandling> hentKoblingTilInternBehandling(String henvisning) {
        //FIXME k9-tilbake må støtte base64(uuid) fra K9
        if (erGyldigTall(henvisning)) {
            long eksternBehandlingId = Long.parseLong(henvisning);
            return eksternBehandlingRepository.hentFraEksternId(eksternBehandlingId);
        }
        return Optional.empty();
    }

    private void validerBehandlingsEksistens(String henvisning, String saksnummer) {
        //FIXME k9-tilbake må støtte base64(uuid) fra K9
        if (!erGyldigTall(henvisning)) {
            throw LesKravgrunnlagTaskFeil.FACTORY.ugyldigHenvisning(henvisning).toException();
        }
        if (!erBehandlingFinnesIFpsak(saksnummer, henvisning)) {
            throw LesKravgrunnlagTaskFeil.FACTORY.behandlingFinnesIkkeIFagsaksystemet(henvisning).toException();
        }
    }

    public interface LesKravgrunnlagTaskFeil extends DeklarerteFeil {

        LesKravgrunnlagTaskFeil FACTORY = FeilFactory.create(LesKravgrunnlagTaskFeil.class);

        //FIXME k9-tilbake kan ikke hardkode fpsak i feilmeldingen
        @TekniskFeil(feilkode = "FPT-587195",
            feilmelding = "Mottok et tilbakekrevingsgrunnlag fra Økonomi for en behandling som ikke finnes i fpsak. henvisning=%s. Kravgrunnlaget skulle kanskje til et annet system. Si i fra til Økonomi!",
            logLevel = LogLevel.WARN)
        Feil behandlingFinnesIkkeIFagsaksystemet(String henvisning);

        //FIXME k9-tilbake må støtte k9-format for henvisning også
        @TekniskFeil(feilkode = "FPT-675363",
            feilmelding = "Mottok et tilbakekrevingsgrunnlag fra Økonomi med henvisning som ikke er et tall. behandlingId=%s. Kravgrunnlaget skulle kanskje til et annet system. Si i fra til Økonomi!",
            logLevel = LogLevel.WARN)
        Feil ugyldigHenvisning(String behandlingId);

        @TekniskFeil(feilkode = "FPT-839288",
            feilmelding = "Mottok et ugyldig kravgrunnlag for saksnummer=%s henvisning=%s mottattXmlId=%s",
            logLevel = LogLevel.WARN)
        Feil ugyldigKravgrunnlag(String saksnummer, String henvisning, Long mottattXmlId, KravgrunnlagValidator.UgyldigKravgrunnlagException cause);
    }

}
