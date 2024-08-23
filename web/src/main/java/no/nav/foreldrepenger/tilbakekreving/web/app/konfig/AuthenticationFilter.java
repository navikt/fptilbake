package no.nav.foreldrepenger.tilbakekreving.web.app.konfig;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.ext.Provider;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.JettyServer;
import no.nav.vedtak.sikkerhet.jaxrs.AuthenticationFilterDelegate;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;

import java.util.Optional;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String K9_ID_TOKEN_COOKIE_NAME = "ID_token";

    @Context
    private ResourceInfo resourceinfo;

    public AuthenticationFilter() {
        // Ingenting
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) {
        AuthenticationFilterDelegate.fjernKontekst();
    }

    @Override
    public void filter(ContainerRequestContext req) {
        AuthenticationFilterDelegate.validerSettKontekst(resourceinfo, req, () -> getToken(req)); // Særordning for K9-tilbake
    }

    private static Optional<TokenString> getToken(ContainerRequestContext request) {
        return AuthenticationFilterDelegate.getTokenFromHeader(request).or(() -> getCookieToken(request));
    }

    private static Optional<TokenString> getCookieToken(ContainerRequestContext request) {
        var cookiePath = JettyServer.getCookiePath();
        var idTokenCookie = Optional.ofNullable(request.getCookies()).map(c -> c.get(K9_ID_TOKEN_COOKIE_NAME));
        return idTokenCookie.filter(c -> cookiePath != null && cookiePath.equalsIgnoreCase(c.getPath()))
            .or(() -> idTokenCookie)
            .map(Cookie::getValue)
            .map(TokenString::new);
    }

}
