package no.nav.foreldrepenger.tilbakekreving.web.app.sikkerhetsfilter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

import no.nav.vedtak.isso.config.ServerInfo;

@WebFilter(urlPatterns = "/*")
public class SecurityFilter implements Filter {

    /**
     * <p>
     * Dette er en base64-encodet sha256-hash av innholdet i inline script-tag i index.htm fra swagger-
     * </p>
     * <p>
     * Uten denne vil ikke swagger kjøres i nettleseren.
     * </p>
     * <p>
     * Vedlikehold: Når innholdet i inline script-tag i index.html fra swagger endres, må denne også endres.
     * Browsere oppfordres i spesifikasjon for CSP 2.0 til å opplyse hva riktig hash er,
     * det gjør det enklelt å finne ny riktig verdi dersom innholdet endres (testet i Chrome).
     * 
     * Tips: Åpne swagger-siden i Chrome, inspiser siden, vis console. Sørg for at header med HASH er satt og 
     * da skal man få en console-feil med forventet hash
     * </p>
     */
    private static final String HASH_FOR_SWAGGER_INLINE_JAVASCRIPT = "sha256-jZ9Y3HJCuOrdIL95F0ngZyJyP3DaGenAOpFYW3rvs5E=";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //Ingenting å gjøre.
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletResponse instanceof HttpServletResponse) {
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            response.setHeader("Content-Security-Policy", String.format("script-src %s '%s'", ServerInfo.instance().getSchemeHostPort(), HASH_FOR_SWAGGER_INLINE_JAVASCRIPT)); // NOSONAR her har vi full kontroll
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("X-XSS-Protection", "1;mode=block");
            response.setHeader("Strict-Transport-Security", "max-age=31536000");
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        //Ingenting å gjøre.
    }
}
