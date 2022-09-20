package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.simulering;

import java.net.URI;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.simulering.FeilutbetaltePerioderDto;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.CONTEXT, application = FpApplication.FPOPPDRAG)
public class FpoppdragRestKlient {

    private RestClient restClient;
    private URI target;

    protected FpoppdragRestKlient() {
        //for cdi proxy
    }

    @Inject
    public FpoppdragRestKlient(RestClient restClient) {
        this.restClient = restClient;
        var endpoint = RestConfig.contextPathFromAnnotation(FpoppdragRestKlient.class);
        this.target = UriBuilder.fromUri(endpoint).path("/api/simulering/feilutbetalte-perioder").build();
    }

    public Optional<FeilutbetaltePerioderDto> hentFeilutbetaltePerioder(long fpsakBehandlingId) {
        var request = RestRequest.newPOSTJson(new BehandlingIdDto(fpsakBehandlingId), target, FpoppdragRestKlient.class);
        return restClient.sendReturnOptional(request, FeilutbetaltePerioderDto.class);
    }

}
