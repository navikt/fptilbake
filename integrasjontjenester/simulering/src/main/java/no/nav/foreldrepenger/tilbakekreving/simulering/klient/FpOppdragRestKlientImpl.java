package no.nav.foreldrepenger.tilbakekreving.simulering.klient;

import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.SimuleringResultatDto;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URI;
import java.util.Optional;

@ApplicationScoped
public class FpOppdragRestKlientImpl implements FpOppdragRestKlient {

    private static final String FPOPPDRAG_HENT_RESULTAT = "/simulering/resultat";
    private static final String FPOPPDRAG_HENT_FEILUTBETALINGER = "/simulering/feilutbetalte-perioder";


    private OidcRestClient restClient;
    private URI uriHentResultat;
    private URI uriHentFeilutbetalinger;

    public FpOppdragRestKlientImpl() {
        //for cdi proxy
    }

    @Inject
    public FpOppdragRestKlientImpl(OidcRestClient restClient) {
        this.restClient = restClient;
        String fpoppdragBaseUrl = FpoppdragFelles.getFpoppdragBaseUrl();
        uriHentResultat = URI.create(fpoppdragBaseUrl + FPOPPDRAG_HENT_RESULTAT);
        uriHentFeilutbetalinger = URI.create(fpoppdragBaseUrl + FPOPPDRAG_HENT_FEILUTBETALINGER);
    }

    @Override
    public Optional<SimuleringResultatDto> hentResultat(Long behandlingId) {
        return restClient.postReturnsOptional(uriHentResultat, new BehandlingIdDto(behandlingId), SimuleringResultatDto.class);
    }

    @Override
    public Optional<FeilutbetaltePerioderDto> hentFeilutbetaltePerioder(Long behandlingId) {
        return restClient.postReturnsOptional(uriHentFeilutbetalinger, new BehandlingIdDto(behandlingId), FeilutbetaltePerioderDto.class);
    }
}
