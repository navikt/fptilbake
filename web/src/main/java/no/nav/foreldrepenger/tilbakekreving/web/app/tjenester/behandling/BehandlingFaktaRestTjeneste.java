package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingFeilutbetalingFaktaDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.BehandlingReferanseAbacAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@Path(BehandlingFaktaRestTjeneste.BASE_PATH)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@ApplicationScoped
@Transactional
public class BehandlingFaktaRestTjeneste {

    public static final String BASE_PATH = "/behandlingfakta";

    private FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste;
    private BehandlingTjeneste behandlingTjeneste;

    public BehandlingFaktaRestTjeneste() {
    }

    @Inject
    public BehandlingFaktaRestTjeneste(FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste,
                                       BehandlingTjeneste behandlingTjeneste) {
        this.faktaFeilutbetalingTjeneste = faktaFeilutbetalingTjeneste;
        this.behandlingTjeneste = behandlingTjeneste;
    }

    @GET
    @Operation(
            tags = "behandlingfakta",
            description = "Hent fakta om feilutbetaling"
    )
    @Path("/hent-fakta/feilutbetaling")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK, sporingslogg = false)
    public BehandlingFeilutbetalingFaktaDto hentFeilutbetalingFakta(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
                                                                    @QueryParam(value = "uuid") @NotNull @Valid BehandlingReferanse behandlingReferanse) {
        BehandlingFeilutbetalingFakta fakta = faktaFeilutbetalingTjeneste.hentBehandlingFeilutbetalingFakta(hentBehandlingId(behandlingReferanse));
        BehandlingFeilutbetalingFaktaDto dto = new BehandlingFeilutbetalingFaktaDto();
        dto.setBehandlingFakta(fakta);
        return dto;
    }

    private Long hentBehandlingId(BehandlingReferanse behandlingReferanse) {
        return behandlingReferanse.erInternBehandlingId()
                ? behandlingReferanse.getBehandlingId()
                : behandlingTjeneste.hentBehandlingId(behandlingReferanse.getBehandlingUuid());
    }
}
