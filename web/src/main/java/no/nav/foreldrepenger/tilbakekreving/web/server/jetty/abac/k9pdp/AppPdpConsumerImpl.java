package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.xacml.XacmlRequest;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.xacml.XacmlResponse;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ApplicationScoped
public class AppPdpConsumerImpl {

    private static final String MEDIA_TYPE = "application/xacml+json";
    private static final Logger LOG = LoggerFactory.getLogger(AppPdpConsumerImpl.class);

    private HttpClient client;
    private ObjectReader reader;

    private URI pdpUrl;
    private String basicCredentials;

    AppPdpConsumerImpl() {
    } // CDI

    @Inject
    public AppPdpConsumerImpl(@KonfigVerdi("systembruker.username") String bruker,
                              @KonfigVerdi("systembruker.password") String passord,
                              @KonfigVerdi(value = "abac.pdp.endpoint.url", defaultVerdi = "http://abac-foreldrepenger.teamabac/application/authorize") String pdpUrl) {
        this.pdpUrl = URI.create(pdpUrl);
        this.basicCredentials = basicCredentials(bruker, passord);
        // TODO - vurder om bør settes static final?
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).proxy(HttpClient.Builder.NO_PROXY).build();
        this.reader = DefaultJsonMapper.getObjectMapper().readerFor(XacmlResponse.class);
    }

    public XacmlResponse evaluate(XacmlRequest xacmlRequest) {
        var request = HttpRequest.newBuilder()
            .header("Authorization", basicCredentials)
            .header("Content-type", MEDIA_TYPE)
            .timeout(Duration.ofSeconds(5))
            .uri(pdpUrl)
            .POST(HttpRequest.BodyPublishers.ofString(DefaultJsonMapper.toJson(xacmlRequest), UTF_8))
            .build();

        // Enkel retry
        int i = 2;
        while (i-- > 0) {
            try {
                return send(request);
            } catch (IntegrasjonException e) {
                LOG.trace("F-157387 IntegrasjonException ved kall {} til PDP", 2 - i, e);
            }
        }
        return send(request);
    }

    private XacmlResponse send(HttpRequest request) {
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString(UTF_8));
            if (response != null && response.statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new ManglerTilgangException("F-157388", "Ingen tilgang fra PDP");
            }
            if (response == null || response.body() == null) {
                LOG.info("ingen response fra PDP status = {}", response == null ? "null" : response.statusCode());
                throw new IntegrasjonException("F-157386", "Kunne ikke hente svar fra PDP");
            }
            return reader.readValue(response.body(), XacmlResponse.class);
        } catch (JsonProcessingException e) {
            throw new IntegrasjonException("F-208314", "Kunne ikke deserialisere objekt til JSON", e);
        } catch (IOException e) {
            throw new IntegrasjonException("F-091324", "Uventet IO-exception mot PDP", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IntegrasjonException("F-432938", "InterruptedException ved kall mot PDP", e);
        }
    }

    private static String basicCredentials(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString(String.format("%s:%s", username, password).getBytes(UTF_8));
    }

}
