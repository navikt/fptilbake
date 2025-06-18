package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto.SaksinformasjonTilgangskontrollInputDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto.resultat.Tilgangsbeslutning;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@RestClientConfig(tokenConfig = TokenFlow.ADAPTIVE, scopesProperty = "sif.abac.pdp.scope", scopesDefault = "api://prod-fss.k9saksbehandling.sif-abac-pdp/.default",
    endpointDefault = "http://sif-abac-pdp/sif/sif-abac-pdp/api/tilgangskontroll/v2/k9/saksinformasjon-uten-personer", endpointProperty = "sif.abac.pdp.url")
public class SifAbacPdpRestKlient {

    private final static Logger LOGGER = LoggerFactory.getLogger(SifAbacPdpRestKlient.class);

    private final RestClient restClient;
    private final RestConfig restConfig;

    public SifAbacPdpRestKlient() {
        this.restClient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
    }

    public Tilgangsbeslutning sjekkTilgangForInnloggetBruker(SaksinformasjonTilgangskontrollInputDto input) {
        if (Environment.current().isDev()){
            ObjectMapper om = no.nav.vedtak.mapper.json.DefaultJsonMapper.getObjectMapper();
            try {
                LOGGER.info("saksinformasjon: {}", om.writeValueAsString(input));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        if (input.isSaksinformasjonMangler()){
            throw new IllegalArgumentException("saksinformasjon er påkrevet for UPDATE på FAGSAK, men saksinformasjon er null");
        }
        if (input.isSaksnummerMangler()){
            throw new IllegalArgumentException("saksnummer er påkrevet for FAGSAK, men er null");
        }
        var request = RestRequest.newPOSTJson(input, restConfig.endpoint(), restConfig);
        return restClient.send(request, Tilgangsbeslutning.class);
    }

}
