package no.nav.foreldrepenger.tilbakekreving.behandling.steg.sendvarsel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varselrespons.Varselrespons;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.SendVarselbrevTask;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.VarselresponsTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.FPDateUtil;


@BehandlingStegRef(kode = "VARSELSTEG")
@BehandlingTypeRef
@ApplicationScoped
public class VarselStegImpl implements VarselSteg {

    private static final Logger log = LoggerFactory.getLogger(VarselStegImpl.class);

    private BehandlingRepository behandlingRepository;
    private ProsessTaskRepository taskRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private VarselresponsTjeneste varselresponsTjeneste;
    private Period ventefrist;

    public VarselStegImpl() {
        //for cdi proxy
    }

    @Inject
    public VarselStegImpl(BehandlingRepository behandlingRepository,
                          BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                          VarselresponsTjeneste varselresponsTjeneste,
                          ProsessTaskRepository taskRepository,
                          @KonfigVerdi(value = "behandling.venter.frist.lengde") Period ventefrist) {
        this.behandlingRepository = behandlingRepository;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.varselresponsTjeneste = varselresponsTjeneste;
        this.ventefrist = ventefrist;
        this.taskRepository = taskRepository;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        LocalDateTime fristTid = FPDateUtil.nå().plus(ventefrist).plusDays(1);
        opprettSendVarselTask(behandling);

        behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
                BehandlingStegType.VARSEL, fristTid, Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);

        return BehandleStegResultat.settPåVent();
    }

    @Override
    public BehandleStegResultat gjenopptaSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        LocalDate iDag = FPDateUtil.iDag();
        Optional<Varselrespons> varselrespons = varselresponsTjeneste.hentRespons(kontekst.getBehandlingId());
        Optional<LocalDate> frist = Optional.ofNullable(behandling.getFristDatoBehandlingPåVent());
        if (frist.isPresent() && iDag.isAfter(frist.get()) && !varselrespons.isPresent()) {
            log.info("gjenopptar behandling etter utgått frist for venting på brukerrespons");
        } else {
            log.info("gjenopptar behandling etter registrert respons");
        }
        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }

    private void opprettSendVarselTask(Behandling behandling) {
        ProsessTaskData taskData = new ProsessTaskData(SendVarselbrevTask.TASKTYPE);
        taskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        taskRepository.lagre(taskData);
    }
}
