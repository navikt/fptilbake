package no.nav.foreldrepenger.tilbakekreving.fpsak.klient;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.kontrakter.simulering.resultat.v1.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.Fptilbake;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.Tillegsinformasjon;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.FagsakDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.FamilieHendelseType;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.PersonopplysningDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.SamletEksternBehandlingInfo;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.TilbakekrevingValgDto;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.VergeDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.FpsakBehandlingInfoDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.FpsakTilbakeDto;
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

    private static final String BEHANDLING_PATH = "/api/tilbake/behandling";
    private static final String HENVISNING_PATH = "/api/tilbake/henvisning";

    private final RestClient restClient;
    private final RestConfig restConfig;
    private final URI henvisningTarget;

    private final FpoppdragRestKlient fpoppdragKlient;

    public FpsakKlient() {
        this(RestClient.client(), new FpoppdragRestKlient());
    }

    FpsakKlient(RestClient restClient, FpoppdragRestKlient fpoppdragKlient) {
        this.restClient = restClient;
        this.restConfig = RestConfig.forClient(this.getClass());
        this.fpoppdragKlient = fpoppdragKlient;
        this.henvisningTarget = UriBuilder.fromUri(restConfig.fpContextPath()).path(HENVISNING_PATH).build();
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

    @Override
    public Optional<EksternBehandlingsinfoDto> hentBehandlingOptional(UUID eksternUuid) {
        return hentBehandlingsinfoOpt(eksternUuid).map(SamletEksternBehandlingInfo::getGrunninformasjon);
    }

    @Override
    public EksternBehandlingsinfoDto hentBehandling(UUID eksternUuid) {
        return hentBehandlingOptional(eksternUuid)
                .orElseThrow(() -> new IntegrasjonException("FPT-7428496", String.format("Fant ingen ekstern behandling i Fpsak for Uuid %s", eksternUuid.toString())));
    }

    @Override
    public Optional<TilbakekrevingValgDto> hentTilbakekrevingValg(UUID eksternUuid) {
        return hentBehandlingsinfoOpt(eksternUuid, Tillegsinformasjon.TILBAKEKREVINGSVALG).map(SamletEksternBehandlingInfo::getTilbakekrevingsvalg);
    }


    @Override
    public Optional<EksternBehandlingsinfoDto> hentBehandlingForSaksnummerHenvisning(String saksnummer, Henvisning henvisning) {
        return hentFpsakBehandlingForSaksnummerHenvisning(saksnummer, henvisning).map(Function.identity());
    }

    @Override
    public FeilutbetaltePerioderDto hentFeilutbetaltePerioder(Henvisning henvisning) {
        throw new IllegalStateException("Utviklerfeil: Kall metoden med flere parametre");
    }

    @Override
    public FeilutbetaltePerioderDto hentFeilutbetaltePerioder(Henvisning henvisning, UUID behandlingUuid, String saksnummer) {
        long fpsakBehandlingId = henvisning.toLong();
        return fpoppdragKlient.hentFeilutbetaltePerioder(fpsakBehandlingId, behandlingUuid, saksnummer)
                .orElseThrow(() -> new IntegrasjonException("FPT-748279", String.format("Fant ikke behandling med behandlingId %s fpoppdrag", fpsakBehandlingId)));
    }


    private Optional<SamletEksternBehandlingInfo> hentBehandlingsinfoOpt(UUID eksternUuid, Collection<Tillegsinformasjon> tilleggsinformasjon) {
        var fullbehandling = hentFpsakFullBehandlingOptional(eksternUuid);
        return fullbehandling.map(fpsakBehandingInfo -> mapSamletEksternBehandlingInfo(tilleggsinformasjon, fpsakBehandingInfo));
    }

    private Optional<FpsakTilbakeDto> hentFpsakFullBehandlingOptional(UUID eksternUuid) {
        var target = UriBuilder.fromUri(restConfig.fpContextPath()).path(BEHANDLING_PATH)
            .queryParam("uuid", eksternUuid.toString())
            .build();
        return restClient.sendReturnOptional(RestRequest.newGET(target, restConfig), FpsakTilbakeDto.class);
    }

    private static SamletEksternBehandlingInfo mapSamletEksternBehandlingInfo(Collection<Tillegsinformasjon> tilleggsinformasjon,
                                                                              FpsakTilbakeDto fpsakBehandingInfo) {
        var builder = SamletEksternBehandlingInfo.builder(tilleggsinformasjon);
        builder.setGrunninformasjon(FpsakBehandlingInfoDto.fraFullDto(fpsakBehandingInfo));
        builder.setPersonopplysninger(personopplysningFraFullDto(fpsakBehandingInfo));
        Optional.ofNullable(fpsakBehandingInfo.feilutbetaling()).map(FpsakTilbakeDto.FeilutbetalingDto::varseltekst)
            .ifPresent(builder::setVarseltekst);
        builder.setSendtoppdrag(fpsakBehandingInfo.sendtoppdrag());
        videreFraFullDto(fpsakBehandingInfo).ifPresent(builder::setTilbakekrevingvalg);
        builder.setFagsak(fagsakFraFullDto(fpsakBehandingInfo));
        builder.setFamiliehendelse(soknadFraFullDto(fpsakBehandingInfo));
        vergeFraFullDto(fpsakBehandingInfo).ifPresent(builder::setVerge);
        return builder.build();
    }

    private Optional<FpsakBehandlingInfoDto> hentFpsakBehandlingForSaksnummerHenvisning(String saksnummer, Henvisning henvisning) {
        var request = RestRequest.newPOSTJson(new HenvisningRequestDto(saksnummer, henvisning.toLong()), henvisningTarget, restConfig);
        var full = restClient.sendReturnOptional(request, FpsakTilbakeDto.class);
        return full.map(FpsakBehandlingInfoDto::fraFullDto);
    }


    private static PersonopplysningDto personopplysningFraFullDto(FpsakTilbakeDto fullDto) {
        var personopplysninger = new PersonopplysningDto();
        personopplysninger.setAktoerId(fullDto.fagsak().aktørId());
        personopplysninger.setAntallBarn(fullDto.familieHendelse().antallBarn());
        return personopplysninger;
    }

    private static FagsakDto fagsakFraFullDto(FpsakTilbakeDto fullDto) {
        var fagsakDto = new FagsakDto();
        fagsakDto.setSaksnummer(fullDto.fagsak().saksnummer());
        fagsakDto.setFagsakYtelseType(switch (fullDto.fagsak().fagsakYtelseType()) {
            case FORELDREPENGER -> FagsakYtelseType.FORELDREPENGER;
            case SVANGERSKAPSPENGER -> FagsakYtelseType.SVANGERSKAPSPENGER;
            case ENGANGSSTØNAD -> FagsakYtelseType.ENGANGSTØNAD;
        });
        return fagsakDto;
    }

    private static FamilieHendelseType soknadFraFullDto(FpsakTilbakeDto fullDto) {
        return fullDto.familieHendelse().familieHendelseType() == FpsakTilbakeDto.FamilieHendelseType.ADOPSJON ? FamilieHendelseType.ADOPSJON : FamilieHendelseType.FØDSEL;
    }

    private static Optional<TilbakekrevingValgDto> videreFraFullDto(FpsakTilbakeDto fullDto) {
        var feilutbetalingValg = Optional.ofNullable(fullDto.feilutbetaling())
            .map(FpsakTilbakeDto.FeilutbetalingDto::feilutbetalingValg)
            .map(v -> switch (v) {
                case OPPRETT -> VidereBehandling.TILBAKEKR_OPPRETT;
                case OPPDATER -> VidereBehandling.TILBAKEKR_OPPDATER;
                case IGNORER -> VidereBehandling.IGNORER_TILBAKEKREVING;
                case INNTREKK -> VidereBehandling.INNTREKK;
            });
        return feilutbetalingValg.map(TilbakekrevingValgDto::new);
    }

    private static Optional<VergeDto> vergeFraFullDto(FpsakTilbakeDto fullDto) {
        if (fullDto.verge() == null) {
            return Optional.empty();
        }
        var vergeDto = new VergeDto();
        vergeDto.setGyldigFom(fullDto.verge().gyldigFom());
        vergeDto.setGyldigTom(fullDto.verge().gyldigTom());
        vergeDto.setNavn(fullDto.verge().navn());
        vergeDto.setOrganisasjonsnummer(fullDto.verge().organisasjonsnummer());
        vergeDto.setAktoerId(fullDto.verge().aktørId());
        vergeDto.setVergeType(switch (fullDto.verge().vergeType()) {
                case ADVOKAT -> VergeType.ADVOKAT;
                case BARN -> VergeType.BARN;
                case FULLMEKTIG -> VergeType.ANNEN_F;
                case FORELDRELØS -> VergeType.FBARN;
                case VOKSEN -> VergeType.VOKSEN;
            });
        return Optional.of(vergeDto);
    }

    public record HenvisningRequestDto(@NotNull @Digits(integer = 18, fraction = 0) String saksnummer,
                                              @NotNull @Min(0) @Max(Long.MAX_VALUE) Long henvisning) {

    }
}
