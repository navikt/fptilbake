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

import org.eclipse.jetty.http.HttpStatus;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTyperPrYtelseTypeDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertypeDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste.FeilutbetalingÅrsakTjeneste;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "feilutbetalingårsak")
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
    @ApiOperation(value = "Henter kodeverk for årsak med underårsaker for feilutbetaling")
    @ApiResponse(code = HttpStatus.OK_200, message = "OK", response = HendelseTypeMedUndertypeDto.class)
    @BeskyttetRessurs(action = READ, ressurs = APPLIKASJON)
    public List<HendelseTyperPrYtelseTypeDto> hentAlleFeilutbetalingÅrsaker() {
        return feilutbetalingÅrsakTjeneste.hentFeilutbetalingårsaker();
    }
}
