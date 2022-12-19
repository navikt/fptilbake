package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.sikkerhet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

import no.nav.foreldrepenger.konfig.Environment;

@WebFilter(urlPatterns = "/*")
public class SecurityFilter implements Filter {

    private static final String LBURL = Environment.current().getProperty("loadbalancer.url", "");

    @Override
    public void init(FilterConfig filterConfig) {
        //Ingenting å gjøre.
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletResponse instanceof HttpServletResponse response) {
            response.setHeader("Content-Security-Policy", String.format("script-src %s", LBURL));
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
