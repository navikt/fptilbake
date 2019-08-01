package no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.task.FortsettBehandlingTaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingKandidaterRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingVenterRepository;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.ResponsKanal;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.VarselresponsTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.TaskStatus;
import no.nav.vedtak.log.mdc.MDCOperations;

@ApplicationScoped
public class GjenopptaBehandlingTjenesteImpl implements GjenopptaBehandlingTjeneste{

    private static Logger LOGGER = LoggerFactory.getLogger(GjenopptaBehandlingTjenesteImpl.class);

    private ProsessTaskRepository prosessTaskRepository;
    private BehandlingKandidaterRepository behandlingKandidaterRepository;
    private BehandlingVenterRepository behandlingVenterRepository;
    private VarselresponsTjeneste varselresponsTjeneste;

    public GjenopptaBehandlingTjenesteImpl() {
        // CDI
    }

    @Inject
    public GjenopptaBehandlingTjenesteImpl(ProsessTaskRepository prosessTaskRepository,
                                           BehandlingKandidaterRepository behandlingKandidaterRepository,
                                           BehandlingVenterRepository behandlingVenterRepository,
                                           VarselresponsTjeneste varselresponsTjeneste) {
        this.prosessTaskRepository = prosessTaskRepository;
        this.behandlingKandidaterRepository = behandlingKandidaterRepository;
        this.behandlingVenterRepository = behandlingVenterRepository;
        this.varselresponsTjeneste = varselresponsTjeneste;
    }

    @Override
    public String automatiskGjenopptaBehandlinger() {
        List<Behandling> behandlinger = behandlingKandidaterRepository.finnBehandlingerForAutomatiskGjenopptagelse();

        final String callId = hentCallId();

        behandlinger.forEach(behandling -> {
            String nyCallId = callId + behandling.getId();
            opprettFortsettBehandlingTask(behandling, nyCallId);
        });

        return "-";
    }

    @Override
    public Optional<String> fortsettBehandlingManuelt(long behandlingId) {
        Optional<Behandling> behandlingOpt = behandlingVenterRepository.hentBehandlingPåVent(behandlingId);
        behandlingOpt.ifPresent(b -> {
            if (BehandlingStegType.VARSEL.equals(b.getAktivtBehandlingSteg())
                    && Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING.equals(b.getVenteårsak())) {
                varselresponsTjeneste.lagreRespons(behandlingId, ResponsKanal.MANUELL);
            }
        });
        return fortsettBehandling(behandlingId);
    }

    @Override
    public Optional<String> fortsettBehandlingMedGrunnlag(long behandlingId) {
        Optional<Behandling> behandlingOpt = behandlingVenterRepository.hentBehandlingPåVent(behandlingId);
        if (behandlingOpt.isPresent()) {
            BehandlingStegType bst = behandlingOpt.get().getAktivtBehandlingSteg();
            if (BehandlingStegType.TBKGSTEG.equals(bst)) {
                return fortsettBehandling(behandlingId);
            }
        }
        return Optional.empty();
    }

    @Override
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

    @Override
    public List<TaskStatus> hentStatusForGjenopptaBehandlingGruppe(String gruppe) {
        return prosessTaskRepository.finnStatusForTaskIGruppe(GjenopptaBehandlingTask.TASKTYPE, gruppe);
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

        LOGGER.info("oppretter ny prosesstask med callId: {}", callId);
        return prosessTaskRepository.lagre(prosessTaskData);
    }

}
