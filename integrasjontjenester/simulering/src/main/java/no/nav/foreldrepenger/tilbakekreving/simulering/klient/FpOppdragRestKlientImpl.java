package no.nav.foreldrepenger.tilbakekreving.simulering.klient;

import java.net.URI;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.FeilutbetaltePerioderDto;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

@ApplicationScoped
public class FpOppdragRestKlientImpl implements FpOppdragRestKlient {

    private static final String FPOPPDRAG_HENT_FEILUTBETALINGER = "/simulering/feilutbetalte-perioder";


    private OidcRestClient restClient;
    private URI uriHentFeilutbetalinger;

    public FpOppdragRestKlientImpl() {
        //for cdi proxy
    }

    @Inject
    public FpOppdragRestKlientImpl(OidcRestClient restClient) {
        this.restClient = restClient;
        String fpoppdragBaseUrl = FpoppdragFelles.getFpoppdragBaseUrl();
        uriHentFeilutbetalinger = URI.create(fpoppdragBaseUrl + FPOPPDRAG_HENT_FEILUTBETALINGER);
    }

    @Override
    public Optional<FeilutbetaltePerioderDto> hentFeilutbetaltePerioder(Long behandlingId) {
        return restClient.postReturnsOptional(uriHentFeilutbetalinger, new BehandlingIdDto(behandlingId), FeilutbetaltePerioderDto.class);
    }
}
