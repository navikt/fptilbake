package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.etterpopuler;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.BehandlingTilstandTjeneste;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.aiven.AivenEttersendelserKafkaProducer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@Dependent
@ProsessTask(value = "dvh.ettersend.sakshendelser", prioritet = 4)
public class EttersendSakshendelserTilDvhTask implements ProsessTaskHandler {

    private final AivenEttersendelserKafkaProducer aivenEttersendelserKafkaProducer;
    private final BehandlingRepository behandlingRepository;
    private final BehandlingTilstandTjeneste behandlingTilstandTjeneste;

    @Inject
    public EttersendSakshendelserTilDvhTask(AivenEttersendelserKafkaProducer aivenEttersendelserKafkaProducer,
                                            BehandlingRepository behandlingRepository,
                                            BehandlingTilstandTjeneste behandlingTilstandTjeneste) {
        this.aivenEttersendelserKafkaProducer = aivenEttersendelserKafkaProducer;
        this.behandlingRepository = behandlingRepository;
        this.behandlingTilstandTjeneste = behandlingTilstandTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        long behandlingId = prosessTaskData.getBehandlingIdAsLong();
        var behandling = behandlingRepository.hentBehandling(behandlingId);
        if (!behandling.erAvsluttet()) {
            return;
        }
        var behandlingTilstand = behandlingTilstandTjeneste.hentBehandlingensTilstand(behandling);
        aivenEttersendelserKafkaProducer.sendMelding(behandlingTilstand);
    }

}
