package no.nav.foreldrepenger.tilbakekreving.fpsak.klient;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.kontrakter.simulering.resultat.v1.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.Fptilbake;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.FagsakDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.PersonopplysningDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SendtoppdragDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SoknadDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.VarseltekstDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.VergeDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.BehandlingResourceLinkDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.FpsakBehandlingInfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.simulering.FpoppdragRestKlient;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@Fptilbake
@RestClientConfig(tokenConfig = TokenFlow.ADAPTIVE, application = FpApplication.FPSAK)
public class FpsakKlient implements FagsystemKlient {

    private final RestClient restClient;
    private final RestConfig restConfig;

    private final FpoppdragRestKlient fpoppdragKlient;

    public FpsakKlient() {
        this(RestClient.client(), new FpoppdragRestKlient());
    }

    FpsakKlient(RestClient restClient, FpoppdragRestKlient fpoppdragKlient) {
        this.restClient = restClient;
        this.restConfig = RestConfig.forClient(this.getClass());
        this.fpoppdragKlient = fpoppdragKlient;
    }

    @Override
    public boolean finnesBehandlingIFagsystem(String saksnummer, Henvisning henvisning) {
        var eksternBehandlinger = hentBehandlingForSaksnummer(saksnummer);
        if (!eksternBehandlinger.isEmpty()) {
            return eksternBehandlinger.stream()
                    .anyMatch(eksternBehandlingsinfoDto -> henvisning.equals(eksternBehandlingsinfoDto.getHenvisning()));
        }
        return false;
    }

    @Override
    public SamletEksternBehandlingInfo hentBehandlingsinfo(UUID eksternUuid, Tillegsinformasjon... tillegsinformasjon) {
        return hentBehandlingsinfoOpt(eksternUuid, Arrays.asList(tillegsinformasjon))
                .orElseThrow(() -> new IntegrasjonException("FPT-841932", String.format("Fant ikke behandling med behandingUuid %s i fpsak", eksternUuid)));
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
                if (tilleggsinformasjon.contains(Tillegsinformasjon.SENDTOPPDRAG) && lenke.getRel().equals(Tillegsinformasjon.SENDTOPPDRAG.getFpsakRelasjonNavn())) {
                    hentSendtoppdrag(lenke).ifPresent(builder::setSendtoppdrag);
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
        URI endpoint = createUri("/api/behandling/backend-root", "uuid", eksternUuid.toString());
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
                .orElseThrow(() -> new IntegrasjonException("FPT-7428496", String.format("Fant ingen ekstern behandling i Fpsak for Uuid %s", eksternUuid.toString())));
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
                .orElseThrow(() -> new IntegrasjonException("FPT-748279", String.format("Fant ikke behandling med behandlingId %s fpoppdrag", fpsakBehandlingId)));
    }

    static class ListeAvFpsakBehandlingInfoDto extends ArrayList<FpsakBehandlingInfoDto> {
    }

    public List<FpsakBehandlingInfoDto> hentFpsakBehandlingForSaksnummer(String saksnummer) {
        var endpoint = createUri("/api/behandlinger/alle", "saksnummer", saksnummer);
        List<FpsakBehandlingInfoDto> behandlinger = restClient.send(RestRequest.newGET(endpoint, restConfig), ListeAvFpsakBehandlingInfoDto.class);
        for (var dto : behandlinger) {
            dto.setHenvisning(Henvisning.fraEksternBehandlingId(dto.getId()));
        }
        return behandlinger;
    }

    private URI endpointFraLink(BehandlingResourceLinkDto resourceLink) {
        var linkpath = resourceLink.getHref();
        var path = linkpath.startsWith("/fpsak") ?  linkpath.replaceFirst("/fpsak", "") : linkpath;
        return URI.create(restConfig.fpContextPath() + path);
    }

    private PersonopplysningDto hentPersonopplysninger(BehandlingResourceLinkDto resourceLink) {
        URI endpoint = endpointFraLink(resourceLink);
        return get(endpoint, PersonopplysningDto.class)
            .orElseThrow(() -> new IllegalArgumentException("Forventet å finne personopplysninger på lenken: " + endpoint));
    }

    private Optional<VarseltekstDto> hentVarseltekst(BehandlingResourceLinkDto resourceLink) {
        URI endpoint = endpointFraLink(resourceLink);
        return get(endpoint, VarseltekstDto.class);

    }

    private Optional<SendtoppdragDto> hentSendtoppdrag(BehandlingResourceLinkDto resourceLink) {
        URI endpoint = endpointFraLink(resourceLink);
        return get(endpoint, SendtoppdragDto.class);
    }

    private SoknadDto hentSøknad(BehandlingResourceLinkDto resourceLink) {
        URI endpoint = endpointFraLink(resourceLink);
        return get(endpoint, SoknadDto.class)
                .orElseThrow(() -> new IllegalArgumentException("Forventet å finne søknad på lenken: " + endpoint));
    }

    private Optional<TilbakekrevingValgDto> hentTilbakekrevingValg(BehandlingResourceLinkDto resourceLink) {
        URI endpoint = endpointFraLink(resourceLink);
        return get(endpoint, TilbakekrevingValgDto.class);
    }

    private FagsakDto hentFagsak(BehandlingResourceLinkDto resourceLink) {
        URI endpoint = endpointFraLink(resourceLink);
        return get(endpoint, FagsakDto.class)
                .orElseThrow(() -> new IllegalArgumentException("Forventet å finne fagsak på lenken: " + endpoint));
    }

    private Optional<VergeDto> hentVergeInformasjon(BehandlingResourceLinkDto resourceLink) {
        URI endpoint = endpointFraLink(resourceLink);
        return get(endpoint, VergeDto.class);
    }

    private <T> Optional<T> get(URI endpoint, Class<T> tClass) {
        return restClient.sendReturnOptional(RestRequest.newGET(endpoint, restConfig), tClass);
    }

    private URI createUri(String endpoint, String paramName, String paramValue) {
        var builder = UriBuilder.fromUri(restConfig.fpContextPath())
            .path(endpoint);

        if (notNullOrEmpty(paramName) && notNullOrEmpty(paramValue)) {
            builder.queryParam(paramName, paramValue);
        }
        return builder.build();
    }

    private boolean notNullOrEmpty(String str) {
        return (str != null && !str.isEmpty());
    }

}
