package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.revurdering;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.request.HentKravgrunnlagDetaljDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.request.KodeAksjon;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy.KravgrunnlagHenter;
import no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagValidator;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(value = "kravgrunnlag.hent", prioritet = 2)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class HentKravgrunnlagTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(HentKravgrunnlagTask.class);

    private BehandlingRepository behandlingRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private HistorikkinnslagRepository historikkRepositoryTeamAware;

    private KravgrunnlagTjeneste kravgrunnlagTjeneste;
    private FagsystemKlient fagsystemKlient;

    private KravgrunnlagHenter kravgrunnlagHenter;

    HentKravgrunnlagTask() {
        // for CDI proxy
    }

    @Inject
    public HentKravgrunnlagTask(BehandlingRepositoryProvider repositoryProvider,
                                KravgrunnlagTjeneste kravgrunnlagTjeneste,
                                FagsystemKlient fagsystemKlient,
                                KravgrunnlagHenter kravgrunnlagHenter) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.eksternBehandlingRepository = repositoryProvider.getEksternBehandlingRepository();
        this.historikkRepositoryTeamAware = repositoryProvider.getHistorikkinnslagRepository();

        this.kravgrunnlagTjeneste = kravgrunnlagTjeneste;
        this.fagsystemKlient = fagsystemKlient;
        this.kravgrunnlagHenter = kravgrunnlagHenter;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Long origBehandlingId = Long.valueOf(prosessTaskData.getPropertyValue(TaskProperties.PROPERTY_ORIGINAL_BEHANDLING_ID));

        Kravgrunnlag431 kravgrunnlag431 = hentNyttKravgrunnlag(origBehandlingId);

        KravgrunnlagValidator.validerGrunnlag(kravgrunnlag431);

        String saksnummer = kravgrunnlag431.getSaksnummer().getVerdi();
        Henvisning henvisning = kravgrunnlag431.getReferanse();
        kravgrunnlagTjeneste.lagreTilbakekrevingsgrunnlagFraØkonomi(behandlingId, kravgrunnlag431, true);
        oppdaterHenvisningFraGrunnlag(behandling, saksnummer, henvisning);
        lagHistorikkInnslagForMotattKravgrunnlag(behandling, kravgrunnlag431);
    }

    private Kravgrunnlag431 hentNyttKravgrunnlag(Long origBehandlingId) {
        var kravgrunnlag = grunnlagRepository.finnKravgrunnlag(origBehandlingId);
        var hentKravgrunnlagRequest = new HentKravgrunnlagDetaljDto.Builder()
            .kodeAksjon(KodeAksjon.HENT_GRUNNLAG_OMGJØRING)
            .kravgrunnlagId(new BigInteger(kravgrunnlag.getEksternKravgrunnlagId()))
            .enhetAnsvarlig(kravgrunnlag.getAnsvarligEnhet())
            .saksbehId(kravgrunnlag.getSaksBehId())
            .build();
        return kravgrunnlagHenter.hentKravgrunnlagFraOS(origBehandlingId, hentKravgrunnlagRequest);
    }

    private void oppdaterHenvisningFraGrunnlag(Behandling behandling, String saksnummer, Henvisning grunnlagHenvisning) {
        List<EksternBehandlingsinfoDto> eksternBehandlinger = hentBehandlingerFraFagsystem(saksnummer);
        Optional<EksternBehandlingsinfoDto> eksternBehandlingsinfoDto = eksternBehandlinger.stream()
                .filter(eksternBehandling -> grunnlagHenvisning.equals(eksternBehandling.getHenvisning())).findFirst();
        if (eksternBehandlingsinfoDto.isPresent()) {
            LOG.info("Oppdaterer EksternBehandling henvisning={} for behandlingId={}", grunnlagHenvisning, behandling.getId());
            EksternBehandlingsinfoDto eksternBehandlingDto = eksternBehandlingsinfoDto.get();
            EksternBehandling eksternBehandling = new EksternBehandling(behandling, eksternBehandlingDto.getHenvisning(), eksternBehandlingDto.getUuid());
            eksternBehandlingRepository.lagre(eksternBehandling);
        } else {
            throw new TekniskException("FPT-587169",
                    String.format("Hentet et tilbakekrevingsgrunnlag fra Økonomi for en behandling som ikke finnes i Fagsaksystemet for saksnummer=%s, henvisning=%s. Kravgrunnlaget skulle kanskje til et annet system. Si i fra til Økonomi!", saksnummer, grunnlagHenvisning));
        }
    }

    private List<EksternBehandlingsinfoDto> hentBehandlingerFraFagsystem(String saksnummer) {
        return fagsystemKlient.hentBehandlingForSaksnummer(saksnummer);
    }

    private void lagHistorikkInnslagForMotattKravgrunnlag(Behandling behandling, Kravgrunnlag431 kravgrunnlag431) {
        var historikkinnslagMottattKravgrunnlag = new Historikkinnslag.Builder()
            .medAktør(HistorikkAktør.VEDTAKSLØSNINGEN)
            .medFagsakId(behandling.getFagsakId())
            .medBehandlingId(behandling.getId())
            .medTittel("Kravgrunnlag mottatt")
            .addLinje(String.format("ID: %s", kravgrunnlag431.getVedtakId()))
            .addLinje(String.format("Status: %s", kravgrunnlag431.getKravStatusKode().getNavn()))
            .build();
        historikkRepositoryTeamAware.lagre(historikkinnslagMottattKravgrunnlag);
    }

}
