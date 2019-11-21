package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.finn;

import static no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty.ROOT_ELEMENT_KRAV_VEDTAK_STATUS_XML;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty;
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
    private EksternBehandlingRepository eksternBehandlingRepository;
    private ØkonomiMottattXmlRepository mottattXmlRepository;
    private KravVedtakStatusTjeneste kravVedtakStatusTjeneste;
    private KravVedtakStatusMapper kravVedtakStatusMapper;
    private KravgrunnlagMapper kravgrunnlagMapper;

    FinnGrunnlagTask() {
        // for CDI proxy
    }

    @Inject
    public FinnGrunnlagTask(BehandlingRepositoryProvider repositoryProvider,
                            ØkonomiMottattXmlRepository mottattXmlRepository,
                            KravVedtakStatusTjeneste kravVedtakStatusTjeneste,
                            KravVedtakStatusMapper kravVedtakStatusMapper,
                            KravgrunnlagMapper kravgrunnlagMapper) {
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.mottattXmlRepository = mottattXmlRepository;

        this.kravVedtakStatusTjeneste = kravVedtakStatusTjeneste;
        this.kravVedtakStatusMapper = kravVedtakStatusMapper;
        this.kravgrunnlagMapper = kravgrunnlagMapper;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = prosessTaskData.getBehandlingId();
        EksternBehandling eksternBehandling = eksternBehandlingRepository.hentFraInternId(behandlingId);

        List<ØkonomiXmlMottatt> alleXmlMeldinger = mottattXmlRepository.finnAlleForEksternBehandlingId(String.valueOf(eksternBehandling.getEksternId()));
        if (!alleXmlMeldinger.isEmpty()) {
            alleXmlMeldinger = alleXmlMeldinger.stream()
                .sorted(Comparator.comparing(ØkonomiXmlMottatt::getSekvens).reversed())
                .collect(Collectors.toList());
            ØkonomiXmlMottatt sisteMottattXml = alleXmlMeldinger.get(0);
            Long mottattXmlId = sisteMottattXml.getId();
            String mottattXml = sisteMottattXml.getMottattXml();
            logger.info("siste Xml mottatt med mottattXmlId={} for behandlingId={}", mottattXmlId, behandlingId);
            if (mottattXml.contains(TaskProperty.ROOT_ELEMENT_KRAVGRUNNLAG_XML)) {
                kobleGrunnlagMedBehandling(behandlingId, mottattXmlId, mottattXml);
            } else if (mottattXml.contains(ROOT_ELEMENT_KRAV_VEDTAK_STATUS_XML)) {
                håndtereGrunnlagStatusForBehandling(behandlingId, mottattXmlId, mottattXml);
            }
        }
        logger.info("Xml mottatt ikke for behandlingId={}", behandlingId);
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
}
