package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.task;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkTabellRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.task.ProsessTaskDataWrapper;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

/**
 * Kjører behandlingskontroll automatisk fra der prosessen står.
 */
@ApplicationScoped
@ProsessTask(FortsettBehandlingTaskProperties.TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class FortsettBehandlingTask implements ProsessTaskHandler {

    private BehandlingRepository behandlingRepository;
    private AksjonspunktRepository aksjonspunktRepository;
    private KodeverkTabellRepository kodeverkTabellRepository;

    FortsettBehandlingTask() {
        // For CDI proxy
    }

    @Inject
    public FortsettBehandlingTask(BehandlingRepositoryProvider repositoryProvider) {
        behandlingRepository = repositoryProvider.getBehandlingRepository();
        aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        kodeverkTabellRepository = repositoryProvider.getKodeverkRepository().getKodeverkTabellRepository();
    }

    @Override
    public void doTask(ProsessTaskData data) {

        // dynamisk lookup, så slipper vi å validere bean ved oppstart i test av moduler etc. før det faktisk brukes
        CDI<Object> cdi = CDI.current();
        BehandlingskontrollTjeneste behandlingskontrollTjeneste = cdi.select(BehandlingskontrollTjeneste.class).get();

        try {
            Long behandlingId = ProsessTaskDataWrapper.wrap(data).getBehandlingId();
            BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
            Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
            Boolean manuellFortsettelse = Optional.ofNullable(data.getPropertyValue(FortsettBehandlingTaskProperties.MANUELL_FORTSETTELSE))
                .map(Boolean::valueOf)
                .orElse(Boolean.FALSE);
            String gjenoppta = data.getPropertyValue(FortsettBehandlingTaskProperties.GJENOPPTA_STEG);

            BehandlingStegType stegtype = null;
            if (gjenoppta != null) {
                stegtype = finnBehandlingStegType(gjenoppta);
            }
            if (gjenoppta != null || manuellFortsettelse) {
                taBehandlingAvVentSettAlleAutopunkterUtført(behandling, kontekst, behandlingskontrollTjeneste);
            } else {
                settAutopunktTilUtført(data, kontekst, behandlingskontrollTjeneste);
            }
            // Ingen åpne autopunkt her, takk
            validerBehandlingIkkeErSattPåVent(behandling);

            // Sjekke om kan prosesserere, samt feilhåndtering vs savepoint: Ved retry av feilet task som har passert gjenopptak må man fortsette.
            Optional<BehandlingStegTilstand> tilstand = behandling.getBehandlingStegTilstand();
            if (gjenoppta != null && tilstand.isPresent() && tilstand.get().getBehandlingSteg().equals(stegtype)
                && BehandlingStegStatus.VENTER.equals(tilstand.get().getBehandlingStegStatus())) {
                behandlingskontrollTjeneste.prosesserBehandlingGjenopptaHvisStegVenter(kontekst, stegtype);
            } else if (!behandling.erAvsluttet()) {
                behandlingskontrollTjeneste.prosesserBehandling(kontekst);
            }

        } finally {
            cdi.destroy(behandlingskontrollTjeneste);
        }
    }

    private void validerBehandlingIkkeErSattPåVent(Behandling behandling) {
        if (behandling.isBehandlingPåVent()) {
            throw new IllegalStateException("Utviklerfeil: Ikke tillatt å fortsette behandling på vent");
        }
    }

    private BehandlingStegType finnBehandlingStegType(String gjenoppta) {
        BehandlingStegType stegtype = kodeverkTabellRepository.finnBehandlingStegType(gjenoppta);
        if (stegtype == null) {
            throw new IllegalStateException("Utviklerfeil: ukjent steg " + gjenoppta);
        }
        return stegtype;
    }

    private void taBehandlingAvVentSettAlleAutopunkterUtført(Behandling behandling, BehandlingskontrollKontekst kontekst,
                                                             BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        if (behandling.isBehandlingPåVent()) { // Autopunkt
            behandlingskontrollTjeneste.taBehandlingAvVentSetAlleAutopunktUtført(behandling, kontekst);
        }
    }

    private void settAutopunktTilUtført(ProsessTaskData data, BehandlingskontrollKontekst kontekst, BehandlingskontrollTjeneste behandlingskontrollTjeneste) {
        String utført = data.getPropertyValue(FortsettBehandlingTaskProperties.UTFORT_AUTOPUNKT);
        if (utført != null) {
            AksjonspunktDefinisjon aksjonspunkt = aksjonspunktRepository.finnAksjonspunktDefinisjon(utført);
            behandlingskontrollTjeneste.settAutopunktTilUtført(aksjonspunkt, kontekst);
        }
    }
}
