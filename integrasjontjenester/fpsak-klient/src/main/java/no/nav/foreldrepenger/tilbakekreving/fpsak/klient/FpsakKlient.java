package no.nav.foreldrepenger.tilbakekreving.fpsak.klient;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.BehandlingResourceLinkDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.PersonopplysningDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.VarseltekstDto;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.PropertyUtil;

@ApplicationScoped
public class FpsakKlient {

    private static final Logger logger = LoggerFactory.getLogger(FpsakKlient.class);

    private static final String FPSAK_BASE_URL = "http://fpsak";
    private static final String FPSAK_OVERRIDE_URL = "fpsak.override.url";
    private static final String FPSAK_API_PATH = "/fpsak/api";

    private static final String BEHANDLING_EP = "/behandling/backend-root";
    private static final String BEHANDLING_ALLE_EP = "/behandlinger/alle";

    private static final String FILTER_REL_PERSONOPPLYSNINGER = "soeker-personopplysninger";
    private static final String FILTER_REL_TILBAKEKREVINGVALG = "tilbakekreving-valg";
    private static final String FILTER_REL_VARSELFRITEKST = "tilbakekrevingsvarsel-fritekst";

    private static final String PARAM_NAME_BEHANDLING_UUID = "uuid";
    private static final String PARAM_NAME_SAKSNUMMER = "saksnummer";

    private OidcRestClient restClient;

    FpsakKlient() {
        // CDI
    }

    @Inject
    public FpsakKlient(OidcRestClient restClient) {
        this.restClient = restClient;
    }

    public boolean finnesBehandlingIFpsak(String saksnummer, Long eksternBehandlingId) {
        List<EksternBehandlingsinfoDto> eksternBehandlinger = hentBehandlingForSaksnummer(saksnummer);
        if (!eksternBehandlinger.isEmpty()) {
            return eksternBehandlinger.stream()
                .anyMatch(eksternBehandlingsinfoDto -> eksternBehandlingId.equals(eksternBehandlingsinfoDto.getId()));
        }
        return false;
    }

    public Optional<EksternBehandlingsinfoDto> hentBehandlingsinfo(UUID eksternUuid) {
        Optional<EksternBehandlingsinfoDto> eksternBehandlingsinfoDtoOptional = hentBehandling(eksternUuid);

        eksternBehandlingsinfoDtoOptional.ifPresent(eksternBehandlingsinfo -> {
            eksternBehandlingsinfo.getLinks().forEach(personopplysningRessursLink -> {
                final Optional<PersonopplysningDto> personopplysningDto = hentPersonopplysninger(personopplysningRessursLink);
                personopplysningDto.ifPresent(eksternBehandlingsinfo::setPersonopplysningDto);
            });

            eksternBehandlingsinfo.getLinks().forEach(varselRessursLink -> {
                final Optional<VarseltekstDto> varseltekstDto = hentVarseltekst(varselRessursLink);
                varseltekstDto.ifPresent(varselTekst -> eksternBehandlingsinfo.setVarseltekst(varselTekst.getVarseltekst()));
            });

        });
        return eksternBehandlingsinfoDtoOptional;
    }

    public Optional<EksternBehandlingsinfoDto> hentBehandling(UUID eksternUuid) {
        URI endpoint = createUri(BEHANDLING_EP, PARAM_NAME_BEHANDLING_UUID, eksternUuid.toString());
        return get(endpoint, EksternBehandlingsinfoDto.class);
    }

    public Optional<TilbakekrevingValgDto> hentTilbakekrevingValg(String eksternUuid) {
        Optional<EksternBehandlingsinfoDto> eksternBehandlingsinfoDtoOptional = hentBehandling(UUID.fromString(eksternUuid));
        if (eksternBehandlingsinfoDtoOptional.isPresent()) {
            Optional<BehandlingResourceLinkDto> ressursLink = eksternBehandlingsinfoDtoOptional.get().getLinks().stream()
                .filter(resourceLink -> FILTER_REL_TILBAKEKREVINGVALG.equals(resourceLink.getRel())).findAny();
            if (ressursLink.isPresent()) {
                return hentTilbakekrevingValg(ressursLink.get());
            }
        }
        return Optional.empty();
    }


    private List<EksternBehandlingsinfoDto> hentBehandlingForSaksnummer(String saksnummer) {
        URI endpoint = createUri(BEHANDLING_ALLE_EP, PARAM_NAME_SAKSNUMMER, saksnummer);
        JsonNode jsonNode = restClient.get(endpoint, JsonNode.class);
        return lesResponsFraJsonNode(saksnummer, jsonNode);
    }

    private List<EksternBehandlingsinfoDto> lesResponsFraJsonNode(String saksnummer, JsonNode jsonNode) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(new TypeReference<List<EksternBehandlingsinfoDto>>() {
        });
        try {
            return reader.readValue(jsonNode);
        } catch (IOException e) {
            throw FpsakKlientFeil.FACTORY.lesResponsFeil(saksnummer,e).toException();
        }
    }

    private Optional<PersonopplysningDto> hentPersonopplysninger(BehandlingResourceLinkDto resourceLink) {
        if (FILTER_REL_PERSONOPPLYSNINGER.equals(resourceLink.getRel())) {
            URI endpoint = URI.create(baseUri() + resourceLink.getHref());
            return get(endpoint, PersonopplysningDto.class);
        }
        return Optional.empty();
    }

    private Optional<VarseltekstDto> hentVarseltekst(BehandlingResourceLinkDto resourceLink) {
        if (FILTER_REL_VARSELFRITEKST.equals(resourceLink.getRel())) {
            URI endpoint = URI.create(baseUri() + resourceLink.getHref());
            return get(endpoint, VarseltekstDto.class);
        }
        return Optional.empty();
    }

    private Optional<TilbakekrevingValgDto> hentTilbakekrevingValg(BehandlingResourceLinkDto resourceLink) {
        URI endpoint = URI.create(baseUri() + resourceLink.getHref());
        return get(endpoint, TilbakekrevingValgDto.class);
    }


    private <T> Optional<T> get(URI endpoint, Class<T> tClass) {
        return restClient.getReturnsOptional(endpoint, tClass);
    }

    private URI createUri(String endpoint, String paramName, String paramValue) {
        UriBuilder builder = UriBuilder.fromUri(apiUri())
            .path(endpoint);

        if (notNullOrEmpty(paramName) && notNullOrEmpty(paramValue)) {
            builder.queryParam(paramName, paramValue);
        }
        return builder.build();
    }

    private URI apiUri() {
        return UriBuilder.fromUri(baseUri()).path(FPSAK_API_PATH).build();
    }

    private URI baseUri() {
        String override = PropertyUtil.getProperty(FPSAK_OVERRIDE_URL);
        if (override != null && !override.isEmpty()) {
            logger.info("Overstyrer fpsak base URL med {}", override);
            return URI.create(override);
        }
        return URI.create(FPSAK_BASE_URL);
    }

    private boolean notNullOrEmpty(String str) {
        return (str != null && !str.isEmpty());
    }

}
