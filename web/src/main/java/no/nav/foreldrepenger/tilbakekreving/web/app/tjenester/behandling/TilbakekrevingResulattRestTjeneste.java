package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

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
import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.TilbakekrevingBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Path(TilbakekrevingResulattRestTjeneste.PATH_FRAGMENT)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@ApplicationScoped
public class TilbakekrevingResulattRestTjeneste {

    public static final String PATH_FRAGMENT = "/beregning";
    private TilbakekrevingBeregningTjeneste tilbakekrevingBeregningTjeneste;
    private BehandlingTjeneste behandlingTjeneste;

    public TilbakekrevingResulattRestTjeneste() {
        // for CDI
    }

    @Inject
    public TilbakekrevingResulattRestTjeneste(TilbakekrevingBeregningTjeneste tilbakekrevingBeregningTjeneste,
                                              BehandlingTjeneste behandlingTjeneste) {
        this.tilbakekrevingBeregningTjeneste = tilbakekrevingBeregningTjeneste;
        this.behandlingTjeneste = behandlingTjeneste;
    }

    @GET
    @Path("/resultat")
    @Operation(tags = "beregning", description = "Henter beregningsresultat for tilbakekreving")
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    public BeregningResultat hentBeregningResultat(@QueryParam("uuid") @NotNull @Valid BehandlingReferanse behandlingReferanse) {
        return tilbakekrevingBeregningTjeneste.beregn(hentBehandlingId(behandlingReferanse));
    }

    private Long hentBehandlingId(BehandlingReferanse behandlingReferanse) {
        return behandlingReferanse.erInternBehandlingId()
            ? behandlingReferanse.getBehandlingId()
            : behandlingTjeneste.hentBehandlingId(behandlingReferanse.getBehandlingUuid());
    }
}
