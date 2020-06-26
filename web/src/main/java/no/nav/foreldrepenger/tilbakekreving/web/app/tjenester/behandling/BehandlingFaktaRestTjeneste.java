package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

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
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.FaktaFeilutbetalingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingFeilutbetalingFaktaDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Path(BehandlingFaktaRestTjeneste.PATH_FRAGMENT)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@ApplicationScoped
@Transactional
public class BehandlingFaktaRestTjeneste {

    public static final String PATH_FRAGMENT = "/behandlingfakta";

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
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public BehandlingFeilutbetalingFaktaDto hentFeilutbetalingFakta(@QueryParam(value = "behandlingUuid") @NotNull @Valid BehandlingReferanse behandlingReferanse) {
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
