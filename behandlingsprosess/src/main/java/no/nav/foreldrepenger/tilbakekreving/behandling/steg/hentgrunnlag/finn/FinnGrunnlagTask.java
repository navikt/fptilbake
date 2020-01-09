package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.finn;

import static no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty.ROOT_ELEMENT_KRAVGRUNNLAG_XML;
import static no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty.ROOT_ELEMENT_KRAV_VEDTAK_STATUS_XML;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagXmlUnmarshaller;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status.KravVedtakStatusMapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status.KravVedtakStatusTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.status.KravVedtakStatusXmlUnmarshaller;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
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
    private KravVedtakStatusTjeneste kravVedtakStatusTjeneste;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private KravVedtakStatusMapper kravVedtakStatusMapper;
    private KravgrunnlagMapper kravgrunnlagMapper;

    FinnGrunnlagTask() {
        // for CDI proxy
    }

    @Inject
    public FinnGrunnlagTask(BehandlingRepositoryProvider repositoryProvider,
                            ØkonomiMottattXmlRepository mottattXmlRepository,
                            KravVedtakStatusTjeneste kravVedtakStatusTjeneste,
                            BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                            KravVedtakStatusMapper kravVedtakStatusMapper,
                            KravgrunnlagMapper kravgrunnlagMapper) {
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.mottattXmlRepository = mottattXmlRepository;

        this.kravVedtakStatusTjeneste = kravVedtakStatusTjeneste;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.kravVedtakStatusMapper = kravVedtakStatusMapper;
        this.kravgrunnlagMapper = kravgrunnlagMapper;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = prosessTaskData.getBehandlingId();
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
                    kobleGrunnlagMedBehandling(behandlingId, mottattXmlId, mottattXml);
                } else if (mottattXml.contains(ROOT_ELEMENT_KRAV_VEDTAK_STATUS_XML) && grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)) {
                    logger.info("xml er status xml med mottattXmlId={}", mottattXmlId);
                    håndtereGrunnlagStatusForBehandling(behandlingId, mottattXmlId, mottattXml);
                } else {
                    logger.warn("xml rekkefølge er ikke riktig med mottattXmlId={}", mottattXmlId);
                }
                mottattXmlRepository.opprettTilkobling(mottattXmlId);
            }
            taBehandlingAvVentHvisGrunnlagetIkkeErSperret(behandling);

        } else {
            logger.info("Xml mottatt ikke for behandlingId={}", behandlingId);
        }

    }

    private void håndtereGrunnlagStatusForBehandling(Long behandlingId, Long mottattXmlId, String mottattXml) {
        KravOgVedtakstatus kravOgVedtakstatus = KravVedtakStatusXmlUnmarshaller.unmarshall(mottattXmlId, mottattXml);
        KravVedtakStatus437 kravVedtakStatus437 = kravVedtakStatusMapper.mapTilDomene(kravOgVedtakstatus);
        kravVedtakStatusTjeneste.håndteresMottakAvKravVedtakStatus(behandlingId, kravVedtakStatus437);
    }

    private void kobleGrunnlagMedBehandling(Long behandlingId, Long mottattXmlId, String mottattXml) {
        DetaljertKravgrunnlag kravgrunnlag = KravgrunnlagXmlUnmarshaller.unmarshall(mottattXmlId, mottattXml);
        kravgrunnlag.setKodeStatusKrav(KravStatusKode.NYTT.getKode()); // alltid vurderes som nytt grunnlag for den behandlingen
        Kravgrunnlag431 kravgrunnlag431 = kravgrunnlagMapper.mapTilDomene(kravgrunnlag);
        grunnlagRepository.lagre(behandlingId, kravgrunnlag431);
    }

    private void taBehandlingAvVentHvisGrunnlagetIkkeErSperret(Behandling behandling) {
        Long behandlingId = behandling.getId();
        if (behandling.isBehandlingPåVent() && !grunnlagRepository.erKravgrunnlagSperret(behandlingId)) {
            BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
            behandlingskontrollTjeneste.taBehandlingAvVentSetAlleAutopunktUtført(behandling, kontekst);
        }
    }

}
