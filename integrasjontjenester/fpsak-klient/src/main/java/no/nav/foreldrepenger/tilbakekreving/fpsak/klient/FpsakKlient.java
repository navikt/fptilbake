package no.nav.foreldrepenger.tilbakekreving.fpsak.klient;

import java.net.URI;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.util.UriEncoder;

import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.BehandlingResourceLinkDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.FagsakDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.PersonopplysningDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.TilbakekrevingDataDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.VarseltekstDto;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.PropertyUtil;

@ApplicationScoped
public class FpsakKlient {

    private static final Logger logger = LoggerFactory.getLogger(FpsakKlient.class);

    private static final String FPSAK_BASE_URL = "http://fpsak";
    private static final String FPSAK_OVERRIDE_URL = "fpsak.override.url";
    private static final String FPSAK_API_PATH = "/fpsak/api";

    private static final String BEHANDLING_EP = "/behandlinger";
    private static final String FAGSAK_EP = "/fagsak";

    private static final String TILBAKEKREVING_EP = "/behandling/tilbakekreving";
    private static final String TILBAKEKREVING_DATA_EP = TILBAKEKREVING_EP + "/data";
    private static final String TILBAKEKREVING_VARSELTEKST_EP = TILBAKEKREVING_EP + "/varseltekst";

    private static final String FILTER_REL_PERSONOPPLYSNINGER = "soeker-personopplysninger";

    private static final String PARAM_NAME_BEHANDLING_ID = "behandlingId";

    private OidcRestClient restClient;

    FpsakKlient() {
        // CDI
    }

    @Inject
    public FpsakKlient(OidcRestClient restClient) {
        this.restClient = restClient;
    }

    public Long hentFagsakId(long behandlingId) {
        Optional<EksternBehandlingsinfoDto> eksternBehandlingOptional = hentBehandling(behandlingId);

        EksternBehandlingsinfoDto eksternBehandlingsinfoDto = eksternBehandlingOptional.orElseThrow(() -> FpsakKlientFeil.FACTORY.fantIkkeBehandlingIFpsak(behandlingId).toException());

        return eksternBehandlingsinfoDto.getFagsakId();
    }

    public boolean finnesBehandlingIFpsak(Long behandlingId) {
        return hentBehandling(behandlingId).isPresent();
    }

    public Optional<EksternBehandlingsinfoDto> hentBehandlingsinfo(long behandlingId, String saksnummer) {
        Optional<EksternBehandlingsinfoDto> eksternBehandlingsinfoDtoOptional = hentBehandling(behandlingId);

        eksternBehandlingsinfoDtoOptional.ifPresent(eksternBehandlingsinfo -> {
            eksternBehandlingsinfo.getLinks().forEach(resourceLink -> {
                final Optional<PersonopplysningDto> personopplysningDto = hentPersonopplysninger(resourceLink);
                personopplysningDto.ifPresent(eksternBehandlingsinfo::setPersonopplysningDto);
            });

            Optional<FagsakDto> fagsakDto = hentFagsakType(saksnummer);
            fagsakDto.ifPresent(fagsakType -> eksternBehandlingsinfo.setFagsaktype(fagsakType.getSakstype()));

            Optional<String> varseltekstDto = hentVarseltekst(behandlingId);
            varseltekstDto.ifPresent(eksternBehandlingsinfo::setVarseltekst);

        });
        return eksternBehandlingsinfoDtoOptional;
    }

    public Optional<String> hentVarseltekst(long behandlingId) {
        URI endpointUri = createUri(TILBAKEKREVING_VARSELTEKST_EP, PARAM_NAME_BEHANDLING_ID, String.valueOf(behandlingId));
        Optional<VarseltekstDto> varseltekstDto = get(endpointUri, VarseltekstDto.class);
        return varseltekstDto.map(VarseltekstDto::getVarseltekst);
    }

    private Optional<EksternBehandlingsinfoDto> hentBehandling(long behandlingId) {
        URI endpoint = createUri(BEHANDLING_EP, PARAM_NAME_BEHANDLING_ID, String.valueOf(behandlingId));
        return get(endpoint, EksternBehandlingsinfoDto.class);
    }

    private Optional<PersonopplysningDto> hentPersonopplysninger(BehandlingResourceLinkDto resourceLink) {
        if (FILTER_REL_PERSONOPPLYSNINGER.equals(resourceLink.getRel())) {
            URI endpoint = URI.create(baseUri() + resourceLink.getHref());
            return get(endpoint, PersonopplysningDto.class);
        }
        return Optional.empty();
    }

    private Optional<FagsakDto> hentFagsakType(String saksnummer) {
        URI endpoint = createUri(FAGSAK_EP, "saksnummer", saksnummer);
        return get(endpoint, FagsakDto.class);
    }

    public Optional<TilbakekrevingDataDto> hentTilbakekrevingData(long behandlingId) {
        URI endpoint = createUri(TILBAKEKREVING_DATA_EP, PARAM_NAME_BEHANDLING_ID, String.valueOf(behandlingId));
        return get(endpoint, TilbakekrevingDataDto.class);
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
