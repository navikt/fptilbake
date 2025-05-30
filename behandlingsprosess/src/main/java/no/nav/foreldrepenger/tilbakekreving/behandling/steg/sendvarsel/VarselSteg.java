package no.nav.foreldrepenger.tilbakekreving.behandling.steg.sendvarsel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselInfo;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.VarselRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.respons.Varselrespons;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.SendVarselbrevTask;
import no.nav.foreldrepenger.tilbakekreving.felles.Frister;
import no.nav.foreldrepenger.tilbakekreving.varselrespons.VarselresponsTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;


@BehandlingStegRef(BehandlingStegType.VARSEL)
@BehandlingTypeRef
@ApplicationScoped
public class VarselSteg implements BehandlingSteg {

    private static final Logger LOG = LoggerFactory.getLogger(VarselSteg.class);

    private BehandlingRepository behandlingRepository;
    private ProsessTaskTjeneste taskTjeneste;
    private VarselRepository varselRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private VarselresponsTjeneste varselresponsTjeneste;

    public VarselSteg() {
        //for cdi proxy
    }

    @Inject
    public VarselSteg(BehandlingRepositoryProvider behandlingRepositoryProvider,
                      BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                      VarselresponsTjeneste varselresponsTjeneste,
                      ProsessTaskTjeneste taskTjeneste) {
        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
        this.taskTjeneste = taskTjeneste;
        this.varselRepository = behandlingRepositoryProvider.getVarselRepository();

        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.varselresponsTjeneste = varselresponsTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        if (sjekkTilbakekrevingOpprettetUtenVarsel(behandling.getId()) || behandling.isManueltOpprettet()) { //ikke sendt varsel når behandling er opprettet manuelt eller opprettet uten varsel
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        }
        LocalDateTime fristTid = LocalDateTime.now().plus(Frister.BEHANDLING_TILSVAR).plusDays(1);
        opprettSendVarselTask(behandling);

        behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
                BehandlingStegType.VARSEL, fristTid, Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);

        return BehandleStegResultat.settPåVent();
    }

    @Override
    public BehandleStegResultat gjenopptaSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        LocalDate iDag = LocalDate.now();
        if (sjekkTilbakekrevingOpprettetUtenVarsel(behandling.getId())) { // hvis det er ingen varselTekst finnes,kan behandling fortsette
            return BehandleStegResultat.utførtUtenAksjonspunkter();
        }
        Optional<Varselrespons> varselrespons = varselresponsTjeneste.hentRespons(kontekst.getBehandlingId());
        Optional<LocalDate> frist = Optional.ofNullable(behandling.getFristDatoBehandlingPåVent());
        if (frist.isPresent() && iDag.isAfter(frist.get()) && varselrespons.isEmpty()) {
            LOG.info("gjenopptar behandling etter utgått frist for venting på brukerrespons");
        } else {
            LOG.info("gjenopptar behandling etter registrert respons");
        }
        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }

    private void opprettSendVarselTask(Behandling behandling) {
        ProsessTaskGruppe taskGruppe = new ProsessTaskGruppe();

        sendVarsel(behandling, taskGruppe);

        taskTjeneste.lagre(taskGruppe);
    }

    private void sendVarsel(Behandling behandling, ProsessTaskGruppe taskGruppe) {
        ProsessTaskData sendVarselbrev = ProsessTaskData.forProsessTask(SendVarselbrevTask.class);
        sendVarselbrev.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getFagsakId(), behandling.getId());
        sendVarselbrev.setProperty(TaskProperties.BESTILLING_UUID, UUID.randomUUID().toString()); // Brukes som eksternReferanseId ved journalføring av brev
        taskGruppe.addNesteSekvensiell(sendVarselbrev);
    }

    private boolean sjekkTilbakekrevingOpprettetUtenVarsel(Long behandlingId) {
        Optional<VarselInfo> varselEntitet = varselRepository.finnVarsel(behandlingId);
        if (varselEntitet.isEmpty()) {
            LOG.info("VarselTekst finnes ikke for behandlingId={}, ikke sende varsel til bruker!!", behandlingId);
            return true;
        }
        return false;
    }
}
