package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy;

import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_GONE;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_NOT_AUTHORITATIVE;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.nav.vedtak.mapper.json.DefaultJsonMapper.fromJson;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.error.FeilDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.error.FeilType;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.iverksett.TilbakekrevingVedtakDTO;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.request.AnnullerKravGrunnlagDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.request.HentKravgrunnlagDetaljDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.kravgrunnlag.respons.Kravgrunnlag431Dto;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ManglendeKravgrunnlagException;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.SperringKravgrunnlagException;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.UkjentKvitteringFraOSException;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.STS_CC, endpointProperty = "fpwsproxy.override.url", endpointDefault = "http://fpwsproxy.teamforeldrepenger/fpwsproxy")
public class ØkonomiProxyKlient {
    private static final String PATH_TILBAKEKREVING_KONTROLLER = "/tilbakekreving";

    private final RestClient restClient;
    private final RestConfig restConfig;
    private final URI endpointKravgrunnlag;
    private final URI endpointKravgrunnlagAnnuller;
    private final URI endpointIverksett;
    private final URI endpointIverksettSammenligning;

    public ØkonomiProxyKlient() {
        this.restClient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        this.endpointKravgrunnlag = UriBuilder.fromUri(restConfig.endpoint()).path(PATH_TILBAKEKREVING_KONTROLLER).path("/kravgrunnlag").build();
        this.endpointKravgrunnlagAnnuller = UriBuilder.fromUri(restConfig.endpoint()).path(PATH_TILBAKEKREVING_KONTROLLER).path("/kravgrunnlag/annuller").build();
        this.endpointIverksett = UriBuilder.fromUri(restConfig.endpoint()).path(PATH_TILBAKEKREVING_KONTROLLER).path("/tilbakekrevingsvedtak").build();
        this.endpointIverksettSammenligning = UriBuilder.fromUri(restConfig.endpoint()).path(PATH_TILBAKEKREVING_KONTROLLER).path("/tilbakekrevingsvedtak/sammenligning").build();
    }

    public void iverksettTilbakekrevingsvedtak(TilbakekrevingVedtakDTO tilbakekrevingVedtakDTO) {
        var target = UriBuilder.fromUri(endpointIverksett).build();
        var request = RestRequest.newPOSTJson(tilbakekrevingVedtakDTO, target, restConfig);
        handleIverksettVedtakRespons(restClient.sendReturnUnhandled(request));
    }

    public Kravgrunnlag431Dto hentKravgrunnlag(HentKravgrunnlagDetaljDto hentKravgrunnlagDetaljDto) {
        var target = UriBuilder.fromUri(endpointKravgrunnlag).build();
        var request = RestRequest.newPOSTJson(hentKravgrunnlagDetaljDto, target, restConfig);
        return handleKravgrunnlagResponse(restClient.sendReturnUnhandled(request))
            .map(r -> fromJson(r, Kravgrunnlag431Dto.class))
            .orElseThrow(() -> new IllegalStateException("Respons fra fpwsproxy tilsier at det er funnet et kravgrunnlag men responsen er tom. Dette må sjekkes opp i! Sjekk loggen til fpwsproxy for mer info."));
    }

    public void anullereKravgrunnlag(AnnullerKravGrunnlagDto annullerKravgrunnlagDto) {
        var target = UriBuilder.fromUri(endpointKravgrunnlagAnnuller).build();
        var putMethod = new RestRequest.Method(RestRequest.WebMethod.PUT, RestRequest.jsonPublisher(annullerKravgrunnlagDto));
        var request = RestRequest.newRequest(putMethod, target, restConfig);
        handleAnnullertKravgrunnlagResponse(restClient.sendReturnUnhandled(request));
    }

    @Deprecated
    public TilbakekrevingVedtakDtoResponsMidlertidig hentIverksettVedtakRequestXMLStrengForSammenligning(TilbakekrevingVedtakDTO tilbakekrevingVedtakDTO) {
        var target = UriBuilder.fromUri(endpointIverksettSammenligning).build();
        var request = RestRequest.newPOSTJson(tilbakekrevingVedtakDTO, target, restConfig);
        return handleIverksettVedtakResponsSammenlignign(restClient.sendReturnUnhandled(request))
            .map(r -> fromJson(r, TilbakekrevingVedtakDtoResponsMidlertidig.class))
            .orElseThrow(() -> new IllegalStateException("Tom respons tilbake! Dette virker feil?"));
    }

