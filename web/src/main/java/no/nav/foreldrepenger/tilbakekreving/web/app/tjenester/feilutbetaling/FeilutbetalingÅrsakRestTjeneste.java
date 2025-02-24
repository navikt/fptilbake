package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.feilutbetaling;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertypeDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTyperPrYtelseTypeDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste.FeilutbetalingÅrsakTjeneste;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@Path(FeilutbetalingÅrsakRestTjeneste.BASE_PATH)
@Produces(APPLICATION_JSON)
@RequestScoped
@Transactional
public class FeilutbetalingÅrsakRestTjeneste {

    public static final String BASE_PATH = "/feilutbetalingaarsak";

    private FeilutbetalingÅrsakTjeneste feilutbetalingÅrsakTjeneste;

    public FeilutbetalingÅrsakRestTjeneste() {
        // For CDI
    }

    @Inject
    public FeilutbetalingÅrsakRestTjeneste(FeilutbetalingÅrsakTjeneste feilutbetalingÅrsakTjeneste) {
        this.feilutbetalingÅrsakTjeneste = feilutbetalingÅrsakTjeneste;
    }

    @GET
    @Operation(
            tags = "kodeverk",
            description = "Henter kodeverk for årsak med underårsaker for feilutbetaling",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Kodeverk", content = @Content(schema = @Schema(implementation = HendelseTypeMedUndertypeDto.class)))
            })
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.APPLIKASJON)
    public List<HendelseTyperPrYtelseTypeDto> hentAlleFeilutbetalingÅrsaker() {
        return feilutbetalingÅrsakTjeneste.hentFeilutbetalingårsaker();
    }
}
