package no.nav.foreldrepenger.tilbakekreving.fpsak.klient;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

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

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.Fptilbake;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.FagsakDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.PersonopplysningDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SoknadDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.VarseltekstDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.VergeDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.simulering.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.BehandlingResourceLinkDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.FpsakBehandlingInfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.simulering.FpoppdragRestKlient;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.PropertyUtil;

@ApplicationScoped
@Fptilbake
public class FpsakKlient implements FagsystemKlient {

    private static final Logger logger = LoggerFactory.getLogger(FpsakKlient.class);

    private static final String FPSAK_BASE_URL = "http://fpsak";
    private static final String FPSAK_OVERRIDE_URL = "fpsak.override.url";
    private static final String FPSAK_API_PATH = "/fpsak/api";

    private static final String BEHANDLING_EP = "/behandling/backend-root";
    private static final String BEHANDLING_ALLE_EP = "/behandlinger/alle";

    private static final String PARAM_NAME_BEHANDLING_UUID = "uuid";
    private static final String PARAM_NAME_SAKSNUMMER = "saksnummer";

    private OidcRestClient restClient;

    //TODO skriv om slik at fpoppdrag ikke behandles spesielt
    //fpoppdrag trenger ikke en egen klient
    //kanskje den til og med skal skrives om til at fpsak gir lenke (slik som for de andre tjenestene)
    private FpoppdragRestKlient fpoppdragKlient;

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
    public FpsakKlient(OidcRestClient restClient, FpoppdragRestKlient fpoppdragKlient) {
        this.restClient = restClient;
        this.fpoppdragKlient = fpoppdragKlient;
    }

    @Override
    public boolean finnesBehandlingIFagsystem(String saksnummer, Henvisning henvisning) {
        List<EksternBehandlingsinfoDto> eksternBehandlinger = hentBehandlingForSaksnummer(saksnummer);
        if (!eksternBehandlinger.isEmpty()) {
            return eksternBehandlinger.stream()
                .anyMatch(eksternBehandlingsinfoDto -> henvisning.equals(eksternBehandlingsinfoDto.getHenvisning()));
        }
        return false;
    }

    @Override
    public SamletEksternBehandlingInfo hentBehandlingsinfo(UUID eksternUuid, Tillegsinformasjon... tillegsinformasjon) {
        return hentBehandlingsinfoOpt(eksternUuid, Arrays.asList(tillegsinformasjon))
            .orElseThrow(() -> FpsakKlientFeil.FACTORY.fantIkkeYtelesbehandlingIFagsystemet(eksternUuid).toException());
    }

    @Override
    public Optional<SamletEksternBehandlingInfo> hentBehandlingsinfoOpt(UUID eksternUuid, Tillegsinformasjon... tillegsinformasjon) {
        return hentBehandlingsinfoOpt(eksternUuid, Arrays.asList(tillegsinformasjon));
    }

    private Optional<SamletEksternBehandlingInfo> hentBehandlingsinfoOpt(UUID eksternUuid, Collection<Tillegsinformasjon> tilleggsinformasjon) {
        Optional<FpsakBehandlingInfoDto> fpsakBehandlingInfoDtoOptoinal = hentFpsakBehandlingOptional(eksternUuid);
        return fpsakBehandlingInfoDtoOptoinal.map(fpsakBehandingInfo -> {
            SamletEksternBehandlingInfo.Builder builder = SamletEksternBehandlingInfo.builder(tilleggsinformasjon);
            builder.setGrunninformasjon(fpsakBehandingInfo);
            List<BehandlingResourceLinkDto> lenker = fpsakBehandingInfo.getLinks();
            for (BehandlingResourceLinkDto lenke : lenker) {
                if (tilleggsinformasjon.contains(Tillegsinformasjon.PERSONOPPLYSNINGER) && lenke.getRel().equals(Tillegsinformasjon.PERSONOPPLYSNINGER.getFpsakRelasjonNavn())) {
                    builder.setPersonopplysninger(hentPersonopplysninger(lenke));
                }
                if (tilleggsinformasjon.contains(Tillegsinformasjon.VARSELTEKST) && lenke.getRel().equals(Tillegsinformasjon.VARSELTEKST.getFpsakRelasjonNavn())) {
                    hentVarseltekst(lenke).ifPresent(builder::setVarseltekst);
                }
                if (tilleggsinformasjon.contains(Tillegsinformasjon.SØKNAD) && lenke.getRel().equals(Tillegsinformasjon.SØKNAD.getFpsakRelasjonNavn())) {
                    builder.setFamiliehendelse(hentSøknad(lenke));
                }
                if (tilleggsinformasjon.contains(Tillegsinformasjon.TILBAKEKREVINGSVALG) && lenke.getRel().equals(Tillegsinformasjon.TILBAKEKREVINGSVALG.getFpsakRelasjonNavn())) {
                    hentTilbakekrevingValg(lenke).ifPresent(builder::setTilbakekrevingvalg);
                }
                if (tilleggsinformasjon.contains(Tillegsinformasjon.FAGSAK) && lenke.getRel().equals(Tillegsinformasjon.FAGSAK.getFpsakRelasjonNavn())) {
                    builder.setFagsak(hentFagsak(lenke));
                }
                if (tilleggsinformasjon.contains(Tillegsinformasjon.VERGE) && lenke.getRel().equals(Tillegsinformasjon.VERGE.getFpsakRelasjonNavn())) {
                    hentVergeInformasjon(lenke).ifPresent(builder::setVerge);
                }
            }
            return builder.build();
        });
    }

