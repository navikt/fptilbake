package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.ØkonomiProxyIntegrasjonResponsSammenligner;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.revurdering.HentKravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KodeAksjon;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagValidator;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.tilbakekreving.web.app.util.StringUtils;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.HentKravgrunnlagDetaljDto;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask("kravgrunnlag.korrigert.hent")
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class HentKorrigertKravgrunnlagTask implements ProsessTaskHandler {

    public static final String KRAVGRUNNLAG_ID = "KRAVGRUNNLAG_ID";
    public static final String ANSVARLIG_ENHET_NØS = "8020";  // fast verdi
    public static final String OKO_SAKSBEH_ID = "K231B433";  //fast verdi

    private EksternBehandlingRepository eksternBehandlingRepository;
    private KravgrunnlagRepository kravgrunnlagRepository;
    private BehandlingRepository behandlingRepository;

    private HentKravgrunnlagMapper hentKravgrunnlagMapper;
    private ØkonomiConsumer økonomiConsumer;
    private FagsystemKlient fagsystemKlient;
    private ØkonomiProxyIntegrasjonResponsSammenligner økonomiProxyIntegrasjonSammenligner;

    HentKorrigertKravgrunnlagTask() {
        // for CDI
    }

    @Inject
    public HentKorrigertKravgrunnlagTask(BehandlingRepositoryProvider repositoryProvider,
                                         HentKravgrunnlagMapper hentKravgrunnlagMapper,
                                         ØkonomiConsumer økonomiConsumer,
                                         FagsystemKlient fagsystemKlient,
                                         ØkonomiProxyIntegrasjonResponsSammenligner økonomiProxyIntegrasjonSammenligner) {
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.kravgrunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();

        this.hentKravgrunnlagMapper = hentKravgrunnlagMapper;
        this.økonomiConsumer = økonomiConsumer;
        this.fagsystemKlient = fagsystemKlient;
        this.økonomiProxyIntegrasjonSammenligner = økonomiProxyIntegrasjonSammenligner;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        String kravgrunnlagId = prosessTaskData.getPropertyValue(KRAVGRUNNLAG_ID);
        Kravgrunnlag431 korrigertKravgrunnlag = hentKorrigertKravgrunnlagFraØkonomi(behandlingId, kravgrunnlagId);

        KravgrunnlagValidator.validerGrunnlag(korrigertKravgrunnlag);
        Henvisning henvisning = korrigertKravgrunnlag.getReferanse();
        if (!finnesEksternBehandling(behandlingId, henvisning)) {
            Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
            EksternBehandlingsinfoDto eksternBehandlingsinfoDto = hentEksternBehandlingFraFpsak(behandling, henvisning);
            oppdaterEksternBehandling(behandling, eksternBehandlingsinfoDto);
        }
        kravgrunnlagRepository.lagreOgFiksDuplikateKravgrunnlag(behandlingId, korrigertKravgrunnlag);
    }

    private HentKravgrunnlagDetaljDto forberedHentKravgrunnlagDetailRequest(String kravgrunnlagId, String ansvarligEnhet,
                                                                            String saksbehId) {
        HentKravgrunnlagDetaljDto hentKravgrunnlagDetalj = new HentKravgrunnlagDetaljDto();
        hentKravgrunnlagDetalj.setKodeAksjon(KodeAksjon.HENT_KORRIGERT_KRAVGRUNNLAG.getKode());
        hentKravgrunnlagDetalj.setEnhetAnsvarlig(ansvarligEnhet);
        hentKravgrunnlagDetalj.setKravgrunnlagId(new BigInteger(kravgrunnlagId));
        hentKravgrunnlagDetalj.setSaksbehId(saksbehId);
        return hentKravgrunnlagDetalj;
    }

    private Kravgrunnlag431 hentKorrigertKravgrunnlagFraØkonomi(Long behandlingId, String kravgrunnlagId) {
        HentKravgrunnlagDetaljDto request;
        if (StringUtils.erIkkeTom(kravgrunnlagId)) {
            request = forberedHentKravgrunnlagDetailRequest(kravgrunnlagId, ANSVARLIG_ENHET_NØS, OKO_SAKSBEH_ID);
        } else {
            Kravgrunnlag431 kravgrunnlag431 = kravgrunnlagRepository.finnKravgrunnlag(behandlingId);
            request = forberedHentKravgrunnlagDetailRequest(kravgrunnlag431.getEksternKravgrunnlagId(), kravgrunnlag431.getAnsvarligEnhet(), kravgrunnlag431.getSaksBehId());
        }
        DetaljertKravgrunnlagDto detaljertKravgrunnlagDto = økonomiConsumer.hentKravgrunnlag(behandlingId, request);
        var kravgrunnlag = hentKravgrunnlagMapper.mapTilDomene(detaljertKravgrunnlagDto);

        // Sammenligning
        økonomiProxyIntegrasjonSammenligner.hentKravgrunnlagFraFpwsproxyOgSammenlignFailsafe(behandlingId, request, kravgrunnlag);
        return kravgrunnlag;
    }

    private boolean finnesEksternBehandling(long behandlingId, Henvisning henvisning) {
        return eksternBehandlingRepository.finnesEksternBehandling(behandlingId, henvisning);
    }

    //TODO k9-tilbake flytt til saksbehandlingKlient-er
    private EksternBehandlingsinfoDto hentEksternBehandlingFraFpsak(Behandling behandling, Henvisning henvisning) {
        String saksnummer = behandling.getFagsak().getSaksnummer().getVerdi();
        List<EksternBehandlingsinfoDto> eksternBehandlinger = fagsystemKlient.hentBehandlingForSaksnummer(saksnummer);
        Optional<EksternBehandlingsinfoDto> eksternBehandling = eksternBehandlinger.stream()
                .filter(eksternBehandlingsinfoDto -> eksternBehandlingsinfoDto.getHenvisning().equals(henvisning)).findAny();
        if (eksternBehandling.isEmpty()) {
            throw new TekniskException("FPT-587197",
                    String.format("Hentet et kravgrunnlag fra Økonomi for en behandling som ikke finnes i fpsak. behandlingId=%s. Kravgrunnlaget skulle kanskje til et annet system. Si i fra til Økonomi!", behandling.getId()));
        }
        return eksternBehandling.get();
    }

    private void oppdaterEksternBehandling(Behandling behandling, EksternBehandlingsinfoDto eksternBehandlingsinfoDto) {
        EksternBehandling eksternBehandling = new EksternBehandling(behandling, eksternBehandlingsinfoDto.getHenvisning(), eksternBehandlingsinfoDto.getUuid());
        eksternBehandlingRepository.lagre(eksternBehandling);
    }

}
