package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.avstemming;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.DRIFT;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.tilbakekreving.avstemming.AvstemmingTjeneste;
import no.nav.vedtak.felles.integrasjon.unleash.EnvironmentProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@ApplicationScoped
@Path("/avstemming")
public class AvstemmingRestTestTjeneste {
    private static final Logger logger = LoggerFactory.getLogger(AvstemmingRestTestTjeneste.class);
    private AvstemmingTjeneste avstemmingTjeneste;

    public AvstemmingRestTestTjeneste() {
        //for cdi proxy
    }

    @Inject
    public AvstemmingRestTestTjeneste(AvstemmingTjeneste avstemmingTjeneste) {
        this.avstemmingTjeneste = avstemmingTjeneste;
    }

    @GET
    @Path("/hent")
    @Operation(tags = "Avstemming-TEST", description = "Tjeneste for å hente avstemmingdata for en dag. Brukes bare for test")
    @BeskyttetRessurs(action = READ, ressurs = DRIFT)
    public Response hentAvstemmingData(LocalDate dato) {
        validerIkkeIProd();
        String data = avstemmingTjeneste.oppsummer(dato);
        logger.info("Hentet avstemmingsdata for {}", dato);
        return Response.ok(data, MediaType.TEXT_PLAIN_TYPE).build();
    }

    private static void validerIkkeIProd() {
        Optional<String> envName = EnvironmentProperty.getEnvironmentName();
        if (envName.isPresent() && EnvironmentProperty.PROD.equalsIgnoreCase(envName.get())) {
            throw new IllegalArgumentException("Ikke tilgjengelig i dette miljøet");
        }
    }
}
