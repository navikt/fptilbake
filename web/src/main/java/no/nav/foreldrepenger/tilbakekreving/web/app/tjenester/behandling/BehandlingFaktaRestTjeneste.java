package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import io.swagger.annotations.Api;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingFeilutbetalingFaktaDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "behandlingfakta")
@Path(BehandlingFaktaRestTjeneste.PATH_FRAGMENT)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@ApplicationScoped
@Transaction
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
    @Path("/hent-fakta/feilutbetaling")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public BehandlingFeilutbetalingFaktaDto hentFeilutbetalingFakta(@QueryParam(value = "behandlingId") @NotNull @Valid BehandlingIdDto idDto) {
        BehandlingFeilutbetalingFakta fakta = faktaFeilutbetalingTjeneste.hentBehandlingFeilutbetalingFakta(idDto.getBehandlingId());
        BehandlingFeilutbetalingFaktaDto dto = new BehandlingFeilutbetalingFaktaDto();
        dto.setBehandlingFakta(fakta);
        return dto;
    }

}
