package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.caching;

import java.io.IOException;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;

/**
 * Setter Cache-Control response header på GET metoder annotert med @Cache-Control annotasjon.
 */
public class CacheControlFilter implements ContainerResponseFilter {
    private final int maxAge;
    private final boolean noStore;
    private final boolean mustRevalidate;
    private final boolean isPrivate;
    private final boolean noTransform;
    private final boolean immutable;
    private final int staleWhileRevalidate;
    private final int staleIfError;


    public CacheControlFilter(
        final int maxAge,
        final boolean noStore,
        final boolean mustRevalidate,
        final boolean isPrivate,
        final boolean noTransform,
        final boolean immutable,
        final int staleWhileRevalidate,
        final int staleIfError) {
        this.maxAge = maxAge;
        this.noStore = noStore;
        this.mustRevalidate = mustRevalidate;
        this.isPrivate = isPrivate;
        this.noTransform = noTransform;
        this.immutable = immutable;
        this.staleWhileRevalidate = staleWhileRevalidate;
        this.staleIfError = staleIfError;
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        if(req.getMethod().equals("GET")) {
            final ExtendedCacheControl cc = new ExtendedCacheControl();
            cc.setMaxAge(this.maxAge);
            cc.setNoStore(this.noStore);
            cc.setMustRevalidate(this.mustRevalidate);
            cc.setPrivate(this.isPrivate);
            cc.setNoTransform(this.noTransform);
            cc.setImmutable(this.immutable);
            cc.setStaleWhileRevalidate(this.staleWhileRevalidate);
            cc.setStaleIfError(this.staleIfError);
            res.getHeaders().add("Cache-Control", cc);
        }
    }
}
