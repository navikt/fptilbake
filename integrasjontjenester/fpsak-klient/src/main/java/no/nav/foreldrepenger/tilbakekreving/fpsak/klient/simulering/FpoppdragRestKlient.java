package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.simulering;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import jakarta.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.kontrakter.simulering.resultat.request.SimuleringResultatRequest;
import no.nav.foreldrepenger.kontrakter.simulering.resultat.v1.FeilutbetaltePerioderDto;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, application = FpApplication.FPOPPDRAG)
public class FpoppdragRestKlient {

    private final RestClient restClient;
    private final RestConfig restConfig;
    private final URI target;

    public FpoppdragRestKlient() {
        this.restClient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        this.target = UriBuilder.fromUri(restConfig.fpContextPath()).path("/api/simulering/feilutbetalte-perioder").build();
    }

    public Optional<FeilutbetaltePerioderDto> hentFeilutbetaltePerioder(long fpsakBehandlingId, UUID behandlingUuid, String saksnummer) {
        var request = RestRequest.newPOSTJson(new SimuleringResultatRequest(fpsakBehandlingId, behandlingUuid, saksnummer), target, restConfig);
        return restClient.sendReturnOptional(request, FeilutbetaltePerioderDto.class);
    }
}
