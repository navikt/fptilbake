package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.varsel.manuelt;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties;
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
        var unikBestillingUuid = UUID.fromString(Optional.of(prosessTaskData.getPropertyValue(TaskProperties.BESTILLING_UUID)).orElseThrow());

        // sjekk om behandlingen har verge
        var finnesVerge = vergeRepository.finnesVerge(behandlingId);
        if (DokumentMalType.VARSEL_DOK.equals(malType)) {
            if (finnesVerge) {
                manueltVarselBrevTjeneste.sendManueltVarselBrev(behandlingId, friTekst, BrevMottaker.VERGE, unikBestillingUuid);
            }
            manueltVarselBrevTjeneste.sendManueltVarselBrev(behandlingId, friTekst, BrevMottaker.BRUKER, unikBestillingUuid);
        } else if (DokumentMalType.KORRIGERT_VARSEL_DOK.equals(malType)) {
            if (finnesVerge) {
                manueltVarselBrevTjeneste.sendKorrigertVarselBrev(behandlingId, friTekst, BrevMottaker.VERGE, unikBestillingUuid);
            }
            manueltVarselBrevTjeneste.sendKorrigertVarselBrev(behandlingId, friTekst, BrevMottaker.BRUKER, unikBestillingUuid);
        }

        var fristTid = LocalDateTime.now().plus(Frister.BEHANDLING_TILSVAR).plusDays(1);
        var behandling = behandlingRepository.hentBehandling(behandlingId);
        if (!behandling.isBehandlingPåVent() || !behandling.getÅpneAksjonspunkter(Set.of(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING)).isEmpty()) {
            behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
                fristTid, Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);
        }
    }
}
