package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.Optional;

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
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
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

    private BehandlingTjeneste behandlingTjeneste;

    public BehandlingFaktaRestTjeneste() {
    }

    @Inject
    public BehandlingFaktaRestTjeneste(BehandlingTjeneste behandlingTjeneste) {
        this.behandlingTjeneste = behandlingTjeneste;
    }

    @GET
    @Path("/hent-fakta/feilutbetaling")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public BehandlingFeilutbetalingFaktaDto hentFeilutbetalingFakta(@QueryParam(value = "behandlingId") @NotNull @Valid BehandlingIdDto idDto) {
        BehandlingFeilutbetalingFaktaDto dto = null;
        Optional<BehandlingFeilutbetalingFakta> behandlingFeilutbetalingFakta = behandlingTjeneste.hentBehandlingFeilutbetalingFakta(idDto.getBehandlingId());
        if (behandlingFeilutbetalingFakta.isPresent()) {
            dto = new BehandlingFeilutbetalingFaktaDto();
            dto.setBehandlingFakta(behandlingFeilutbetalingFakta.get());
        }
        return dto;
    }

}
