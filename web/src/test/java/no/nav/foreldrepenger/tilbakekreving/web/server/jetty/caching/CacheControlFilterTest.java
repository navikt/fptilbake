package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.caching;

import jakarta.ws.rs.container.ContainerRequestContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.container.ContainerResponseContext;

import jakarta.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.message.internal.HeaderUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class CacheControlFilterTest {
    @Test
    public void skal_sette_response_cache_header() throws IOException {
        final var ccf = new CacheControlFilter(
            10,
            false,
            true,
            true,
            true,
            true,
            5,
            3
        );
        final ContainerRequestContext req = mock(ContainerRequestContext.class);
        when(req.getMethod()).thenReturn("GET");
        final ContainerResponseContext res = mock(ContainerResponseContext.class);
        final MultivaluedMap<String, Object> headers = HeaderUtils.createOutbound();
        when(res.getHeaders()).thenReturn(headers);

        ccf.filter(req, res);
        final var cclist = res.getHeaders().get("Cache-Control");
        assertThat(cclist).hasSize(1);
        assertThat(cclist.getFirst()).isInstanceOf(ExtendedCacheControl.class);
        final var ecc = (ExtendedCacheControl) cclist.getFirst();
        assertThat(ecc.getMaxAge()).isEqualTo(10);
        assertThat(ecc.isNoStore()).isFalse();
        assertThat(ecc.isMustRevalidate()).isTrue();
        assertThat(ecc.isPrivate()).isTrue();
        assertThat(ecc.isNoTransform()).isTrue();
        assertThat(ecc.isImmutable()).isTrue();
        assertThat(ecc.getStaleWhileRevalidate()).isEqualTo(5);
        assertThat(ecc.getStaleIfError()).isEqualTo(3);
    }
}
