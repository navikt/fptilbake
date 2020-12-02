package no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.task.FortsettBehandlingTaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingKandidaterRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingVenterRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.ResponsKanal;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.VarselresponsTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.TaskStatus;
import no.nav.vedtak.log.mdc.MDCOperations;

@ApplicationScoped
public class GjenopptaBehandlingTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(GjenopptaBehandlingTjeneste.class);

    private ProsessTaskRepository prosessTaskRepository;
    private BehandlingKandidaterRepository behandlingKandidaterRepository;
    private BehandlingVenterRepository behandlingVenterRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private HistorikkRepository historikkRepository;
    private VarselresponsTjeneste varselresponsTjeneste;

    public GjenopptaBehandlingTjeneste() {
        // CDI
    }

    @Inject
    public GjenopptaBehandlingTjeneste(ProsessTaskRepository prosessTaskRepository,
                                       BehandlingKandidaterRepository behandlingKandidaterRepository,
                                       BehandlingVenterRepository behandlingVenterRepository,
                                       BehandlingRepositoryProvider repositoryProvider,
                                       VarselresponsTjeneste varselresponsTjeneste) {
        this.prosessTaskRepository = prosessTaskRepository;
        this.behandlingKandidaterRepository = behandlingKandidaterRepository;
        this.behandlingVenterRepository = behandlingVenterRepository;
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.historikkRepository = repositoryProvider.getHistorikkRepository();
        this.varselresponsTjeneste = varselresponsTjeneste;
    }

    /**
         * Finner behandlinger som står på vent, som kan fortsettes automatisk.
         * @return
         */
    public String automatiskGjenopptaBehandlinger() {
        Set<Behandling> behandlinger = behandlingKandidaterRepository.finnBehandlingerForAutomatiskGjenopptagelse();

        final String callId = hentCallId();

        behandlinger.forEach(behandling -> {
            long behandlingId = behandling.getId();
            String nyCallId = callId + behandlingId;
            if (kanGjenopptaSteg(behandlingId) || !grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)) {
                opprettFortsettBehandlingTask(behandling, nyCallId);
            } else {
                logger.info("Behandling med id={} har et sperret kravgrunnlag, kan ikke gjenopptatt,", behandlingId);
            }
        });

        return "-";
    }

    /**
         * Fortsetter behandling manuelt, registrerer brukerrespons hvis i varsel-steg
         * og venter på brukerrespons.
         * @param behandlingId
         * @return
         */
    public Optional<String> fortsettBehandlingManuelt(long behandlingId, HistorikkAktør historikkAktør) {
        Optional<Behandling> behandlingOpt = behandlingVenterRepository.hentBehandlingPåVent(behandlingId);
        if (behandlingOpt.isPresent()) {
            Behandling behandling = behandlingOpt.get();
            if (BehandlingStegType.VARSEL.equals(behandling.getAktivtBehandlingSteg()) && Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING.equals(behandling.getVenteårsak())) {
                varselresponsTjeneste.lagreRespons(behandlingId, ResponsKanal.MANUELL);
            } else if (!kanGjenopptaSteg(behandlingId) && Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG.equals(behandling.getVenteårsak())) {
                return Optional.empty();
            }
        }
        Optional<String> callId = fortsettBehandling(behandlingId);
        if (callId.isPresent()) {
            opprettHistorikkInnslagForManueltGjenopptaBehandling(behandlingId, historikkAktør);
        }
        return callId;
    }

    /**
         * Fortsetter behandling ved registrering av grunnlag dersom behandling er i TBKGSTEG.
         * @param behandlingId
         * @return
         */
    public Optional<String> fortsettBehandlingMedGrunnlag(long behandlingId) {
        Optional<Behandling> behandlingOpt = behandlingVenterRepository.hentBehandlingPåVent(behandlingId);
        if (behandlingOpt.isPresent()) {
            BehandlingStegType bst = behandlingOpt.get().getAktivtBehandlingSteg();
            if (BehandlingStegType.TBKGSTEG.equals(bst) || BehandlingStegType.FAKTA_FEILUTBETALING.equals(bst)) {
                return fortsettBehandling(behandlingId);
            }
        }
        return Optional.empty();
    }

    /**
         * Fortsetter behandling, oppretter FortsettBehandlingTask
         * @param behandlingId
         * @return prosesstask gruppe ID
         */
    public Optional<String> fortsettBehandling(long behandlingId) {
        Optional<Behandling> behandlingOpt = behandlingVenterRepository.hentBehandlingPåVent(behandlingId);

        if (!behandlingOpt.isPresent()) {
            return Optional.empty();
        }

        final String callId = hentCallId();
        Behandling behandling = behandlingOpt.get();

        String nyCallId = callId + behandling.getId();
        return Optional.ofNullable(opprettFortsettBehandlingTask(behandling, nyCallId));
    }

    /**
         * Henter status for prosesstaskgruppe
         * @param gruppe
         * @return
         */
    public List<TaskStatus> hentStatusForGjenopptaBehandlingGruppe(String gruppe) {
        return prosessTaskRepository.finnStatusForTaskIGruppe(GjenopptaBehandlingTask.TASKTYPE, gruppe);
    }

    /**
         * Sjekk om behandling kan ta av vent
         * @param behandlingId
         * @return
         */
    public boolean kanGjenopptaSteg(long behandlingId) {
        return grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)
            && !grunnlagRepository.erKravgrunnlagSperret(behandlingId)
            && grunnlagRepository.erKravgrunnlagSomForventet(behandlingId);
    }

    private String hentCallId() {
        String cid = MDCOperations.getCallId();
        return (cid == null ? MDCOperations.generateCallId() : cid) + "_";
    }

    private String opprettFortsettBehandlingTask(Behandling behandling, String callId) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(FortsettBehandlingTaskProperties.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setSekvens("1");
        prosessTaskData.setPrioritet(100);
        prosessTaskData.setProperty(FortsettBehandlingTaskProperties.GJENOPPTA_STEG, behandling.getAktivtBehandlingSteg().getKode());

        // unik per task da det gjelder ulike behandlinger, gjenbruker derfor ikke
        prosessTaskData.setCallId(callId);

        logger.info("Gjenopptar behandling av behandlingId={}, oppretter {}-prosesstask med callId={}", behandling.getId(), prosessTaskData.getTaskType(), callId);
        return prosessTaskRepository.lagre(prosessTaskData);
    }

    private void opprettHistorikkInnslagForManueltGjenopptaBehandling(long behandlingId, HistorikkAktør historikkAktør) {
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setAktør(historikkAktør);
        historikkinnslag.setType(HistorikkinnslagType.BEH_MAN_GJEN);
        historikkinnslag.setBehandlingId(behandlingId);

        HistorikkInnslagTekstBuilder builder = new HistorikkInnslagTekstBuilder();
        builder.medHendelse(HistorikkinnslagType.BEH_MAN_GJEN).build(historikkinnslag);
        historikkRepository.lagre(historikkinnslag);
    }

}
