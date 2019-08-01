package no.nav.foreldrepenger.tilbakekreving.web.app.metrics;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;

@ApplicationScoped
public class AppMetricsServlet extends MetricsServlet {

    @SuppressWarnings("unused")
	private transient MetricRegistry registry;  // NOSONAR

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
        super.doGet(req, resp);  // NOSONAR
    }

    @Inject
    public void setRegistry(MetricRegistry registry) {
        this.registry = registry; // NOSONAR
    }

}
