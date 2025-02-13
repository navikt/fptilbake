package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.VergeRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.felles.BrevMottaker;
import no.nav.foreldrepenger.tilbakekreving.felles.Frister;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@Dependent
@ProsessTask("brev.sendInnhentDokumentasjon")
@FagsakProsesstaskRekkefølge(gruppeSekvens = false)
public class InnhentDokumentasjonbrevTask implements ProsessTaskHandler {

    private BehandlingRepository behandlingRepository;
    private VergeRepository vergeRepository;

    private InnhentDokumentasjonbrevTjeneste innhentDokumentasjonBrevTjeneste;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    @Inject
    public InnhentDokumentasjonbrevTask(BehandlingRepositoryProvider repositoryProvider,
                                        InnhentDokumentasjonbrevTjeneste innhentDokumentasjonBrevTjeneste,
                                        BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.vergeRepository = repositoryProvider.getVergeRepository();
        this.innhentDokumentasjonBrevTjeneste = innhentDokumentasjonBrevTjeneste;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        String friTekst = prosessTaskData.getPayloadAsString();
        var unikBestillingUuid = UUID.fromString(Optional.of(prosessTaskData.getPropertyValue(TaskProperties.BESTILLING_UUID)).orElseThrow());

        if (vergeRepository.finnesVerge(behandlingId)) {
            innhentDokumentasjonBrevTjeneste.sendInnhentDokumentasjonBrev(behandlingId, friTekst, BrevMottaker.VERGE, unikBestillingUuid);
        }
        innhentDokumentasjonBrevTjeneste.sendInnhentDokumentasjonBrev(behandlingId, friTekst, BrevMottaker.BRUKER, unikBestillingUuid);

        LocalDateTime fristTid = LocalDateTime.now().plus(Frister.BEHANDLING_TILSVAR).plusDays(1);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (!behandling.isBehandlingPåVent() || !behandling.getÅpneAksjonspunkter(Set.of(AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING)).isEmpty()) {
            behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
                fristTid, Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);
        }
    }
}
