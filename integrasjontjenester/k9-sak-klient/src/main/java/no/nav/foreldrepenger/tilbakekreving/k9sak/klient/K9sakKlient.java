package no.nav.foreldrepenger.tilbakekreving.k9sak.klient;

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

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.K9tilbake;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.FagsakDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.PersonopplysningDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SoknadDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.VarseltekstDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.simulering.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.tilbakekreving.k9sak.klient.dto.BehandlingResourceLinkDto;
import no.nav.foreldrepenger.tilbakekreving.k9sak.klient.dto.K9sakBehandlingInfoDto;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.PropertyUtil;

@ApplicationScoped
@K9tilbake
public class K9sakKlient implements FagsystemKlient {

    private static final Logger logger = LoggerFactory.getLogger(K9sakKlient.class);

    private static final String K9SAK_BASE_URL = "http://k9-sak";
    private static final String K9SAK_OVERRIDE_URL = "k9sak.override.url";
    private static final String K9SAK_API_PATH = "/k9/sak/api";

    private static final String BEHANDLING_EP = "/behandling/backend-root";
    private static final String BEHANDLING_ALLE_EP = "/behandlinger/alle";

    private static final String PARAM_NAME_BEHANDLING_UUID = "behandlingUuid";
    private static final String PARAM_NAME_SAKSNUMMER = "saksnummer";

    private static final String K9_OPPDRAG_BASE_URL = "http://k9-oppdrag/k9/oppdrag/api";
    private static final String K9_OPPDRAG_OVERRIDE_URL = "k9oppdrag.override.url";
    private static final String K9_OPPDRAG_HENT_FEILUTBETALINGER = "/simulering/feilutbetalte-perioder";

    private OidcRestClient restClient;

