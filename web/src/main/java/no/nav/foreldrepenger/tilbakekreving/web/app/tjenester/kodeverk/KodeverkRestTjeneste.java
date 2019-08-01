package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.APPLIKASJON;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.app.HentKodeverkTjeneste;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.util.LRUCache;

@Api(tags = {"kodeverk"})
@Path("/kodeverk")
@RequestScoped
@Transaction
public class KodeverkRestTjeneste {

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
    @ApiOperation(value = "Henter kodeliste", notes = ("Returnerer gruppert kodeliste."))
    @BeskyttetRessurs(action = READ, ressurs = APPLIKASJON, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Map<String, Object> hentGruppertKodeliste() {
        if (kodelisteCache.get("alle") == null) {
            kodelisteCache.put("alle", this.hentGruppertKodelisteTilCache());
        }
        return kodelisteCache.get("alle");
    }

    private synchronized Map<String, Object> hentGruppertKodelisteTilCache() {
        Map<String, Object> kodelisterGruppertPåType = new HashMap<>();

        Map<String, List<Kodeliste>> grupperteKodelister = hentKodeverkTjeneste.hentGruppertKodeliste();
        grupperteKodelister.entrySet().forEach(e -> kodelisterGruppertPåType.put(e.getKey(), e.getValue()));

        return kodelisterGruppertPåType;
    }
}
