package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.tilbakekreving.web.app.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.app.HentKodeverkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@Path("/kodeverk")
@RequestScoped
@Transactional
public class KodeverkRestTjeneste {

    public static final String KODERVERK_PATH = "/kodeverk";

    private HentKodeverkTjeneste hentKodeverkTjeneste; // NOSONAR

    private static final JacksonJsonConfig jsonMapper = new JacksonJsonConfig(true); // generere fulle kodeverdi-objekt

    private static final ObjectMapper objectMapper = jsonMapper.getObjectMapper();

    private static String KODELISTER;

    public KodeverkRestTjeneste() {
        // for CDI
    }

    @Inject
    public KodeverkRestTjeneste(HentKodeverkTjeneste hentKodeverkTjeneste) {
        this.hentKodeverkTjeneste = hentKodeverkTjeneste;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(tags = "kodeverk", description = "Henter kodeliste", summary = "Returnerer gruppert kodeliste.")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.APPLIKASJON, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentGruppertKodeliste() throws IOException {
        if (KODELISTER == null) {
            hentGruppertKodelisteTilCache();
        }
        var cc = new CacheControl();
        cc.setMaxAge(600 * 60); // tillater klient caching i 10 timer - case redeploy
        return Response.ok()
            .entity(KODELISTER)
            .type(MediaType.APPLICATION_JSON)
            .cacheControl(cc)
            .build();
    }

    private synchronized void hentGruppertKodelisteTilCache() throws JsonProcessingException {

        var grupperteKodelister = hentKodeverkTjeneste.hentGruppertKodeliste();
        Map<String, Object> kodelisterGruppertPåType = new HashMap<>(grupperteKodelister);

        KODELISTER = tilJson(kodelisterGruppertPåType);

    }

    private static String tilJson(Map<String, Object> kodeverk) throws JsonProcessingException {
        return objectMapper.writeValueAsString(kodeverk);
    }
}
