package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy;

import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.nav.vedtak.mapper.json.DefaultJsonMapper.fromJson;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.kontrakter.tilbakekreving.kravgrunnlag.request.HentKravgrunnlagDetaljDto;
import no.nav.foreldrepenger.kontrakter.tilbakekreving.kravgrunnlag.respons.Kravgrunnlag431Dto;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.STS_CC, application = FpApplication.FPWSPROXY)
public class ØkonomiProxyKlient {

    private final RestClient restClient;
    private final RestConfig restConfig;
    private final URI endpointKravgrunnlag;

    public ØkonomiProxyKlient() {
        this.restClient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        this.endpointKravgrunnlag = UriBuilder.fromUri(restConfig.endpoint()).path("/tilbakekreving/kravgrunnlag").build();
    }

    public Kravgrunnlag431Dto hentKravgrunnlag(HentKravgrunnlagDetaljDto hentKravgrunnlagDetaljDto) {
        var target = UriBuilder.fromUri(endpointKravgrunnlag).build();
        var request = RestRequest.newPOSTJson(hentKravgrunnlagDetaljDto, target, restConfig);
        return handleResponse(restClient.sendReturnUnhandled(request))
                .map(r -> fromJson(r, Kravgrunnlag431Dto.class))
                .orElse(null); // TODO:
    }

    // TODO EXECPTION håndetering
    private static Optional<String> handleResponse(HttpResponse<String> response) {
        int status = response.statusCode();
        var body = response.body();
        if (status >= HTTP_OK && status < HTTP_MULT_CHOICE) {
            return body != null && !body.isEmpty() ? Optional.of(body) : Optional.empty();
        } else if (status == HTTP_FORBIDDEN) {
            throw new ManglerTilgangException("F-468816", "Mangler tilgang. Fikk http-kode 403 fra server");
        } else {
            throw new IntegrasjonException("F-468817", String.format("Uventet respons %s fra FpWsProxy. Sjekk loggen til fpwsproxy for mer info.", status));
        }
    }

}
