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

import no.nav.foreldrepenger.kontrakter.tilbakekreving.kravgrunnlag.request.HentKravgrunnlagDetaljDto;
import no.nav.foreldrepenger.kontrakter.tilbakekreving.kravgrunnlag.respons.Kravgrunnlag431Dto;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.ManglendeKravgrunnlagException;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.SperringKravgrunnlagException;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.UkjentOppdragssystemException;
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
                .orElseThrow(() -> new IllegalStateException("Respons fra fpwsproxy tilsier at det er funnet et kravgrunnlag men responsen er tom. Dette må sjekkes opp i! Sjekk loggen til fpwsproxy for mer info."));
    }

    private static Optional<String> handleResponse(HttpResponse<String> response) {
        int status = response.statusCode();
        var body = response.body();
        if (status >= HTTP_OK && status < HTTP_MULT_CHOICE) {
            if (status == HTTP_NOT_AUTHORITATIVE && erKravgrunnlagSperret(body)) {
                throw new SperringKravgrunnlagException("FPT-539081", "Fikk feil fra OS ved henting av kravgrunnlag. Sjekk loggen til fpwsproxy for mer info.");
            }
            return body != null && !body.isEmpty() ? Optional.of(body) : Optional.empty();
        } else if (status == HTTP_FORBIDDEN) {
            throw new ManglerTilgangException("F-468816", "Mangler tilgang. Fikk http-kode 403 fra server");
        } else if (status == HTTP_GONE && finnesIkkeKravgrunnlagPåRequest(body)) {
            throw new ManglendeKravgrunnlagException("FPT-539080", "Fikk feil fra OS ved henting av kravgrunnlag. Request er logget i secure loggs til fpwsproxy.");
        } else if (status == HTTP_INTERNAL_ERROR && kvitteringInneholderUkjentFeil(body)) {
            throw new UkjentOppdragssystemException("FPT-539080", "Fikk feil fra OS ved henting av kravgrunnlag. Sjekk loggen til fpwsproxy for mer info.");
        } else {
            throw new IntegrasjonException("F-468817", String.format("Uventet respons %s fra FpWsProxy. Sjekk loggen til fpwsproxy for mer info.", status));
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
            return FeilType.KRAVGRUNNLAG_UKJENT_FEIL.equals(fromJson(body, FeilDto.class).type());
        } catch (Exception e) {
            return false;
        }
    }

    // TODO: Konsolider FeilDto fra web modul. Flytt til felles? kontrakter? egen FeilDto for fpwsproxy integrasjon?
    private record FeilDto(FeilType type) {
    }

    private enum FeilType {
        MANGLER_TILGANG_FEIL,
        TOMT_RESULTAT_FEIL,
        OPPDRAG_FORVENTET_NEDETID,
        KRAVGRUNNLAG_MANGLER,
        KRAVGRUNNLAG_SPERRET,
        KRAVGRUNNLAG_UKJENT_FEIL,
        GENERELL_FEIL,
    }

}
