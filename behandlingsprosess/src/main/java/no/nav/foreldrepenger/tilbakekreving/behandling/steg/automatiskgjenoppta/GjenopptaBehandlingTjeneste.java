package no.nav.foreldrepenger.tilbakekreving.behandling.steg.automatiskgjenoppta;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.steg.henleggelse.HenleggBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.task.FortsettBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingKandidaterRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingVenterRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepositoryOld;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagOld;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.log.mdc.MDCOperations;

@ApplicationScoped
public class GjenopptaBehandlingTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(GjenopptaBehandlingTjeneste.class);

    private ProsessTaskTjeneste taskTjeneste;
    private BehandlingKandidaterRepository behandlingKandidaterRepository;
    private BehandlingVenterRepository behandlingVenterRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private HistorikkRepositoryOld historikkRepository;
    private BehandlingRepository behandlingRepository;

    public GjenopptaBehandlingTjeneste() {
        // CDI
    }

    @Inject
    public GjenopptaBehandlingTjeneste(ProsessTaskTjeneste taskTjeneste,
                                       BehandlingKandidaterRepository behandlingKandidaterRepository,
                                       BehandlingVenterRepository behandlingVenterRepository,
                                       BehandlingRepositoryProvider repositoryProvider) {
        this.taskTjeneste = taskTjeneste;
        this.behandlingKandidaterRepository = behandlingKandidaterRepository;
        this.behandlingVenterRepository = behandlingVenterRepository;
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.historikkRepository = repositoryProvider.getHistorikkRepositoryOld();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
    }

    /**
     * Finner behandlinger som står på vent, som kan fortsettes automatisk.
     *
     * @return
     */
    public String automatiskGjenopptaBehandlinger() {
        final String callId = hentCallId();
        Set<Behandling> behandlinger = behandlingKandidaterRepository.finnBehandlingerForAutomatiskGjenopptagelse();

        for (Behandling behandling : behandlinger) {
            gjenopptaBehandlingOmMulig(callId, behandling);
        }
        return "-";
    }

    public void gjenopptaBehandlingOmMulig(String callId, Behandling behandling) {
        long behandlingId = behandling.getId();
        KravgrunnlagTilstand status = kanGjennopptaStatus(behandlingId);
        var venterPåKravgrunnlagFristPassert = behandling.getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG)
            .filter(Aksjonspunkt::erOpprettet)
            .filter(a -> a.getFristTid() != null && LocalDate.now().isAfter(a.getFristTid().toLocalDate()))
            .isPresent();
        if (status == KravgrunnlagTilstand.KRAVGRUNNLAG_MANGLER && venterPåKravgrunnlagFristPassert) {
            opprettHenleggBehandlingTask(behandling, callId);
        } else if (status == KravgrunnlagTilstand.OK || status == KravgrunnlagTilstand.KRAVGRUNNLAG_MANGLER) {
            opprettFortsettBehandlingTask(behandling, callId);
        } else if (status == KravgrunnlagTilstand.KRAVGRUNNLAG_ER_SPERRET) {
            LOG.info("Behandling med id={} har et sperret kravgrunnlag, kan ikke gjenopptas,", behandlingId);
        } else if (status == KravgrunnlagTilstand.KRAVGRUNNLAG_ER_UGYLDIG) {
            LOG.info("Behandling med id={} har et ugyldig kravgrunnlag, kan ikke gjenopptas,", behandlingId);
        } else {
            throw new IllegalArgumentException("Ikke-støttet status: " + status);
        }
    }

    /**
     * Fortsetter behandling manuelt
     */
    public Optional<String> fortsettBehandlingManuelt(long behandlingId, HistorikkAktør historikkAktør) {
        var behandling = behandlingRepository.hentBehandling(behandlingId);
        if (behandling.erAvsluttet()) {
            throw new IllegalArgumentException("Kan ikke fortsette avsluttet behandling");
        }

        var kanGjenopptaBehandling = behandling.isBehandlingPåVent() && BehandlingStegType.VARSEL.equals(behandling.getAktivtBehandlingSteg())
            || (!Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG.equals(behandling.getVenteårsak()) && kanGjenopptaSteg(behandlingId));
        if (kanGjenopptaBehandling) {
            var gruppe = opprettFortsettBehandlingTask(behandling, hentCallId());
            opprettHistorikkInnslagForManueltGjenopptaBehandling(behandlingId, historikkAktør);
            return Optional.ofNullable(gruppe);
        }
        return Optional.empty();
    }

    /**
     * Fortsetter behandling, oppretter FortsettBehandlingTask
     *
     * @param behandlingId
     * @return prosesstask gruppe ID
     */
    public void fortsettBehandling(long behandlingId) {
        behandlingVenterRepository.hentBehandlingPåVent(behandlingId)
            .ifPresent(behandling -> opprettFortsettBehandlingTask(behandling, hentCallId() + behandling.getId()));
    }

    /**
     * Sjekk om behandling kan ta av vent
     *
     * @param behandlingId
     * @return
     */
    public boolean kanGjenopptaSteg(long behandlingId) {
        KravgrunnlagTilstand status = kanGjennopptaStatus(behandlingId);
        if (status == KravgrunnlagTilstand.OK) {
            return true;
        } else {
            LOG.info("Kan ikke gjenoppta steg, status er {} ", status);
            return false;
        }
    }

    private KravgrunnlagTilstand kanGjennopptaStatus(long behandlingId) {
        if (!grunnlagRepository.harGrunnlagForBehandlingId(behandlingId)) {
            return KravgrunnlagTilstand.KRAVGRUNNLAG_MANGLER;
        }
        if (grunnlagRepository.erKravgrunnlagSperret(behandlingId)) {
            return KravgrunnlagTilstand.KRAVGRUNNLAG_ER_SPERRET;
        }
        if (!grunnlagRepository.erKravgrunnlagSomForventet(behandlingId)) {
            return KravgrunnlagTilstand.KRAVGRUNNLAG_ER_UGYLDIG;
        }
        return KravgrunnlagTilstand.OK;
    }

    public enum KravgrunnlagTilstand {
        OK,
        KRAVGRUNNLAG_MANGLER,
        KRAVGRUNNLAG_ER_SPERRET,
        KRAVGRUNNLAG_ER_UGYLDIG
    }

    private String hentCallId() {
        return Optional.ofNullable(MDCOperations.getCallId()).orElseGet(MDCOperations::generateCallId);
    }

    private String opprettFortsettBehandlingTask(Behandling behandling, String callId) {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(FortsettBehandlingTask.class);
        prosessTaskData.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getFagsakId(), behandling.getId());
        prosessTaskData.setSekvens("1");
        prosessTaskData.setPrioritet(3);
        prosessTaskData.setProperty(FortsettBehandlingTask.GJENOPPTA_STEG, behandling.getAktivtBehandlingSteg().getKode());

        // unik per task da det gjelder ulike behandlinger, gjenbruker derfor ikke
        prosessTaskData.setCallId(callId + "_" + behandling.getId());

        LOG.info("Gjenopptar behandling av behandlingId={}, oppretter {} med callId={}", behandling.getId(), prosessTaskData.taskType(), callId);
        return taskTjeneste.lagre(prosessTaskData);
    }

    private String opprettHenleggBehandlingTask(Behandling behandling, String callId) {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(HenleggBehandlingTask.class);
        prosessTaskData.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getFagsakId(), behandling.getId());
        prosessTaskData.setSekvens("1");

        // unik per task da det gjelder ulike behandlinger, gjenbruker derfor ikke
        prosessTaskData.setCallId(callId + "_" + behandling.getId());

        LOG.info("Henlegger behandling med behandlingId={}", behandling.getId());
        return taskTjeneste.lagre(prosessTaskData);
    }

    private void opprettHistorikkInnslagForManueltGjenopptaBehandling(long behandlingId, HistorikkAktør historikkAktør) {
        HistorikkinnslagOld historikkinnslag = new HistorikkinnslagOld();
        historikkinnslag.setAktør(historikkAktør);
        historikkinnslag.setType(HistorikkinnslagType.BEH_MAN_GJEN);
        historikkinnslag.setBehandlingId(behandlingId);

        HistorikkInnslagTekstBuilder builder = new HistorikkInnslagTekstBuilder();
        builder.medHendelse(HistorikkinnslagType.BEH_MAN_GJEN).build(historikkinnslag);
        historikkRepository.lagre(historikkinnslag);
    }

}
