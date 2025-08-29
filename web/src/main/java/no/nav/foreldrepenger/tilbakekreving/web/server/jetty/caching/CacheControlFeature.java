package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.caching;

import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CacheControlFeature implements DynamicFeature {
    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext featureContext) {
        final CacheControl annotation = resourceInfo.getResourceMethod().getAnnotation(CacheControl.class);
        if(annotation == null) return;
        final CacheControlFilter filter = new CacheControlFilter(
            annotation.maxAge(),
            annotation.noStore(),
            annotation.mustRevalidate(),
            annotation.isPrivate(),
            annotation.noTransform(),
            annotation.immutable(),
            annotation.staleWhileRevalidate(),
            annotation.staleIfError()
        );
        featureContext.register(filter);
    }
}
