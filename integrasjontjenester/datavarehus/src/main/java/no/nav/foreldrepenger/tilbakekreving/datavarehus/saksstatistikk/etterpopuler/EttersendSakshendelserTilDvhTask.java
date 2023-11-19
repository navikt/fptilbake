package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.etterpopuler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.BehandlingTilstandTjeneste;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.aiven.AivenEttersendelserKafkaProducer;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.aiven.AivenSakshendelserKafkaProducer;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingTilstandMapper;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;

@Dependent
@ProsessTask("dvh.ettersend.sakshendelser")
public class EttersendSakshendelserTilDvhTask implements ProsessTaskHandler {

    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess");

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
        long behandlingId = Long.parseLong(prosessTaskData.getBehandlingId());
        LOG_CONTEXT.add("behandling", Long.toString(behandlingId));
        var behandling = behandlingRepository.hentBehandling(behandlingId);
        if (!behandling.erAvsluttet()) {
            return;
        }
        var behandlingTilstand = behandlingTilstandTjeneste.hentBehandlingensTilstand(behandling);
        aivenEttersendelserKafkaProducer.sendMelding(behandlingTilstand);
    }

}
