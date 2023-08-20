package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt;

import java.time.LocalDateTime;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.felles.Frister;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@Dependent
@ProsessTask(value = "brev.sendManueltVarsel", maxFailedRuns = 5, firstDelay = 60)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class SendManueltVarselbrevTask implements ProsessTaskHandler {

    public static final String MAL_TYPE = "malType";

    private final BehandlingRepository behandlingRepository;
    private final VergeRepository vergeRepository;

    private final ManueltVarselBrevTjeneste manueltVarselBrevTjeneste;
    private final BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    @Inject
    public SendManueltVarselbrevTask(BehandlingRepositoryProvider repositoryProvider,
                                     ManueltVarselBrevTjeneste manueltVarselBrevTjeneste,
                                     BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.vergeRepository = repositoryProvider.getVergeRepository();
        this.manueltVarselBrevTjeneste = manueltVarselBrevTjeneste;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        var malType = DokumentMalType.fraKode(prosessTaskData.getPropertyValue(MAL_TYPE));
        var friTekst = prosessTaskData.getPayloadAsString();
        // sjekk om behandlingen har verge
        var finnesVerge = vergeRepository.finnesVerge(behandlingId);
        if (DokumentMalType.VARSEL_DOK.equals(malType)) {
            if (finnesVerge) {
                manueltVarselBrevTjeneste.sendManueltVarselBrev(behandlingId, friTekst, BrevMottaker.VERGE);
            }
            manueltVarselBrevTjeneste.sendManueltVarselBrev(behandlingId, friTekst, BrevMottaker.BRUKER);
        } else if (DokumentMalType.KORRIGERT_VARSEL_DOK.equals(malType)) {
            if (finnesVerge) {
                manueltVarselBrevTjeneste.sendKorrigertVarselBrev(behandlingId, friTekst, BrevMottaker.VERGE);
            }
            manueltVarselBrevTjeneste.sendKorrigertVarselBrev(behandlingId, friTekst, BrevMottaker.BRUKER);
        }

        var fristTid = LocalDateTime.now().plus(Frister.BEHANDLING_TILSVAR).plusDays(1);
        var behandling = behandlingRepository.hentBehandling(behandlingId);
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
                fristTid, Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);
    }
}
