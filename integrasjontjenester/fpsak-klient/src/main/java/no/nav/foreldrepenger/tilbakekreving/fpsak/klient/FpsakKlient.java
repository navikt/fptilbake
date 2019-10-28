package no.nav.foreldrepenger.tilbakekreving.fpsak.klient;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.BehandlingResourceLinkDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.FagsakDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.PersonopplysningDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.SoknadDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.TilbakekrevingValgDto;
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

    private static final String PARAM_NAME_BEHANDLING_UUID = "uuid";
    private static final String PARAM_NAME_SAKSNUMMER = "saksnummer";

    private OidcRestClient restClient;

    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

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

    public SamletEksternBehandlingInfo hentBehandlingsinfo(UUID eksternUuid, Tillegsinformasjon... tillegsinformasjon) {
        List<Tillegsinformasjon> ekstrainfo = Arrays.asList(tillegsinformasjon);
        SamletEksternBehandlingInfo.Builder builder = SamletEksternBehandlingInfo.builder(ekstrainfo);
        Optional<EksternBehandlingsinfoDto> eksternBehandlingsinfoDtoOptional = hentBehandling(eksternUuid);

        eksternBehandlingsinfoDtoOptional.ifPresent(eksternBehandlingsinfo -> {
            builder.setGrunninformasjon(eksternBehandlingsinfo);
            List<BehandlingResourceLinkDto> lenker = eksternBehandlingsinfo.getLinks();
            for (BehandlingResourceLinkDto lenke : lenker) {
                if (ekstrainfo.contains(Tillegsinformasjon.PERSONOPPLYSNINGER) && lenke.getRel().equals(Tillegsinformasjon.PERSONOPPLYSNINGER.getFpsakRelasjonNavn())) {
                    builder.setPersonopplysninger(hentPersonopplysninger(lenke));
                }
                if (ekstrainfo.contains(Tillegsinformasjon.SØKNAD) && lenke.getRel().equals(Tillegsinformasjon.SØKNAD.getFpsakRelasjonNavn())) {
                    builder.setFamiliehendelse(hentSøknad(lenke));
                }
                if (ekstrainfo.contains(Tillegsinformasjon.TILBAKEKREVINGSVALG) && lenke.getRel().equals(Tillegsinformasjon.TILBAKEKREVINGSVALG.getFpsakRelasjonNavn())) {
                    hentTilbakekrevingValg(lenke).ifPresent(builder::setTilbakekrevingvalg);
                }
                if (ekstrainfo.contains(Tillegsinformasjon.FAGSAK) && lenke.getRel().equals(Tillegsinformasjon.FAGSAK.getFpsakRelasjonNavn())) {
                    builder.setFagsak(hentFagsak(lenke));
                }
            }
        });
        return builder.build();
    }

    public Optional<EksternBehandlingsinfoDto> hentBehandling(UUID eksternUuid) {
        URI endpoint = createUri(BEHANDLING_EP, PARAM_NAME_BEHANDLING_UUID, eksternUuid.toString());
        return get(endpoint, EksternBehandlingsinfoDto.class);
    }

    public Optional<TilbakekrevingValgDto> hentTilbakekrevingValg(UUID eksternUuid) {
        Optional<EksternBehandlingsinfoDto> eksternBehandlingsinfoDtoOptional = hentBehandling(eksternUuid);
        if (eksternBehandlingsinfoDtoOptional.isPresent()) {
            Optional<BehandlingResourceLinkDto> ressursLink = eksternBehandlingsinfoDtoOptional.get().getLinks().stream()
                .filter(resourceLink -> Tillegsinformasjon.TILBAKEKREVINGSVALG.getFpsakRelasjonNavn().equals(resourceLink.getRel())).findAny();
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
        ObjectReader reader = mapper.readerFor(new TypeReference<List<EksternBehandlingsinfoDto>>() {
        });
        try {
            return reader.readValue(jsonNode);
        } catch (IOException e) {
            throw FpsakKlientFeil.FACTORY.lesResponsFeil(saksnummer, e).toException();
        }
    }

    private PersonopplysningDto hentPersonopplysninger(BehandlingResourceLinkDto resourceLink) {
        URI endpoint = URI.create(baseUri() + resourceLink.getHref());
        return get(endpoint, PersonopplysningDto.class).orElseThrow(() -> new IllegalArgumentException("Forventet å finne personopplysninger på lenken: " + endpoint));
    }

    private SoknadDto hentSøknad(BehandlingResourceLinkDto resourceLink) {
        URI endpoint = URI.create(baseUri() + resourceLink.getHref());
        return get(endpoint, SoknadDto.class)
            .orElseThrow(() -> new IllegalArgumentException("Forventet å finne søknad på lenken: " + endpoint));
    }

    private Optional<TilbakekrevingValgDto> hentTilbakekrevingValg(BehandlingResourceLinkDto resourceLink) {
        URI endpoint = URI.create(baseUri() + resourceLink.getHref());
        return get(endpoint, TilbakekrevingValgDto.class);
    }

    private FagsakDto hentFagsak(BehandlingResourceLinkDto resourceLink) {
        URI endpoint = URI.create(baseUri() + resourceLink.getHref());
        return get(endpoint, FagsakDto.class)
            .orElseThrow(() -> new IllegalArgumentException("Forventet å finne fagsak på lenken: " + endpoint));
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
