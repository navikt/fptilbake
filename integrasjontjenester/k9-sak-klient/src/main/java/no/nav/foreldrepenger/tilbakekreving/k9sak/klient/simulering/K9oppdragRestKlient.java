package no.nav.foreldrepenger.tilbakekreving.k9sak.klient.simulering;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.simulering.FeilutbetaltePerioderDto;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@RestClientConfig(tokenConfig = TokenFlow.ADAPTIVE, scopesProperty = "k9oppdrag.scopes", scopesDefault = "api://prod-fss.k9saksbehandling.k9-oppdrag/.default",
    endpointDefault = "http://k9-oppdrag", endpointProperty = "k9oppdrag.url")
public class K9oppdragRestKlient {

    private static final Logger LOG = LoggerFactory.getLogger(K9oppdragRestKlient.class);

    private final RestClient restClient;
    private final RestConfig restConfig;

    private static final String K9_OPPDRAG_BASE_URL = "http://k9-oppdrag/k9/oppdrag/api";
    private static final String K9_OPPDRAG_OVERRIDE_URL = "k9oppdrag.override.url";
    private static final String K9_OPPDRAG_HENT_FEILUTBETALINGER = "/simulering/feilutbetalte-perioder";

    public K9oppdragRestKlient() {
        this.restClient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
    }

    public Optional<FeilutbetaltePerioderDto> hentFeilutbetaltePerioder(UUID uuid) {
        URI hentFeilutbetalingerUri = URI.create(getK9OoppdragBaseUri() + K9_OPPDRAG_HENT_FEILUTBETALINGER);
        var request = RestRequest.newPOSTJson(uuid, hentFeilutbetalingerUri, restConfig);
        return restClient.sendReturnOptional(request, FeilutbetaltePerioderDto.class);
    }

    private URI getK9OoppdragBaseUri() {
        String overrideUrl = Environment.current().getProperty(K9_OPPDRAG_OVERRIDE_URL);
        if (overrideUrl != null && !overrideUrl.isEmpty()) {
            LOG.info("Overstyrte URL til k9-oppdrag til {}", overrideUrl);
            return URI.create(overrideUrl);
        } else {
            return URI.create(K9_OPPDRAG_BASE_URL);
        }

    }

}
