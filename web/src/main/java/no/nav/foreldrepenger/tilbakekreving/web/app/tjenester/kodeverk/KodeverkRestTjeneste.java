package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.app.HentKodeverkTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.util.LRUCache;

@Path("/kodeverk")
@RequestScoped
@Transactional
public class KodeverkRestTjeneste {

    public static final String KODERVERK_PATH = "/kodeverk";

    private HentKodeverkTjeneste hentKodeverkTjeneste; // NOSONAR

    private static final long CACHE_ELEMENT_LIVE_TIME_MS = TimeUnit.MILLISECONDS.convert(60, TimeUnit.MINUTES);
    private LRUCache<String, Map<String, Object>> kodelisteCache = new LRUCache<>(10, CACHE_ELEMENT_LIVE_TIME_MS);

    public KodeverkRestTjeneste() {
        // for resteasy
    }

    @Inject
    public KodeverkRestTjeneste(HentKodeverkTjeneste hentKodeverkTjeneste) {
        this.hentKodeverkTjeneste = hentKodeverkTjeneste;
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(tags = "kodeverk", description = "Henter kodeliste", summary = "Returnerer gruppert kodeliste.")
    @BeskyttetRessurs(action = READ, property = AbacProperty.APPLIKASJON, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Map<String, Object> hentGruppertKodeliste() {
        if (kodelisteCache.get("alle") == null) {
            kodelisteCache.put("alle", this.hentGruppertKodelisteTilCache());
        }
        return kodelisteCache.get("alle");
    }

    private synchronized Map<String, Object> hentGruppertKodelisteTilCache() {
        Map<String, Object> kodelisterGruppertPåType = new HashMap<>();

        var grupperteKodelister = hentKodeverkTjeneste.hentGruppertKodeliste();
        grupperteKodelister.entrySet().forEach(e -> kodelisterGruppertPåType.put(e.getKey(), e.getValue()));

        return kodelisterGruppertPåType;
    }
}
