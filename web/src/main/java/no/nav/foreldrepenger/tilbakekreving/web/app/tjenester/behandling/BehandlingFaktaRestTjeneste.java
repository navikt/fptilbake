package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingFeilutbetalingFaktaDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Path(BehandlingFaktaRestTjeneste.PATH_FRAGMENT)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@ApplicationScoped
@Transactional
public class BehandlingFaktaRestTjeneste {

    public static final String PATH_FRAGMENT = "/behandlingfakta";

    private FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste;

    public BehandlingFaktaRestTjeneste() {
    }

    @Inject
    public BehandlingFaktaRestTjeneste(FaktaFeilutbetalingTjeneste faktaFeilutbetalingTjeneste) {
        this.faktaFeilutbetalingTjeneste = faktaFeilutbetalingTjeneste;
    }

    @GET
    @Operation(
        tags = "behandlingfakta",
        description = "Hent fakta om feilutbetaling"
    )
    @Path("/hent-fakta/feilutbetaling")
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    public BehandlingFeilutbetalingFaktaDto hentFeilutbetalingFakta(@QueryParam(value = "behandlingId") @NotNull @Valid BehandlingIdDto idDto) {
        BehandlingFeilutbetalingFakta fakta = faktaFeilutbetalingTjeneste.hentBehandlingFeilutbetalingFakta(idDto.getBehandlingId());
        BehandlingFeilutbetalingFaktaDto dto = new BehandlingFeilutbetalingFaktaDto();
        dto.setBehandlingFakta(fakta);
        return dto;
    }

}
