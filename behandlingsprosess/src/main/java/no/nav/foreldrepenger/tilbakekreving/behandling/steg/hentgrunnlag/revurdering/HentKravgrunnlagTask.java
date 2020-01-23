package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.revurdering;

import java.math.BigInteger;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkOpplysningType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KodeAksjon;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagValidator;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ØkonomiConsumer;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlagDto;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.HentKravgrunnlagDetaljDto;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(HentKravgrunnlagTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class HentKravgrunnlagTask implements ProsessTaskHandler {

    private BehandlingRepositoryProvider repositoryProvider;
    private KravgrunnlagRepository grunnlagRepository;

    private KravgrunnlagTjeneste kravgrunnlagTjeneste;
    private HentKravgrunnlagMapper kravgrunnlagMapper;

    private ØkonomiConsumer økonomiConsumer;

    public static final String TASKTYPE = "kravgrunnlag.hent";

    HentKravgrunnlagTask() {
        // for CDI proxy
    }

    @Inject
    public HentKravgrunnlagTask(BehandlingRepositoryProvider repositoryProvider, KravgrunnlagTjeneste kravgrunnlagTjeneste,
                                HentKravgrunnlagMapper kravgrunnlagMapper, ØkonomiConsumer økonomiConsumer) {
        this.repositoryProvider = repositoryProvider;
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();

        this.kravgrunnlagTjeneste = kravgrunnlagTjeneste;
        this.kravgrunnlagMapper = kravgrunnlagMapper;
        this.økonomiConsumer = økonomiConsumer;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = prosessTaskData.getBehandlingId();
        Behandling behandling = repositoryProvider.getBehandlingRepository().hentBehandling(behandlingId);
        Long origBehandlingId = Long.valueOf(prosessTaskData.getPropertyValue(TaskProperty.PROPERTY_ORIGINAL_BEHANDLING_ID));

        Kravgrunnlag431 kravgrunnlag431 = hentNyttKravgrunnlag(origBehandlingId);
        Optional<Feil> valideringsfeil = KravgrunnlagValidator.validerGrunnlag(kravgrunnlag431);
        if (valideringsfeil.isPresent()) {
            throw valideringsfeil.get().toException();
        }

        kravgrunnlagTjeneste.lagreTilbakekrevingsgrunnlagFraØkonomi(behandlingId, kravgrunnlag431);
        lagHistorikkInnslagForMotattKravgrunnlag(behandling, kravgrunnlag431);
    }

    private Kravgrunnlag431 hentNyttKravgrunnlag(Long origBehandlingId) {
        Kravgrunnlag431 kravgrunnlag = grunnlagRepository.finnKravgrunnlag(origBehandlingId);
        DetaljertKravgrunnlagDto grunnlag = hentNyttKravgrunnlagFraØkonomi(origBehandlingId, kravgrunnlag);
        return kravgrunnlagMapper.mapTilDomene(grunnlag);
    }

    private DetaljertKravgrunnlagDto hentNyttKravgrunnlagFraØkonomi(Long origBehandlingId, Kravgrunnlag431 kravgrunnlag431) {
        HentKravgrunnlagDetaljDto hentKravgrunnlagDetalj = forberedeHentKravgrunnlagDetailRequest(kravgrunnlag431);
        return økonomiConsumer.hentKravgrunnlag(origBehandlingId, hentKravgrunnlagDetalj);
    }

    private HentKravgrunnlagDetaljDto forberedeHentKravgrunnlagDetailRequest(Kravgrunnlag431 kravgrunnlag431) {
        HentKravgrunnlagDetaljDto hentKravgrunnlagDetalj = new HentKravgrunnlagDetaljDto();
        hentKravgrunnlagDetalj.setKodeAksjon(KodeAksjon.HENT_GRUNNLAG_OMGJØRING.getKode());
        hentKravgrunnlagDetalj.setEnhetAnsvarlig(kravgrunnlag431.getAnsvarligEnhet());
        hentKravgrunnlagDetalj.setKravgrunnlagId(new BigInteger(kravgrunnlag431.getEksternKravgrunnlagId()));
        hentKravgrunnlagDetalj.setSaksbehId(kravgrunnlag431.getSaksBehId());
        return hentKravgrunnlagDetalj;
    }

    private void lagHistorikkInnslagForMotattKravgrunnlag(Behandling behandling, Kravgrunnlag431 kravgrunnlag431) {
        Historikkinnslag grunnlagMottattInnslag = new Historikkinnslag();
        grunnlagMottattInnslag.setBehandling(behandling);
        grunnlagMottattInnslag.setType(HistorikkinnslagType.NY_KRAVGRUNNLAG_MOTTAT);
        grunnlagMottattInnslag.setAktør(HistorikkAktør.VEDTAKSLØSNINGEN);

        KravStatusKode grunnlagStatus = repositoryProvider.getKodeverkRepository().finn(KravStatusKode.class, kravgrunnlag431.getKravStatusKode());
        HistorikkInnslagTekstBuilder historiebygger = new HistorikkInnslagTekstBuilder()
            .medOpplysning(HistorikkOpplysningType.KRAVGRUNNLAG_VEDTAK_ID, kravgrunnlag431.getVedtakId())
            .medOpplysning(HistorikkOpplysningType.KRAVGRUNNLAG_STATUS, grunnlagStatus.getNavn());
        historiebygger.build(grunnlagMottattInnslag);

        repositoryProvider.getHistorikkRepository().lagre(grunnlagMottattInnslag);
    }
}
