package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.tilbakekreving.web.app.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.app.HentKodeverkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@Path("/kodeverk")
@RequestScoped
@Transactional
public class KodeverkRestTjeneste {

    public static final String KODERVERK_PATH = "/kodeverk";

    private HentKodeverkTjeneste hentKodeverkTjeneste;

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