    @Override
    public Optional<EksternBehandlingsinfoDto> hentBehandlingOptional(UUID eksternUuid) {
        return hentFpsakBehandlingOptional(eksternUuid).map(Function.identity());
    }

    private Optional<FpsakBehandlingInfoDto> hentFpsakBehandlingOptional(UUID eksternUuid) {
        URI endpoint = createUri(BEHANDLING_EP, PARAM_NAME_BEHANDLING_UUID, eksternUuid.toString());
        Optional<FpsakBehandlingInfoDto> dto = get(endpoint, FpsakBehandlingInfoDto.class);
        if (dto.isPresent()) {
            FpsakBehandlingInfoDto fpsakdto = dto.get();
            fpsakdto.setHenvisning(Henvisning.fraEksternBehandlingId(fpsakdto.getId()));
            return Optional.of(fpsakdto);
        }
        return Optional.empty();
    }

    @Override
    public EksternBehandlingsinfoDto hentBehandling(UUID eksternUuid) {
        return hentBehandlingOptional(eksternUuid)
            .orElseThrow(() -> FpsakKlientFeil.FACTORY.fantIkkeEksternBehandlingForUuid(eksternUuid.toString()).toException());
    }

    @Override
    public Optional<TilbakekrevingValgDto> hentTilbakekrevingValg(UUID eksternUuid) {
        Optional<FpsakBehandlingInfoDto> eksternBehandlingsinfoDtoOptional = hentFpsakBehandlingOptional(eksternUuid);
        if (eksternBehandlingsinfoDtoOptional.isPresent()) {
            Optional<BehandlingResourceLinkDto> ressursLink = eksternBehandlingsinfoDtoOptional.get().getLinks().stream()
                .filter(resourceLink -> Tillegsinformasjon.TILBAKEKREVINGSVALG.getFpsakRelasjonNavn().equals(resourceLink.getRel())).findAny();
            if (ressursLink.isPresent()) {
                return hentTilbakekrevingValg(ressursLink.get());
            }
        }
        return Optional.empty();
    }


    @Override
    public List<EksternBehandlingsinfoDto> hentBehandlingForSaksnummer(String saksnummer) {
        return new ArrayList<>(hentFpsakBehandlingForSaksnummer(saksnummer));
    }

    @Override
    public FeilutbetaltePerioderDto hentFeilutbetaltePerioder(Henvisning henvisning) {
        long fpsakBehandlingId = henvisning.toLong();
        return fpoppdragKlient.hentFeilutbetaltePerioder(fpsakBehandlingId)
            .orElseThrow(() -> FpsakKlientFeil.FACTORY.fantIkkeYtelesbehandlingISimuleringsapplikasjonen(fpsakBehandlingId).toException());
    }

    public List<FpsakBehandlingInfoDto> hentFpsakBehandlingForSaksnummer(String saksnummer) {
        URI endpoint = createUri(BEHANDLING_ALLE_EP, PARAM_NAME_SAKSNUMMER, saksnummer);
        JsonNode jsonNode = restClient.get(endpoint, JsonNode.class);
        //TODO Fiks slik at denne kan leses på vanlig måte (json håndtert av OidcRestClient)
        List<FpsakBehandlingInfoDto> behandlinger = lesResponsFraJsonNode(saksnummer, jsonNode);
        for (FpsakBehandlingInfoDto dto : behandlinger) {
            dto.setHenvisning(Henvisning.fraEksternBehandlingId(dto.getId()));
        }
        return behandlinger;
    }

    private List<FpsakBehandlingInfoDto> lesResponsFraJsonNode(String saksnummer, JsonNode jsonNode) {
        ObjectReader reader = mapper.readerFor(new TypeReference<List<FpsakBehandlingInfoDto>>() {
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

    private Optional<VarseltekstDto> hentVarseltekst(BehandlingResourceLinkDto resourceLink) {
        URI endpoint = URI.create(baseUri() + resourceLink.getHref());
        return get(endpoint, VarseltekstDto.class);

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

    private Optional<VergeDto> hentVergeInformasjon(BehandlingResourceLinkDto resourceLink) {
        URI endpoint = URI.create(baseUri() + resourceLink.getHref());
        return get(endpoint, VergeDto.class);
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
