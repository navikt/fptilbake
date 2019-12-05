package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.feilutbetaling;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.APPLIKASJON;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertypeDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTyperPrYtelseTypeDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste.FeilutbetalingÅrsakTjeneste;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Path("/feilutbetalingaarsak")
@Produces(APPLICATION_JSON)
@RequestScoped
@Transaction
public class FeilutbetalingÅrsakRestTjeneste {

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
        tags="kodeverk",
        description = "Henter kodeverk for årsak med underårsaker for feilutbetaling",
    responses = {
            @ApiResponse(responseCode = "200", description = "Kodeverk", content = @Content(schema = @Schema(implementation = HendelseTypeMedUndertypeDto.class)))
    })
    @BeskyttetRessurs(action = READ, ressurs = APPLIKASJON)
    public List<HendelseTyperPrYtelseTypeDto> hentAlleFeilutbetalingÅrsaker() {
        return feilutbetalingÅrsakTjeneste.hentFeilutbetalingårsaker();
    }
}
