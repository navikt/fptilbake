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
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BeregningResultat;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Path(TilbakekrevingResulattRestTjeneste.PATH_FRAGMENT)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@ApplicationScoped
public class TilbakekrevingResulattRestTjeneste {

    public static final String PATH_FRAGMENT = "/beregning";
    private TilbakekrevingBeregningTjeneste tilbakekrevingBeregningTjeneste;

    public TilbakekrevingResulattRestTjeneste() {
        // for CDI
    }

    @Inject
    public TilbakekrevingResulattRestTjeneste(TilbakekrevingBeregningTjeneste tilbakekrevingBeregningTjeneste) {
        this.tilbakekrevingBeregningTjeneste = tilbakekrevingBeregningTjeneste;
    }

    @GET
    @Path("/resultat")
    @Operation(tags = "beregning", description = "Henter beregningsresultat for tilbakekreving")
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    public BeregningResultat hentBeregningResultat(@QueryParam("behandlingId") @NotNull @Valid BehandlingIdDto behandlingIdDto) {
        return tilbakekrevingBeregningTjeneste.beregn(behandlingIdDto.getBehandlingId());
    }

}
