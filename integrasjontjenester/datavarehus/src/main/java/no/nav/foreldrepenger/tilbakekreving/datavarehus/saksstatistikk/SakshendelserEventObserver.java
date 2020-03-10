package no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktTilbakeførtEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunktUtførtEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.AksjonspunkterFunnetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingEnhetEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingStatusEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.prosesstask.UtvidetProsessTaskRepository;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingTilstandMapper;
import no.nav.foreldrepenger.tilbakekreving.kontrakter.sakshendelse.BehandlingTilstand;
import no.nav.vedtak.felles.integrasjon.unleash.EnvironmentProperty;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;


@ApplicationScoped
public class SakshendelserEventObserver {

    private static final Logger logger = LoggerFactory.getLogger(SakshendelserEventObserver.class);

    private ProsessTaskRepository prosessTaskRepository;
    private UtvidetProsessTaskRepository utvidetProsessTaskRepository;
    private BehandlingTilstandTjeneste behandlingTilstandTjeneste;

    private boolean isEnabled;

    SakshendelserEventObserver() {
        // for CDI proxy
    }

    @Inject
    public SakshendelserEventObserver(ProsessTaskRepository prosessTaskRepository,
                                      UtvidetProsessTaskRepository utvidetProsessTaskRepository,
                                      BehandlingTilstandTjeneste behandlingTilstandTjeneste) {
        this.prosessTaskRepository = prosessTaskRepository;
        this.utvidetProsessTaskRepository = utvidetProsessTaskRepository;
        this.behandlingTilstandTjeneste = behandlingTilstandTjeneste;

        //foreløpig kun på for testing
        Optional<String> envName = EnvironmentProperty.getEnvironmentName();
        this.isEnabled = envName.isPresent() && ("t4".equalsIgnoreCase(envName.get()) || "devimg".equalsIgnoreCase(envName.get()));

        logger.info("{} er {}", SakshendelserEventObserver.class, isEnabled ? "skudd på" : "ikke skudd på");
    }

    public void observerAksjonpunktFunnetEvent(@Observes AksjonspunkterFunnetEvent event) {
        klargjørSendingAvBehandlingensTilstand(event.getBehandlingId());
    }

    public void observerAksjonpunktUtførtEvent(@Observes AksjonspunktUtførtEvent event) {
        klargjørSendingAvBehandlingensTilstand(event.getBehandlingId());
    }

    public void observerAksjonpunktTilbakeførtEvent(@Observes AksjonspunktTilbakeførtEvent event) {
        klargjørSendingAvBehandlingensTilstand(event.getBehandlingId());
    }

    public void observerBehandlingAvsluttetEvent(@Observes BehandlingStatusEvent.BehandlingAvsluttetEvent event) {
        klargjørSendingAvBehandlingensTilstand(event.getBehandlingId());
    }

    public void observerAksjonspunktHarEndretBehandlendeEnhetEvent(@Observes BehandlingEnhetEvent event) {
        klargjørSendingAvBehandlingensTilstand(event.getBehandlingId());
    }

    public void observerStoppetEvent(@Observes BehandlingskontrollEvent.StoppetEvent event) {
        klargjørSendingAvBehandlingensTilstand(event.getBehandlingId());
    }

    private void klargjørSendingAvBehandlingensTilstand(long behandlingId) {
        if (isEnabled) {
            BehandlingTilstand tilstand = behandlingTilstandTjeneste.hentBehandlingensTilstand(behandlingId);
            opprettProsessTask(behandlingId, tilstand);
        }
    }

    private void opprettProsessTask(long behandlingId, BehandlingTilstand behandlingTilstand) {
        String gruppe = "dvh-sak-" + behandlingId;

        ProsessTaskData taskData = new ProsessTaskData(SendSakshendelserTilDvhTask.TASK_TYPE);
        taskData.setPayload(BehandlingTilstandMapper.tilJsonString(behandlingTilstand));
        taskData.setProperty("behandlingId", Long.toString(behandlingId));
        taskData.setGruppe(gruppe);
        taskData.setSekvens(String.format("%04d", finnNesteSekvensnummer(gruppe)));

        prosessTaskRepository.lagre(taskData);
    }

    private Integer finnNesteSekvensnummer(String gruppe) {
        Optional<ProsessTaskData> eksisterendeProsessTask = utvidetProsessTaskRepository.finnSisteProsessTaskForProsessTaskGruppe(SendSakshendelserTilDvhTask.TASK_TYPE, gruppe);
        return eksisterendeProsessTask.map(pt -> Integer.parseInt(pt.getSekvens()) + 1).orElse(1);
    }


}
