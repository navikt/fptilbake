package no.nav.foreldrepenger.tilbakekreving.web.app.rest;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.web.app.konfig.ApiConfig;

public final class ResourceLinks {

    private ResourceLinks() {
    }

    public static ResourceLink get(String path, String rel) {
        return get(path, rel, null);
    }

    public static ResourceLink get(String path, String rel, Object queryParams) {
        var href = addPathPrefix(path);
        var query = toQuery(queryParams);
        return ResourceLink.get(href + query, rel);
    }

    public static ResourceLink post(String path, String rel) {
        return post(path, rel, null);
    }

    public static ResourceLink post(String path, String rel, Object requestPayload) {
        var href = addPathPrefix(path);
        return ResourceLink.post(href, rel, requestPayload);
    }

    public static ResourceLink post(String path, String rel, Object requestPayload, Object queryParams) {
        var href = addPathPrefix(path);
        var query = toQuery(queryParams);
        return ResourceLink.post(href + query, rel, requestPayload);
    }

    public static String addPathPrefix(String path) {
        var applikasjon = ApplicationName.hvilkenTilbake();
        var contextPath = switch (applikasjon) {
            case FPTILBAKE -> "/fptilbake";
            case K9TILBAKE -> "/k9/tilbake";
            default ->
                    throw new IllegalStateException("applikasjonsnavn er satt til " + applikasjon + " som ikke er en støttet verdi");
        };
        var apiUri = ApiConfig.API_URI;
        return contextPath + apiUri + path;
    }

    public static String toQuery(Object queryParams) {
        if (queryParams != null) {
            var mapper = new ObjectMapper();
            var mappedQueryParams = mapper.convertValue(queryParams, UriFormat.class).toString();
            if (!mappedQueryParams.isEmpty()) {
                return "?" + mappedQueryParams;
            }
        }
        return "";
    }

    private static class UriFormat {

        private final StringBuilder builder = new StringBuilder();

        @JsonAnySetter
        public void addToUri(String name, Object property) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(name).append("=").append(property);
        }

        @Override
        public String toString() {
            return builder.toString();
        }
    }
}