    @Inject
    public K9sakKlient(OidcRestClient restClient) {
        this.restClient = restClient;
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
    public SamletEksternBehandlingInfo hentBehandlingsinfo(UUID eksternUuid, Tillegsinformasjon... tilleggsinformasjon) {
        return hentBehandlingsinfoOpt(eksternUuid, Arrays.asList(tilleggsinformasjon))
            .orElseThrow(() -> K9sakKlientFeil.FACTORY.fantIkkeYtelesbehandlingIFagsystemet(eksternUuid).toException());
    }

    @Override
    public Optional<SamletEksternBehandlingInfo> hentBehandlingsinfoOpt(UUID eksternUuid, Tillegsinformasjon... tilleggsinformasjon) {
        return hentBehandlingsinfoOpt(eksternUuid, Arrays.asList(tilleggsinformasjon));
    }

    @Override
    public Optional<EksternBehandlingsinfoDto> hentBehandlingOptional(UUID eksternUuid) {
        return hentK9akBehandlingOptional(eksternUuid).map(Function.identity());
    }

    @Override
    public EksternBehandlingsinfoDto hentBehandling(UUID eksternUuid) {
        return hentK9akBehandlingOptional(eksternUuid)
            .orElseThrow(() -> K9sakKlientFeil.FACTORY.fantIkkeEksternBehandlingForUuid(eksternUuid.toString()).toException());
    }

    @Override
    public Optional<TilbakekrevingValgDto> hentTilbakekrevingValg(UUID eksternUuid) {
        Optional<K9sakBehandlingInfoDto> eksternBehandlingsinfoDtoOptional = hentK9akBehandlingOptional(eksternUuid);
        if (eksternBehandlingsinfoDtoOptional.isPresent()) {
            Optional<BehandlingResourceLinkDto> ressursLink = eksternBehandlingsinfoDtoOptional.get().getLinks().stream()
                .filter(resourceLink -> Tillegsinformasjon.TILBAKEKREVINGSVALG.getK9sakRelasjonNavn().equals(resourceLink.getRel())).findAny();
            if (ressursLink.isPresent()) {
                return hentTilbakekrevingValg(ressursLink.get());
            }
        }
        return Optional.empty();
    }

    @Override
    public List<EksternBehandlingsinfoDto> hentBehandlingForSaksnummer(String saksnummer) {
        return new ArrayList<>(hentK9sakBehandlingForSaksnummer(saksnummer));
    }

    @Override
    public FeilutbetaltePerioderDto hentFeilutbetaltePerioder(Henvisning henvisning) {
        UUID uuid = K9HenvisningKonverterer.henvisningTilUuid(henvisning);
        URI hentFeilutbetalingerUri = URI.create(getK9OoppdragBaseUri() + K9_OPPDRAG_HENT_FEILUTBETALINGER);
        return restClient
            .postReturnsOptional(hentFeilutbetalingerUri, uuid, FeilutbetaltePerioderDto.class)
            .orElseThrow(() -> K9sakKlientFeil.FACTORY.fantIkkeYtelesbehandlingISimuleringsapplikasjonen(uuid).toException());
    }

    static class ListeAvK9sakBehandlingInfoDto extends ArrayList<K9sakBehandlingInfoDto>{}

    private List<K9sakBehandlingInfoDto> hentK9sakBehandlingForSaksnummer(String saksnummer) {
        URI endpoint = createUri(BEHANDLING_ALLE_EP, PARAM_NAME_SAKSNUMMER, saksnummer);
        List<K9sakBehandlingInfoDto> behandlinger = restClient.get(endpoint, ListeAvK9sakBehandlingInfoDto.class);
        for (K9sakBehandlingInfoDto dto : behandlinger) {
            dto.setHenvisning(hentHenvisning(dto.getUuid()));
        }
        return behandlinger;
    }

    private Optional<SamletEksternBehandlingInfo> hentBehandlingsinfoOpt(UUID eksternUuid, Collection<Tillegsinformasjon> tilleggsinformasjon) {
        Optional<K9sakBehandlingInfoDto> k9sakBehandlingInfoDtoOptional = hentK9akBehandlingOptional(eksternUuid);
        return k9sakBehandlingInfoDtoOptional.map(k9sakBehandlingInfoDto -> {
            SamletEksternBehandlingInfo.Builder builder = SamletEksternBehandlingInfo.builder(tilleggsinformasjon);
            builder.setGrunninformasjon(k9sakBehandlingInfoDto);
            List<BehandlingResourceLinkDto> lenker = k9sakBehandlingInfoDto.getLinks();
            for (BehandlingResourceLinkDto lenke : lenker) {
                if (tilleggsinformasjon.contains(Tillegsinformasjon.PERSONOPPLYSNINGER) && lenke.getRel().equals(Tillegsinformasjon.PERSONOPPLYSNINGER.getK9sakRelasjonNavn())) {
                    builder.setPersonopplysninger(hentPersonopplysninger(lenke));
                }
                if (tilleggsinformasjon.contains(Tillegsinformasjon.VARSELTEKST) && lenke.getRel().equals(Tillegsinformasjon.VARSELTEKST.getK9sakRelasjonNavn())) {
                    hentVarseltekst(lenke).ifPresent(builder::setVarseltekst);
                }
                if (tilleggsinformasjon.contains(Tillegsinformasjon.SØKNAD) && lenke.getRel().equals(Tillegsinformasjon.SØKNAD.getK9sakRelasjonNavn())) {
                    builder.setFamiliehendelse(hentSøknad(lenke));
                }
                if (tilleggsinformasjon.contains(Tillegsinformasjon.TILBAKEKREVINGSVALG) && lenke.getRel().equals(Tillegsinformasjon.TILBAKEKREVINGSVALG.getK9sakRelasjonNavn())) {
                    hentTilbakekrevingValg(lenke).ifPresent(builder::setTilbakekrevingvalg);
                }
                if (tilleggsinformasjon.contains(Tillegsinformasjon.FAGSAK) && lenke.getRel().equals(Tillegsinformasjon.FAGSAK.getK9sakRelasjonNavn())) {
                    builder.setFagsak(hentFagsak(lenke));
                }
            }
            return builder.build();
        });
    }

    private Optional<K9sakBehandlingInfoDto> hentK9akBehandlingOptional(UUID eksternUuid) {
        URI endpoint = createUri(BEHANDLING_EP, PARAM_NAME_BEHANDLING_UUID, eksternUuid.toString());
        Optional<K9sakBehandlingInfoDto> dto = get(endpoint, K9sakBehandlingInfoDto.class);
        if (dto.isPresent()) {
            K9sakBehandlingInfoDto k9sakDto = dto.get();
            k9sakDto.setHenvisning(hentHenvisning(eksternUuid));
            return Optional.of(k9sakDto);
        }
        return Optional.empty();
    }

    private Henvisning hentHenvisning(UUID uuid) {
        return K9HenvisningKonverterer.uuidTilHenvisning(uuid);
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

    private URI createUri(String endpoint, String paramName, String paramValue) {
        UriBuilder builder = UriBuilder.fromUri(apiUri())
            .path(endpoint);

        if (notNullOrEmpty(paramName) && notNullOrEmpty(paramValue)) {
            builder.queryParam(paramName, paramValue);
        }
        return builder.build();
    }

    private <T> Optional<T> get(URI endpoint, Class<T> tClass) {
        return restClient.getReturnsOptional(endpoint, tClass);
    }

    private URI apiUri() {
        return UriBuilder.fromUri(baseUri()).path(K9SAK_API_PATH).build();
    }

    private URI baseUri() {
        String override = PropertyUtil.getProperty(K9SAK_OVERRIDE_URL);
        if (override != null && !override.isEmpty()) {
            logger.info("Overstyrer k9sak base URL med {}", override);
            return URI.create(override);
        }
        return URI.create(K9SAK_BASE_URL);
    }

    private URI getK9OoppdragBaseUri() {
        String overrideUrl = PropertyUtil.getProperty(K9_OPPDRAG_OVERRIDE_URL);
        if (overrideUrl != null && !overrideUrl.isEmpty()) {
            logger.info("Overstyrte URL til k9-oppdrag til {}", overrideUrl);
            return URI.create(overrideUrl);
        } else {
            return URI.create(K9_OPPDRAG_BASE_URL);
        }

    }

    private boolean notNullOrEmpty(String str) {
        return (str != null && !str.isEmpty());
    }
}
