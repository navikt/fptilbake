package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt;

import java.time.LocalDateTime;
import java.time.Period;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
@ProsessTask(SendManueltVarselbrevTask.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class SendManueltVarselbrevTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "brev.sendManueltVarsel";

    private BehandlingRepository behandlingRepository;

    private ManueltVarselBrevTjeneste manueltVarselBrevTjeneste;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private Period ventefrist;

    @Inject
    public SendManueltVarselbrevTask(BehandlingRepository behandlingRepository,
                                     ManueltVarselBrevTjeneste manueltVarselBrevTjeneste,
                                     BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                     @KonfigVerdi(value = "behandling.venter.frist.lengde") Period ventefrist) {
        this.manueltVarselBrevTjeneste = manueltVarselBrevTjeneste;
        this.behandlingRepository = behandlingRepository;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.ventefrist = ventefrist;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = prosessTaskData.getBehandlingId();
        DokumentMalType malType = DokumentMalType.fraKode(prosessTaskData.getPropertyValue(TaskProperty.MAL_TYPE));
        String friTekst = prosessTaskData.getPayloadAsString();

        if (DokumentMalType.VARSEL_DOK.equals(malType)) {
            manueltVarselBrevTjeneste.sendManueltVarselBrev(behandlingId, malType, friTekst);
        }else if (DokumentMalType.KORRIGERT_VARSEL_DOK.equals(malType)) {
            manueltVarselBrevTjeneste.sendKorrigertVarselBrev(behandlingId, malType, friTekst);
        }

        LocalDateTime fristTid = LocalDateTime.now().plus(ventefrist).plusDays(1);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
            fristTid, Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);
    }
}
