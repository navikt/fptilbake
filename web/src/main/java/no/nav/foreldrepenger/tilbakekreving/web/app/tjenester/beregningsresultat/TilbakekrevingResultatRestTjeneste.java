package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.beregningsresultat;


import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningsresultatTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.BehandlingReferanseAbacAttributter;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@Path(TilbakekrevingResultatRestTjeneste.PATH_FRAGMENT)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@ApplicationScoped
public class TilbakekrevingResultatRestTjeneste {

    public static final String PATH_FRAGMENT = "/beregning";
    private BeregningsresultatTjeneste beregningsresultatTjeneste;
    private BehandlingTjeneste behandlingTjeneste;

    public TilbakekrevingResultatRestTjeneste() {
        // for CDI
    }

    @Inject
    public TilbakekrevingResultatRestTjeneste(BeregningsresultatTjeneste beregningsresultatTjeneste, BehandlingTjeneste behandlingTjeneste) {
        this.beregningsresultatTjeneste = beregningsresultatTjeneste;
        this.behandlingTjeneste = behandlingTjeneste;
    }

    @GET
    @Path("/resultat")
    @Operation(tags = "beregning", description = "Henter beregningsresultat for tilbakekreving")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    public BeregningResultat hentBeregningResultat(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class) @QueryParam("uuid") @NotNull @Valid BehandlingReferanse behandlingReferanse) {
        return beregningsresultatTjeneste.finnEllerBeregn(hentBehandlingId(behandlingReferanse));
    }

    private Long hentBehandlingId(BehandlingReferanse behandlingReferanse) {
        return behandlingReferanse.erInternBehandlingId() ? behandlingReferanse.getBehandlingId() : behandlingTjeneste.hentBehandlingId(
            behandlingReferanse.getBehandlingUuid());
    }
}

