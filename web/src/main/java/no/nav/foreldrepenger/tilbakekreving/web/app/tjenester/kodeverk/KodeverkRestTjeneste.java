package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk;

import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import jakarta.enterprise.context.RequestScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkAktør;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeverdi;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.Aktsomhet;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.AnnenVurdering;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.web.app.jackson.ObjectMapperFactory;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.dto.AlleKodeverdierSomObjektResponse;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.kodeverk.dto.KodeverdiSomObjekt;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.caching.CacheControl;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@Path("/kodeverk")
@RequestScoped
@Transactional
public class KodeverkRestTjeneste {

    public static final String KODERVERK_PATH = "/kodeverk";

    private static final ObjectMapper objectMapper = ObjectMapperFactory.getDefaultObjectMapperCopy(true);

    private static String KODELISTER;

    public KodeverkRestTjeneste() {
        // for CDI
    }

    private final static AlleKodeverdierSomObjektResponse oppslagAlleResponse;
    public final static Map<String, SortedSet<Kodeverdi>> legacyGrupperteKodeverdier;

    // Hjelpefunksjon for å konvertere nytt dataformat til gammalt, så vi kan beholde gammalt endepunkt ei stund.
    private static <K extends Kodeverdi> void addLegacyGruppertKodeverdier(final SortedSet<KodeverdiSomObjekt<K>> verdier) {
        final String gruppenavn = verdier.getFirst().getKilde().getClass().getSimpleName();
        final var verdierSomGenerelleKodeverdiObjekter = verdier.stream()
            .filter(o -> !o.getKode().equals("-")) // Utelat kode "-", for å stemme med tidligere kode
            .map(o -> (Kodeverdi) o.getKilde())
            .collect(Collectors.toCollection(() -> new TreeSet<>((a, b) -> a.getKode().compareTo(b.getKode()))));
        legacyGrupperteKodeverdier.put(gruppenavn, verdierSomGenerelleKodeverdiObjekter);
    }

    static {
        oppslagAlleResponse = new AlleKodeverdierSomObjektResponse(
            KodeverdiSomObjekt.sorterte(EnumSet.allOf(Fagsystem.class)),
            KodeverdiSomObjekt.sorterte(EnumSet.allOf(Venteårsak.class)),
            KodeverdiSomObjekt.sorterte(EnumSet.allOf(Aktsomhet.class)),
            KodeverdiSomObjekt.sorterte(EnumSet.allOf(AnnenVurdering.class)),
            KodeverdiSomObjekt.sorterte(EnumSet.allOf(SærligGrunn.class)),
            KodeverdiSomObjekt.sorterte(EnumSet.allOf(VilkårResultat.class)),
            KodeverdiSomObjekt.sorterte(EnumSet.allOf(VedtakResultatType.class)),
            KodeverdiSomObjekt.sorterte(EnumSet.allOf(ForeldelseVurderingType.class)),
            KodeverdiSomObjekt.sorterte(EnumSet.allOf(HistorikkAktør.class)),
            KodeverdiSomObjekt.sorterte(EnumSet.allOf(HendelseType.class)),
            KodeverdiSomObjekt.sorterte(EnumSet.allOf(SkjermlenkeType.class)),
            KodeverdiSomObjekt.sorterte(EnumSet.allOf(HendelseUnderType.class)),
            KodeverdiSomObjekt.sorterte(EnumSet.allOf(BehandlingResultatType.class)),
            KodeverdiSomObjekt.sorterte(EnumSet.allOf(VidereBehandling.class)),
            KodeverdiSomObjekt.sorterte(EnumSet.allOf(VergeType.class)),
            KodeverdiSomObjekt.sorterte(EnumSet.allOf(VurderÅrsak.class)),
            KodeverdiSomObjekt.sorterte(EnumSet.allOf(BehandlingÅrsakType.class)),
            KodeverdiSomObjekt.sorterte(EnumSet.allOf(BehandlingType.class))
        );

        legacyGrupperteKodeverdier = new LinkedHashMap<>();
        addLegacyGruppertKodeverdier(oppslagAlleResponse.fagsystemer());
        addLegacyGruppertKodeverdier(oppslagAlleResponse.venteårsaker());
        addLegacyGruppertKodeverdier(oppslagAlleResponse.aktsomheter());
        addLegacyGruppertKodeverdier(oppslagAlleResponse.annenVurderinger());
        addLegacyGruppertKodeverdier(oppslagAlleResponse.særligGrunner());
        addLegacyGruppertKodeverdier(oppslagAlleResponse.vilkårResultater());
        addLegacyGruppertKodeverdier(oppslagAlleResponse.vedtakResultatTyper());
        addLegacyGruppertKodeverdier(oppslagAlleResponse.foreldelseVurderingTyper());
        addLegacyGruppertKodeverdier(oppslagAlleResponse.historikkAktører());
        addLegacyGruppertKodeverdier(oppslagAlleResponse.hendelseTyper());
        addLegacyGruppertKodeverdier(oppslagAlleResponse.skjermlenkeTyper());
        addLegacyGruppertKodeverdier(oppslagAlleResponse.hendelseUnderTyper());
        addLegacyGruppertKodeverdier(oppslagAlleResponse.behandlingResultatTyper());
        addLegacyGruppertKodeverdier(oppslagAlleResponse.videreBehandlinger());
        addLegacyGruppertKodeverdier(oppslagAlleResponse.vergeTyper());
        addLegacyGruppertKodeverdier(oppslagAlleResponse.vurderÅrsaker());
        addLegacyGruppertKodeverdier(oppslagAlleResponse.behandlingÅrsakTyper());
        addLegacyGruppertKodeverdier(oppslagAlleResponse.behandlingTyper());
    }

    @GET
    @Path("/alle/objekt")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.APPLIKASJON, sporingslogg = false)
    @Operation(description = "Alle statiske kodeverdier som objekt", tags = "kodeverk")
    @CacheControl(maxAge = 600 * 60)
    public AlleKodeverdierSomObjektResponse alleKodeverdierSomObjekt() {
        return oppslagAlleResponse;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(tags = "kodeverk", description = "Henter kodeliste", summary = "Returnerer gruppert kodeliste.")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.APPLIKASJON, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    @CacheControl(maxAge = 600 * 60)
    public Response hentGruppertKodeliste() throws IOException {
        if (KODELISTER == null) {
            hentGruppertKodelisteTilCache();
        }
        return Response.ok()
            .entity(KODELISTER)
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    private synchronized void hentGruppertKodelisteTilCache() throws JsonProcessingException {
        KODELISTER = tilJson(legacyGrupperteKodeverdier);
    }

    private static String tilJson(Map<String, SortedSet<Kodeverdi>> kodeverk) throws JsonProcessingException {
        return objectMapper.writeValueAsString(kodeverk);
    }
}
