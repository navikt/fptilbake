package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.innhentdokumentasjon;

import java.time.LocalDateTime;
import java.time.Period;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
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
    private Period ventefrist;

    @Inject
    public InnhentDokumentasjonbrevTask(BehandlingRepositoryProvider repositoryProvider,
                                        InnhentDokumentasjonbrevTjeneste innhentDokumentasjonBrevTjeneste,
                                        BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                        @KonfigVerdi(value = "behandling.venter.frist.lengde") Period ventefrist) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.vergeRepository = repositoryProvider.getVergeRepository();
        this.innhentDokumentasjonBrevTjeneste = innhentDokumentasjonBrevTjeneste;
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.ventefrist = ventefrist;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Long behandlingId = ProsessTaskDataWrapper.wrap(prosessTaskData).getBehandlingId();
        String friTekst = prosessTaskData.getPayloadAsString();

        if(vergeRepository.finnesVerge(behandlingId)){
            innhentDokumentasjonBrevTjeneste.sendInnhentDokumentasjonBrev(behandlingId, friTekst, BrevMottaker.VERGE);
        }
        innhentDokumentasjonBrevTjeneste.sendInnhentDokumentasjonBrev(behandlingId, friTekst, BrevMottaker.BRUKER);

        LocalDateTime fristTid = LocalDateTime.now().plus(ventefrist).plusDays(1);
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        behandlingskontrollTjeneste.settBehandlingPåVentUtenSteg(behandling, AksjonspunktDefinisjon.VENT_PÅ_BRUKERTILBAKEMELDING,
            fristTid, Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING);
    }
}