    @Deprecated
    private static Optional<String> handleIverksettVedtakResponsSammenlignign(HttpResponse<String> response) {
        int status = response.statusCode();
        var body = response.body();
        if (status >= HTTP_OK && status < HTTP_MULT_CHOICE) { // 2xx status
            return body != null && !body.isEmpty() ? Optional.of(body) : Optional.empty();
        } else if (status == HTTP_FORBIDDEN) {
            throw new ManglerTilgangException("F-468816", "Mangler tilgang. Fikk http-kode 403 fra server");
        } else if (status == HTTP_INTERNAL_ERROR && kvitteringInneholderUkjentFeil(body)) {
            throw new UkjentKvitteringFraOSException("FPT-539080", "Fikk feil fra OS ved iverksetting av tilbakekrevginsvedtak. Sjekk loggen til fpwsproxy for mer info.");
        } else {
            throw new IntegrasjonException("F-468817", String.format("Uventet respons %s fra FpWsProxy ved iverksetting av tilbakekrevginsvedtak. Sjekk loggen til fpwsproxy for mer info.", status));
        }
    }

    private static void handleIverksettVedtakRespons(HttpResponse<String> response) {
        int status = response.statusCode();
        if (status == HTTP_FORBIDDEN) {
            throw new ManglerTilgangException("F-468816", "Mangler tilgang. Fikk http-kode 403 fra server");
        } else if (status == HTTP_INTERNAL_ERROR && kvitteringInneholderUkjentFeil(response.body())) {
            throw new UkjentKvitteringFraOSException("FPT-539080", "Fikk feil fra OS ved iverksetting av tilbakekrevginsvedtak. Sjekk loggen til fpwsproxy for mer info.");
        } else if (status < HTTP_OK || status >= HTTP_MULT_CHOICE){
            throw new IntegrasjonException("F-468817", String.format("Uventet respons %s fra FpWsProxy ved iverksetting av tilbakekrevginsvedtak. Sjekk loggen til fpwsproxy for mer info.", status));
        }
    }


    private static void handleAnnullertKravgrunnlagResponse(HttpResponse<String> response) {
        int status = response.statusCode();
        if (status == HTTP_FORBIDDEN) {
            throw new ManglerTilgangException("F-468816", "Mangler tilgang. Fikk http-kode 403 fra server");
        }
        if (status < HTTP_OK || status >= HTTP_MULT_CHOICE) {
            throw new IntegrasjonException("F-468817", String.format("Uventet respons %s fra FpWsProxy ved annullering av kravgrunnlag. Sjekk loggen til fpwsproxy for mer info.", status));
        }
    }

    private static Optional<String> handleKravgrunnlagResponse(HttpResponse<String> response) {
        int status = response.statusCode();
        var body = response.body();
        if (status >= HTTP_OK && status < HTTP_MULT_CHOICE) { // 2xx status
            if (status == HTTP_NOT_AUTHORITATIVE && erKravgrunnlagSperret(body)) {
                throw new SperringKravgrunnlagException("FPT-539081", "Fikk feil fra OS ved henting av kravgrunnlag. Sjekk loggen til fpwsproxy for mer info.");
            }
            return body != null && !body.isEmpty() ? Optional.of(body) : Optional.empty();
        } else if (status == HTTP_FORBIDDEN) {
            throw new ManglerTilgangException("F-468816", "Mangler tilgang. Fikk http-kode 403 fra server");
        } else if (status == HTTP_GONE && finnesIkkeKravgrunnlagPåRequest(body)) {
            throw new ManglendeKravgrunnlagException("FPT-539080", "Fikk feil fra OS ved henting av kravgrunnlag. Request er logget i secure loggs til fpwsproxy.");
        } else if (status == HTTP_INTERNAL_ERROR && kvitteringInneholderUkjentFeil(body)) {
            throw new UkjentKvitteringFraOSException("FPT-539080", "Fikk feil fra OS ved henting av kravgrunnlag. Sjekk loggen til fpwsproxy for mer info.");
        } else {
            throw new IntegrasjonException("F-468817", String.format("Uventet respons %s fra FpWsProxy ved henting av kravgrunnlag. Sjekk loggen til fpwsproxy for mer info.", status));
        }
    }

    private static boolean erKravgrunnlagSperret(String body) {
        try {
            return FeilType.KRAVGRUNNLAG_SPERRET.equals(fromJson(body, FeilDto.class).type());
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean finnesIkkeKravgrunnlagPåRequest(String body) {
        try {
            return FeilType.KRAVGRUNNLAG_MANGLER.equals(fromJson(body, FeilDto.class).type());
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean kvitteringInneholderUkjentFeil(String body) {
        try {
            return FeilType.KVITTERING_UKJENT_FEIL.equals(fromJson(body, FeilDto.class).type());
        } catch (Exception e) {
            return false;
        }
    }

}
