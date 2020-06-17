package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.finn;

import static no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty.ROOT_ELEMENT_KRAVGRUNNLAG_XML;
import static no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty.ROOT_ELEMENT_KRAV_VEDTAK_STATUS_XML;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagXmlUnmarshaller;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status.KravVedtakStatusMapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status.KravVedtakStatusTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status.KravVedtakStatusXmlUnmarshaller;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkTabellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravVedtakStatus437;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlag;
import no.nav.tilbakekreving.status.v1.KravOgVedtakstatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(FinnGrunnlagTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class FinnGrunnlagTask implements ProsessTaskHandler {

    private static final Logger logger = LoggerFactory.getLogger(FinnGrunnlagTask.class);
    public static final String TASKTYPE = "kravgrunnlag.finn";

    private KravgrunnlagRepository grunnlagRepository;
    private BehandlingRepository behandlingRepository;
    private ØkonomiMottattXmlRepository mottattXmlRepository;
    private KodeverkTabellRepository kodeverkTabellRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private KravVedtakStatusTjeneste kravVedtakStatusTjeneste;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private KravVedtakStatusMapper kravVedtakStatusMapper;
    private KravgrunnlagMapper kravgrunnlagMapper;
    private FagsystemKlient fagsystemKlient;

    FinnGrunnlagTask() {
        // for CDI proxy
    }

    @Inject
    public FinnGrunnlagTask(BehandlingRepositoryProvider repositoryProvider,
                            ØkonomiMottattXmlRepository mottattXmlRepository,
                            KodeverkTabellRepository kodeverkTabellRepository,
                            KravVedtakStatusTjeneste kravVedtakStatusTjeneste,
                            BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                            KravVedtakStatusMapper kravVedtakStatusMapper,
                            KravgrunnlagMapper kravgrunnlagMapper,
                            FagsystemKlient fagsystemKlient) {
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.mottattXmlRepository = mottattXmlRepository;
        this.kodeverkTabellRepository = kodeverkTabellRepository;
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();

        this.kravVedtakStatusTjeneste = kravVedtakStatusTjeneste;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.kravVedtakStatusMapper = kravVedtakStatusMapper;
        this.kravgrunnlagMapper = kravgrunnlagMapper;
        this.fagsystemKlient = fagsystemKlient;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        String saksnummer = behandling.getFagsak().getSaksnummer().getVerdi();

        List<ØkonomiXmlMottatt> alleXmlMeldinger = mottattXmlRepository.finnAlleForSaksnummerSomIkkeErKoblet(saksnummer);
        if (!alleXmlMeldinger.isEmpty()) {
            logger.info("Fant {} meldinger som ikke er koblet for behandlingId={} og saksnummer={}", alleXmlMeldinger.size(), behandlingId, saksnummer);
            alleXmlMeldinger = alleXmlMeldinger.stream()
                .sorted(Comparator.comparing(ØkonomiXmlMottatt::getOpprettetTidspunkt))
                .collect(Collectors.toList());
            for (ØkonomiXmlMottatt økonomiXmlMottatt : alleXmlMeldinger) {
                Long mottattXmlId = økonomiXmlMottatt.getId();
                String mottattXml = økonomiXmlMottatt.getMottattXml();

                if (mottattXml.contains(ROOT_ELEMENT_KRAVGRUNNLAG_XML)) {
                    logger.info("xml er grunnlag xml med mottattXmlId={}", mottattXmlId);
                    kobleGrunnlagMedBehandling(behandling, mottattXmlId, mottattXml);
                } else if (mottattXml.contains(ROOT_ELEMENT_KRAV_VEDTAK_STATUS_XML) && grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)) {
                    logger.info("xml er status xml med mottattXmlId={}", mottattXmlId);
                    håndtereGrunnlagStatusForBehandling(behandlingId, mottattXmlId, mottattXml);
                } else {
                    logger.warn("xml rekkefølge er ikke riktig med mottattXmlId={}", mottattXmlId);
                }
                mottattXmlRepository.opprettTilkobling(mottattXmlId);
            }
            taBehandlingAvVentOgProssereHvisGrunnlagetIkkeErSperret(behandling);

        } else {
            logger.info("Xml mottatt ikke for behandlingId={}", behandlingId);
        }

    }

    private void håndtereGrunnlagStatusForBehandling(Long behandlingId, Long mottattXmlId, String mottattXml) {
        KravOgVedtakstatus kravOgVedtakstatus = KravVedtakStatusXmlUnmarshaller.unmarshall(mottattXmlId, mottattXml);
        KravVedtakStatus437 kravVedtakStatus437 = kravVedtakStatusMapper.mapTilDomene(kravOgVedtakstatus);
        kravVedtakStatusTjeneste.håndteresMottakAvKravVedtakStatus(behandlingId, kravVedtakStatus437);
    }

    private void kobleGrunnlagMedBehandling(Behandling behandling, Long mottattXmlId, String mottattXml) {
        DetaljertKravgrunnlag kravgrunnlag = KravgrunnlagXmlUnmarshaller.unmarshall(mottattXmlId, mottattXml);
        kravgrunnlag.setKodeStatusKrav(KravStatusKode.NYTT.getKode()); // alltid vurderes som nytt grunnlag for den behandlingen
        Kravgrunnlag431 kravgrunnlag431 = kravgrunnlagMapper.mapTilDomene(kravgrunnlag);
        grunnlagRepository.lagre(behandling.getId(), kravgrunnlag431);

        Henvisning grunnlagReferanse = kravgrunnlag431.getReferanse();
        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandling.getId());
        if (!erReferanseRiktig(grunnlagReferanse, eksternBehandling)) {
            logger.info("Tilkoblet grunnlag har en annen referanse={} enn behandling for behandlingId={}", grunnlagReferanse, behandling.getId());
            oppdatereEksternBehandlingMedRiktigReferanse(behandling, grunnlagReferanse);
        }
    }

    private void taBehandlingAvVentOgProssereHvisGrunnlagetIkkeErSperret(Behandling behandling) {
        Long behandlingId = behandling.getId();
        if (behandling.isBehandlingPåVent() && !grunnlagRepository.erKravgrunnlagSperret(behandlingId)) {
            BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
            behandlingskontrollTjeneste.taBehandlingAvVentSetAlleAutopunktUtført(behandling, kontekst);
            behandlingskontrollTjeneste.prosesserBehandlingGjenopptaHvisStegVenter(kontekst, finnBehandlingStegType(behandling.getAktivtBehandlingSteg().getKode()));
        }
    }

    private BehandlingStegType finnBehandlingStegType(String gjenoppta) {
        BehandlingStegType stegtype = kodeverkTabellRepository.finnBehandlingStegType(gjenoppta);
        if (stegtype == null) {
            throw new IllegalStateException("Utviklerfeil: ukjent steg " + gjenoppta);
        }
        return stegtype;
    }

    private boolean erReferanseRiktig(Henvisning grunnlagReferanse, EksternBehandling eksternBehandling) {
        return grunnlagReferanse.equals(eksternBehandling.getHenvisning());
    }

    private void oppdatereEksternBehandlingMedRiktigReferanse(Behandling behandling, Henvisning grunnlagReferanse) {
        String saksnummer = behandling.getFagsak().getSaksnummer().getVerdi();
        List<EksternBehandlingsinfoDto> eksternBehandlinger = fagsystemKlient.hentBehandlingForSaksnummer(saksnummer);
        if (!eksternBehandlinger.isEmpty()) {
            Optional<EksternBehandlingsinfoDto> eksternBehandlingsinfoDto = eksternBehandlinger.stream()
                .filter(eksternBehandling -> grunnlagReferanse.equals(eksternBehandling.getHenvisning())).findFirst();
            if (eksternBehandlingsinfoDto.isPresent()) {
                logger.info("Oppdaterer ekstern behandling referanse med referanse={} for behandlingId={}", grunnlagReferanse, behandling.getId());
                EksternBehandlingsinfoDto eksternBehandlingDto = eksternBehandlingsinfoDto.get();
                EksternBehandling eksternBehandling = new EksternBehandling(behandling, eksternBehandlingDto.getHenvisning(), eksternBehandlingDto.getUuid());
                eksternBehandlingRepository.lagre(eksternBehandling);
            } else {
                throw FinnGrunnlagTaskFeil.FACTORY.grunnlagHarFeilReferanse(behandling.getId(), saksnummer).toException();
            }
        }
    }

}
