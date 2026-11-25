package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt;

import java.util.Map;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandling.impl.ProsesseringAsynkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.VurderProsessTaskStatusForPollingApi;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.AsyncPollingStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ApplicationScoped
public class BehandlingsprosessApplikasjonTjenesteImpl implements BehandlingsprosessApplikasjonTjeneste {

    BehandlingRepository behandlingRepository;
    ProsesseringAsynkTjeneste prosesseringAsynkTjeneste;

    BehandlingsprosessApplikasjonTjenesteImpl() {
        // CDI
    }

    /**
     * test only
     */
    BehandlingsprosessApplikasjonTjenesteImpl(ProsesseringAsynkTjeneste prosesseringAsynkTjeneste) {
        this.prosesseringAsynkTjeneste = prosesseringAsynkTjeneste;
    }

    @Inject
    public BehandlingsprosessApplikasjonTjenesteImpl(BehandlingRepository behandlingRepository,
                                                     ProsesseringAsynkTjeneste prosesseringAsynkTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.prosesseringAsynkTjeneste = prosesseringAsynkTjeneste;
    }

    @Override
    public Behandling hentBehandling(Long behandlingId) {
        return behandlingRepository.hentBehandling(behandlingId);
    }

    @Override
    public Optional<AsyncPollingStatus> sjekkProsessTaskPågårForBehandling(Behandling behandling, String gruppe) {
        Long behandlingId = behandling.getId();

        Map<String, ProsessTaskData> nesteTask = prosesseringAsynkTjeneste.sjekkProsessTaskPågårForBehandling(behandling, gruppe);
        return new VurderProsessTaskStatusForPollingApi(behandlingId).sjekkStatusNesteProsessTask(gruppe, nesteTask);
    }
}
