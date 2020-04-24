package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.revurdering.HentKravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KodeAksjon;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagValidator;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.foreldrepenger.tilbakekreving.web.app.util.StringUtils;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.HentKravgrunnlagDetaljDto;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(HentKorrigertKravgrunnlagTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class HentKorrigertKravgrunnlagTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "kravgrunnlag.korrigert.hent";
    public static final String KRAVGRUNNLAG_ID = "KRAVGRUNNLAG_ID";
    public static final String ANSVARLIG_ENHET_NØS ="8020";  // fast verdi
    public static final String OKO_SAKSBEH_ID="K231B433";  //fast verdi

    private EksternBehandlingRepository eksternBehandlingRepository;
    private KravgrunnlagRepository kravgrunnlagRepository;
    private BehandlingRepository behandlingRepository;

    private HentKravgrunnlagMapper hentKravgrunnlagMapper;
    private ØkonomiConsumer økonomiConsumer;
    private FpsakKlient fpsakKlient;

    HentKorrigertKravgrunnlagTask() {
        // for CDI
    }

    @Inject
    public HentKorrigertKravgrunnlagTask(BehandlingRepositoryProvider repositoryProvider,
                                         HentKravgrunnlagMapper hentKravgrunnlagMapper,
                                         ØkonomiConsumer økonomiConsumer,
                                         FpsakKlient fpsakKlient) {
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.kravgrunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();

        this.hentKravgrunnlagMapper = hentKravgrunnlagMapper;
        this.økonomiConsumer = økonomiConsumer;
        this.fpsakKlient = fpsakKlient;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        String kravgrunnlagId = prosessTaskData.getPropertyValue(KRAVGRUNNLAG_ID);
        DetaljertKravgrunnlagDto respons = hentKorrigertKravgrunnlagFraØkonomi(behandlingId, kravgrunnlagId);
        Kravgrunnlag431 korrigertKravgrunnlag = hentKravgrunnlagMapper.mapTilDomene(respons);

        KravgrunnlagValidator.validerGrunnlag(korrigertKravgrunnlag);
        long eksternBehandlingId = Long.parseLong(korrigertKravgrunnlag.getReferanse());
        if(!finnesEksternBehandling(behandlingId,eksternBehandlingId)){
            Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
            EksternBehandlingsinfoDto eksternBehandlingsinfoDto = hentEksternBehandlingFraFpsak(behandling,eksternBehandlingId);
            oppdaterEksternBehandling(behandling, eksternBehandlingsinfoDto);
        }
        kravgrunnlagRepository.lagre(behandlingId,korrigertKravgrunnlag);
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

    private DetaljertKravgrunnlagDto hentKorrigertKravgrunnlagFraØkonomi(Long behandlingId, String kravgrunnlagId) {
        HentKravgrunnlagDetaljDto request;
        if (StringUtils.erIkkeTom(kravgrunnlagId)) {
            request = forberedHentKravgrunnlagDetailRequest(kravgrunnlagId, ANSVARLIG_ENHET_NØS, OKO_SAKSBEH_ID);
        } else {
            Kravgrunnlag431 kravgrunnlag431 = kravgrunnlagRepository.finnKravgrunnlag(behandlingId);
            request = forberedHentKravgrunnlagDetailRequest(kravgrunnlag431.getEksternKravgrunnlagId(), kravgrunnlag431.getAnsvarligEnhet(), kravgrunnlag431.getSaksBehId());
        }
        return økonomiConsumer.hentKravgrunnlag(behandlingId, request);
    }

    private boolean finnesEksternBehandling(long behandlingId, long eksternBehandlingId){
        return eksternBehandlingRepository.finnesEksternBehandling(behandlingId,eksternBehandlingId);
    }

    private EksternBehandlingsinfoDto hentEksternBehandlingFraFpsak(Behandling behandling, long eksternBehandlingId){
        String saksnummer = behandling.getFagsak().getSaksnummer().getVerdi();
        List<EksternBehandlingsinfoDto> eksternBehandlinger = fpsakKlient.hentBehandlingForSaksnummer(saksnummer);
        Optional<EksternBehandlingsinfoDto> eksternBehandling =  eksternBehandlinger.stream()
            .filter(eksternBehandlingsinfoDto ->  eksternBehandlingsinfoDto.getId().equals(eksternBehandlingId)).findAny();
        if(eksternBehandling.isEmpty()){
            throw HentKorrigertGrunnlagTaskFeil.FACTORY.behandlingFinnesIkkeIFpsak(behandling.getId()).toException();
        }
        return eksternBehandling.get();
    }

    private void oppdaterEksternBehandling(Behandling behandling, EksternBehandlingsinfoDto eksternBehandlingsinfoDto) {
        EksternBehandling eksternBehandling = new EksternBehandling(behandling,eksternBehandlingsinfoDto.getId(),eksternBehandlingsinfoDto.getUuid());
        eksternBehandlingRepository.lagre(eksternBehandling);
    }


    public interface HentKorrigertGrunnlagTaskFeil extends DeklarerteFeil {

        HentKorrigertGrunnlagTaskFeil FACTORY = FeilFactory.create(HentKorrigertGrunnlagTaskFeil.class);

        @TekniskFeil(feilkode = "FPT-587197",
            feilmelding = "Hentet et kravgrunnlag fra Økonomi for en behandling som ikke finnes i fpsak. behandlingId=%s. Kravgrunnlaget skulle kanskje til et annet system. Si i fra til Økonomi!",
            logLevel = LogLevel.WARN)
        Feil behandlingFinnesIkkeIFpsak(Long behandlingId);
    }

}
