package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt;

import java.util.Map;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.impl.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.VurderProsessTaskStatusForPollingApi;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.AsyncPollingStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ApplicationScoped
public class BehandlingsprosessApplikasjonTjenesteImpl implements BehandlingsprosessApplikasjonTjeneste {

    BehandlingRepository behandlingRepository;
    BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste;

    BehandlingsprosessApplikasjonTjenesteImpl() {
        // CDI
    }

    /**
     * test only
     */
    BehandlingsprosessApplikasjonTjenesteImpl(BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste) {
        this.behandlingskontrollAsynkTjeneste = behandlingskontrollAsynkTjeneste;
    }

    @Inject
    public BehandlingsprosessApplikasjonTjenesteImpl(BehandlingRepository behandlingRepository,
                                                     BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.behandlingskontrollAsynkTjeneste = behandlingskontrollAsynkTjeneste;
    }

    @Override
    public Behandling hentBehandling(Long behandlingId) {
        return behandlingRepository.hentBehandling(behandlingId);
    }

    @Override
    public Optional<AsyncPollingStatus> sjekkProsessTaskPågårForBehandling(Behandling behandling, String gruppe) {
        Long behandlingId = behandling.getId();

        Map<String, ProsessTaskData> nesteTask = behandlingskontrollAsynkTjeneste.sjekkProsessTaskPågårForBehandling(behandling, gruppe);
        return new VurderProsessTaskStatusForPollingApi(behandlingId).sjekkStatusNesteProsessTask(gruppe, nesteTask);
    }
}
