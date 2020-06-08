package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.simulering;

import java.net.URI;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.simulering.FeilutbetaltePerioderDto;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

@ApplicationScoped
public class FpoppdragRestKlient {

    private static final String FPOPPDRAG_HENT_FEILUTBETALINGER = "/simulering/feilutbetalte-perioder";


    private OidcRestClient restClient;
    private URI uriHentFeilutbetalinger;

    public FpoppdragRestKlient() {
        //for cdi proxy
    }

    @Inject
    public FpoppdragRestKlient(OidcRestClient restClient) {
        this.restClient = restClient;
        String fpoppdragBaseUrl = FpoppdragFelles.getFpoppdragBaseUrl();
        uriHentFeilutbetalinger = URI.create(fpoppdragBaseUrl + FPOPPDRAG_HENT_FEILUTBETALINGER);
    }

    public Optional<FeilutbetaltePerioderDto> hentFeilutbetaltePerioder(long fpsakBehandlingId) {
        return restClient.postReturnsOptional(uriHentFeilutbetalinger, new BehandlingIdDto(fpsakBehandlingId), FeilutbetaltePerioderDto.class);
    }
}
