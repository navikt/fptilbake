package no.nav.foreldrepenger.tilbakekreving.sokos;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static no.nav.vedtak.mapper.json.DefaultJsonMapper.fromJson;

import java.net.URI;
import java.net.http.HttpResponse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, endpointProperty = "sokos.base.url",
    endpointDefault = "https://sokos-os-ekstern-api.intern.nav.no/api/v1/tilbakekreving",
    scopesProperty = "sokos.scopes", scopesDefault = "api://prod-gcp.okonomi.sokos-os-ekstern-api/.default")
public class SokosTilbakekrevingKlient {
    private static final String KODE_403_FRA_SERVER = "Mangler tilgang. Fikk http-kode 403 fra server";

    private static final Logger LOG = LoggerFactory.getLogger(SokosTilbakekrevingKlient.class);

    private final RestClient restClient;
    private final RestConfig restConfig;
    private final URI endpointKravgrunnlag;
    private final URI endpointKravgrunnlagAnnuller;
    private final URI endpointIverksett;

    public SokosTilbakekrevingKlient() {
        this.restClient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        this.endpointKravgrunnlag = UriBuilder.fromUri(restConfig.endpoint()).path("/kravgrunnlag/detaljer").build();
        this.endpointKravgrunnlagAnnuller = UriBuilder.fromUri(restConfig.endpoint()).path("/kravgrunnlag/annuller").build();
        this.endpointIverksett = UriBuilder.fromUri(restConfig.endpoint()).path("/vedtak").build();
        LOG.info("proxy klient konfigurasjon: endepunkt {}, scopes {}, token config {}", restConfig.endpoint(), restConfig.scopes(), restConfig.tokenConfig());
    }

    public void iverksettTilbakekrevingsvedtak(TilbakekrevingsvedtakRequest tilbakekrevingVedtakDTO) {
        var target = UriBuilder.fromUri(endpointIverksett).build();
        var request = RestRequest.newPOSTJson(tilbakekrevingVedtakDTO, target, restConfig);
        var response = restClient.sendReturnUnhandled(request);
        handleIverksettVedtakRespons(response);
        validerKvittering(fromJson(response.body(), TilbakekrevingsvedtakResponse.class));
    }

    public HentKravgrunnlagDetaljerResponse hentKravgrunnlag(HentKravgrunnlagDetaljerRequest hentKravgrunnlagDetaljDto) {
        var target = UriBuilder.fromUri(endpointKravgrunnlag).build();
        var request = RestRequest.newPOSTJson(hentKravgrunnlagDetaljDto, target, restConfig);
        var response = restClient.sendReturnUnhandled(request);
        handleKravgrunnlagResponse(response);
        return fromJson(response.body(), HentKravgrunnlagDetaljerResponse.class);
    }

    public void anullerKravgrunnlag(KravgrunnlagAnnulerRequest annullerKravgrunnlagDto) {
        var target = UriBuilder.fromUri(endpointKravgrunnlagAnnuller).build();
        var request = RestRequest.newPOSTJson(annullerKravgrunnlagDto, target, restConfig);
        var response = restClient.sendReturnUnhandled(request);
        handleAnnullertKravgrunnlagResponse(response);
        validerAnnullering(fromJson(response.body(), KravgrunnlagAnnulerResponse.class));
    }

    private static void handleIverksettVedtakRespons(HttpResponse<String> response) {
        var status = response.statusCode();
        if (status == HTTP_FORBIDDEN) {
            throw new ManglerTilgangException("F-468816", KODE_403_FRA_SERVER);
        } else if (status == HTTP_UNAUTHORIZED) {
            throw new ManglerTilgangException("F-468816", "Mangler tilgang. Fikk http-kode 401 fra Sokos");
        } else if (status < HTTP_OK || status >= HTTP_MULT_CHOICE){
            throw new IntegrasjonException("F-468617", String.format("Uventet HTTP-status %s fra Sokos ved iverksetting av tilbakekrevingsvedtak.", status));
        }
    }

    static void validerKvittering(TilbakekrevingsvedtakResponse kvittering) {
        var status = kvittering != null ? kvittering.status() : null;
        if (status == null || status != 0 && status != 4) {
            throw new IntegrasjonException("F-468617", "Sokos avviste tilbakekrevingsvedtak med status " + status + ". Sjekk loggen til Sokos.");
        }
    }

    static void validerAnnullering(KravgrunnlagAnnulerResponse kvittering) {
        var status = kvittering != null ? kvittering.status() : null;
        if (status == null || status != 0 && status != 4) {
            throw new IntegrasjonException("F-468817", "Sokos avviste annullering av kravgrunnlag med status " + status + ". Sjekk loggen til Sokos.");
        }
    }

    private static void handleAnnullertKravgrunnlagResponse(HttpResponse<String> response) {
        var status = response.statusCode();
        if (status == HTTP_FORBIDDEN) {
            throw new ManglerTilgangException("F-468916", KODE_403_FRA_SERVER);
        }
        if (status == HTTP_UNAUTHORIZED) {
            throw new ManglerTilgangException("F-468916", "Mangler tilgang. Fikk http-kode 401 fra Sokos");
        }
        if (status < HTTP_OK || status >= HTTP_MULT_CHOICE) {
            throw new IntegrasjonException("F-468817", String.format("Uventet HTTP-status %s fra Sokos ved annullering av kravgrunnlag.", status));
        }
    }

    static void handleKravgrunnlagResponse(HttpResponse<String> response) {
        var status = response.statusCode();
        if (status == HTTP_FORBIDDEN) {
            throw new ManglerTilgangException("F-468616", KODE_403_FRA_SERVER);
        }
        if (status == HTTP_UNAUTHORIZED) {
            throw new ManglerTilgangException("F-468616", "Mangler tilgang. Fikk http-kode 401 fra Sokos");
        }
        if (status == HTTP_BAD_REQUEST && response.body() != null && !response.body().isBlank()) {
            var error = fromJson(response.body(), ApiError.class);
            throw new SokosKravgrunnlagException(error != null ? error.message() : null);
        }
        if (status < HTTP_OK || status >= HTTP_MULT_CHOICE) {
            throw new IntegrasjonException("F-468817", String.format("Uventet HTTP-status %s fra Sokos ved henting av kravgrunnlag.", status));
        }
    }

}
