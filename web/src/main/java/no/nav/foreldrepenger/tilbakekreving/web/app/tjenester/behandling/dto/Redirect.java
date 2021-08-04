package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.BehandlingRestTjeneste;

public final class Redirect {

    private static final Logger LOG = LoggerFactory.getLogger(Redirect.class);

    private Redirect() {
        // no ctor
    }

    public static Response tilBehandlingPollStatus(HttpServletRequest request, UUID uuid, Optional<String> gruppeOpt) throws URISyntaxException {
        var uriBuilder = getUriBuilder(request)
            .path(BehandlingRestTjeneste.STATUS_PATH)
            .queryParam("uuid", uuid);
        gruppeOpt.ifPresent(s -> uriBuilder.queryParam("gruppe", s));
        return Response.accepted().location(honorXForwardedProto(request, uriBuilder.build())).build();
    }

    public static Response tilBehandlingPollStatus(HttpServletRequest request, UUID uuid) throws URISyntaxException {
        return tilBehandlingPollStatus(request, uuid, Optional.empty());
    }

    public static Response tilBehandlingEllerPollStatus(HttpServletRequest request, UUID uuid, AsyncPollingStatus status) throws URISyntaxException {
        var uriBuilder = getUriBuilder(request)
            .path(BehandlingRestTjeneste.PATH_FRAGMENT)
            .queryParam("uuid", uuid);
        return buildResponse(request, status, uriBuilder.build());
    }

    private static UriBuilder getUriBuilder(HttpServletRequest request) {
        UriBuilder uriBuilder = request == null || request.getContextPath() == null ? UriBuilder.fromUri("") : UriBuilder.fromUri(URI.create(request.getContextPath()));
        Optional.ofNullable(request).map(r -> r.getServletPath()).ifPresent(c -> uriBuilder.path(c));
        return uriBuilder;
    }

    private static Response buildResponse(HttpServletRequest request, AsyncPollingStatus status, URI resultatUri) throws URISyntaxException {
        URI uri = honorXForwardedProto(request, resultatUri);
        if (status != null) {
            // sett alltid resultat-location i tilfelle timeout på klient
            status.setLocation(uri);
            return Response.status(status.getStatus().getHttpStatus()).entity(status).build();
        } else {
            return Response.seeOther(uri).build();
        }
    }

    private static URI honorXForwardedProto(HttpServletRequest request, URI location) throws URISyntaxException {
        URI newLocation = null;
        if (relativLocationAndRequestAvailable(location)) {
            String xForwardedProto = getXForwardedProtoHeader(request);

            if (mismatchedScheme(xForwardedProto, request)) {
                String path = location.toString();
                if (path.startsWith("/")) { // NOSONAR
                    path = path.substring(1); // NOSONAR
                }
                URI baseUri = new URI(request.getRequestURI());
                try {
                    URI rewritten = new URI(xForwardedProto, baseUri.getSchemeSpecificPart(), baseUri.getFragment())
                        .resolve(path);
                    LOG.info("Rewrote URI from '{}' to '{}'", location, rewritten);
                    newLocation = rewritten;
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException(e.getMessage(), e);
                }
            }
        }
        return newLocation != null ? newLocation : leggTilBaseUri(location);
    }

    private static boolean relativLocationAndRequestAvailable(URI location) {
        return location != null && !location.isAbsolute();
    }

    /**
     * @return http, https or null
     */
    private static String getXForwardedProtoHeader(HttpServletRequest httpRequest) {
        String xForwardedProto = httpRequest.getHeader("X-Forwarded-Proto");
        if ("https".equalsIgnoreCase(xForwardedProto) ||
            "http".equalsIgnoreCase(xForwardedProto)) {
            return xForwardedProto;
        }
        return null;
    }

    private static boolean mismatchedScheme(String xForwardedProto, HttpServletRequest httpRequest) {
        return xForwardedProto != null &&
            !xForwardedProto.equalsIgnoreCase(httpRequest.getScheme());
    }

    private static URI leggTilBaseUri(URI resultatUri) {
        // tvinger resultatUri til å være en absolutt URI (passer med Location Header og Location felt når kommer i payload)
        Response response = Response.noContent().location(resultatUri).build();
        return response.getLocation();
    }
}
