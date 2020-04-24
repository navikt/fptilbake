package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.verge;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.collect.Lists;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.VergeFeil;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.verge.VergeTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@ApplicationScoped
@Path(VergeRestTjeneste.BASE_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class VergeRestTjeneste {

    static final String BASE_PATH = "/verge";
    private BehandlingTjeneste behandlingTjeneste;
    private VergeTjeneste vergeTjeneste;

    public VergeRestTjeneste() {
    }

    @Inject
    public VergeRestTjeneste(BehandlingTjeneste behandlingTjeneste,
                             VergeTjeneste vergeTjeneste){
        this.behandlingTjeneste = behandlingTjeneste;
        this.vergeTjeneste = vergeTjeneste;
    }

    @POST
    @Path("/opprett")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Oppretter aksjonspunkt for verge/fullmektig på behandlingen",
        tags = "verge",
        responses = {
            @ApiResponse(responseCode = "200",
                description = "Aksjonspunkt for verge/fullmektig opprettes",
                headers = @Header(name = HttpHeaders.LOCATION)
            )
        })
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response opprettVerge(@Parameter(description = "Behandling som skal få verge/fullmektig") @Valid BehandlingIdDto dto) {
        Behandling behandling = behandlingTjeneste.hentBehandling(dto.getBehandlingId());
        if(behandling.erAvsluttet() || behandling.isBehandlingPåVent()){
            throw VergeFeil .FACTORY.kanIkkeOppretteVerge(behandling.getId()).toException();
        }
        if(!behandling.getÅpneAksjonspunkter(Lists.newArrayList(AksjonspunktDefinisjon.AVKLAR_VERGE)).isEmpty()){
            throw VergeFeil.FACTORY.harAlleredeAksjonspunktForVerge(behandling.getId()).toException();
        }
        vergeTjeneste.opprettVergeAksjonspunktOgHoppTilbakeTilFaktaHvisSenereSteg(behandling);

        return Response.ok().build();
    }
}
