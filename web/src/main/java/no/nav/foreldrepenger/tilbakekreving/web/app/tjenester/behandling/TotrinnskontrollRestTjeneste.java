package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.TotrinnskontrollAksjonspunkterTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.totrinn.TotrinnskontrollSkjermlenkeContextDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.BehandlingReferanseAbacAttributter;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@Path("/behandling/totrinnskontroll")
@ApplicationScoped
@Transactional
public class TotrinnskontrollRestTjeneste {

    private BehandlingRepository behandlingRepository;
    private TotrinnskontrollAksjonspunkterTjeneste totrinnskontrollTjeneste;

    public TotrinnskontrollRestTjeneste() {
        //
    }

    @Inject
    public TotrinnskontrollRestTjeneste(BehandlingRepository behandlingRepository, TotrinnskontrollAksjonspunkterTjeneste totrinnskontrollTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.totrinnskontrollTjeneste = totrinnskontrollTjeneste;
    }


    @GET
    @Path("/arsaker")
    @Operation(tags = "totrinnskontroll", description = "Hent aksjonspunkter som skal til totrinnskontroll.", summary = "Returner aksjonspunkter til totrinnskontroll for behandling.")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<TotrinnskontrollSkjermlenkeContextDto> hentTotrinnskontrollSkjermlenkeContext(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
                                                                                              @NotNull @QueryParam("uuid") @Valid BehandlingReferanse behandlingReferanse) {
        Behandling behandling = hentBehandling(behandlingReferanse);
        return totrinnskontrollTjeneste.hentTotrinnsSkjermlenkeContext(behandling);
    }

    @GET
    @Path("/arsaker_read_only")
    @Operation(tags = "totrinnskontroll", description = "Hent totrinnsvurderinger for aksjonspunkter.", summary = "Returner vurderinger for aksjonspunkter etter totrinnskontroll for behandling.")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<TotrinnskontrollSkjermlenkeContextDto> hentTotrinnskontrollvurderingSkjermlenkeContext(
            @TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
            @NotNull @QueryParam("uuid") @Valid BehandlingReferanse behandlingReferanse) {
        Behandling behandling = hentBehandling(behandlingReferanse);
        return totrinnskontrollTjeneste.hentTotrinnsvurderingSkjermlenkeContext(behandling);
    }

    private Behandling hentBehandling(BehandlingReferanse behandlingReferanse) {
        Behandling behandling;
        if (behandlingReferanse.erInternBehandlingId()) {
            behandling = behandlingRepository.hentBehandling(behandlingReferanse.getBehandlingId());
        } else {
            behandling = behandlingRepository.hentBehandling(behandlingReferanse.getBehandlingUuid());
        }
        return behandling;
    }
}
