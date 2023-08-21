package no.nav.foreldrepenger.tilbakekreving.web.app.metrics;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.common.TextFormat;
import io.swagger.v3.oas.annotations.Operation;
import no.nav.vedtak.felles.prosesstask.api.TaskMonitor;

@Path("/metrics")
@ApplicationScoped
public class PrometheusRestService {

    private static final Gauge TASK_GAUGE = Gauge.build()
        .name("prosesstask_antall")
        .labelNames("status")
        .help("No tasks w/status.")
        .register();

    @GET
    @Operation(tags = "metrics", hidden = true)
    @Path("/prometheus")
    public Response prometheus() {
        TaskMonitor.monitoredStatuses().forEach(s -> TASK_GAUGE.labels(TaskMonitor.statusLabel(s)).set(TaskMonitor.TASK_GAUGES.get(s).get()));
        try (final Writer writer = new StringWriter()) {
            TextFormat.writeFormat(TextFormat.CONTENT_TYPE_004, writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
            return Response.ok().encoding("UTF-8").entity(writer.toString()).header("content-type", TextFormat.CONTENT_TYPE_004).build();
        } catch (IOException e) {
            //TODO logg?
        }

        return null;
    }
}
