package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.tilbakekreving.iverksett.TilbakekrevingVedtakDTO;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsakType;

import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingsvedtakTjeneste;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagXmlUnmarshaller;
import no.nav.foreldrepenger.tilbakekreving.behandling.task.TaskProperties;
import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.task.FortsettBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingresultatRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.EksternBehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.BehandlingTilstandTjeneste;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.SendSakshendelserTilDvhTask;
import no.nav.foreldrepenger.tilbakekreving.datavarehus.saksstatistikk.mapping.BehandlingTilstandMapper;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagValidator;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.BehandlingReferanseAbacAttributter;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.dto.KobleBehandlingTilGrunnlagDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.dto.KorrigertHenvisningDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AbacProperty;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlag;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@Path("/forvaltningBehandling")
@ApplicationScoped
@Transactional
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ForvaltningBehandlingRestTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(ForvaltningBehandlingRestTjeneste.class);

    private BehandlingRepository behandlingRepository;
    private BehandlingresultatRepository behandlingresultatRepository;
    private ProsessTaskTjeneste taskTjeneste;
    private ØkonomiMottattXmlRepository mottattXmlRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private KravgrunnlagMapper kravgrunnlagMapper;
    private KravgrunnlagTjeneste kravgrunnlagTjeneste;
    private EksternBehandlingRepository eksternBehandlingRepository;
    private BehandlingTilstandTjeneste behandlingTilstandTjeneste;

    private TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste;

    public ForvaltningBehandlingRestTjeneste() {
        // for CDI
    }

    @Inject
    public ForvaltningBehandlingRestTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                             ProsessTaskTjeneste taskTjeneste,
                                             BehandlingresultatRepository behandlingresultatRepository,
                                             ØkonomiMottattXmlRepository mottattXmlRepository,
                                             KravgrunnlagMapper kravgrunnlagMapper,
                                             KravgrunnlagTjeneste kravgrunnlagTjeneste,
                                             EksternBehandlingRepository eksternBehandlingRepository,
                                             BehandlingTilstandTjeneste behandlingTilstandTjeneste,
                                             TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.behandlingresultatRepository = behandlingresultatRepository;
        this.taskTjeneste = taskTjeneste;
        this.mottattXmlRepository = mottattXmlRepository;
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.kravgrunnlagMapper = kravgrunnlagMapper;
        this.kravgrunnlagTjeneste = kravgrunnlagTjeneste;
        this.eksternBehandlingRepository = eksternBehandlingRepository;
        this.behandlingTilstandTjeneste = behandlingTilstandTjeneste;
        this.tilbakekrevingsvedtakTjeneste = tilbakekrevingsvedtakTjeneste;
    }

    @POST
    @Path("/tving-henleggelse")
    @Operation(
        tags = "FORVALTNING-behandling",
        description = "Tjeneste for å tvinge en behandling til å bli henlagt, selvom normale regler for saksbehandling ikke tillater henleggelse",
        responses = {
            @ApiResponse(responseCode = "200", description = "Henlagt behandling"),
            @ApiResponse(responseCode = "400", description = "Behandlingen er avsluttet"),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")
        })
    @BeskyttetRessurs(actionType = ActionType.CREATE, property = AbacProperty.DRIFT)
    public Response tvingHenleggelseBehandling(
        @TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
        @QueryParam("behandlingId") @NotNull @Valid BehandlingReferanse behandlingReferanse) {
        Behandling behandling = hentBehandling(behandlingReferanse);
        if (behandling.erAvsluttet()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        LOG.info("Tving henleggelse. Oppretter task for å henlegge behandlingId={}", behandling.getUuid());
        opprettHenleggBehandlingTask(behandling);
        return Response.ok().build();
    }

    @POST
    @Path("/tving-gjenoppta")
    @Operation(
        tags = "FORVALTNING-behandling",
        description = "Tjeneste for å tvinge en behandling til å gjenopptas (tas av vent). NB! Må ikke brukes på saker uten kravgrunnlag!",
        responses = {
            @ApiResponse(responseCode = "200", description = "Gjenopptatt behandling"),
            @ApiResponse(responseCode = "400", description = "Behandlingen er avsluttet eller behandlingen er ikke på vent"),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")
        })
    @BeskyttetRessurs(actionType = ActionType.CREATE, property = AbacProperty.DRIFT)
    public Response tvingGjenopptaBehandling(
        @TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
        @NotNull @QueryParam("behandlingId") @Valid BehandlingReferanse behandlingReferanse) {
        Behandling behandling = hentBehandling(behandlingReferanse);
        if (behandling.erAvsluttet() || !erPåVent(behandling)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        LOG.info("Tving gjenoppta. Oppretter task for å gjenoppta behandlingId={}", behandling.getUuid());
        opprettGjenopptaBehandlingTask(behandling);

        return Response.ok().build();
    }

    private boolean erPåVent(Behandling behandling) {
        //spesielt tilfelle hvor stegstatus er VENTER, men det finnes ikke åpent aksjonspunkt - trenker å kunne dytte videre
        return behandling.isBehandlingPåVent() || behandling.getBehandlingStegStatus() == BehandlingStegStatus.VENTER;
    }

    @POST
    @Path("/fortsett-behandling")
    @Operation(
        tags = "FORVALTNING-behandling",
        description = "Tjenesten for å fortsett en behandling som står i limbo tilstand (unten aksjonspunkter i status venter). NB! Må ikke brukes uten grund!",
        responses = {
            @ApiResponse(responseCode = "200", description = "Fortsett behandling ok"),
            @ApiResponse(responseCode = "400", description = "Behandlingen er avsluttet eller behandlingen er fortsatt på vent"),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")
        })
    @BeskyttetRessurs(actionType = ActionType.CREATE, property = AbacProperty.DRIFT)
    public Response fortsettBehandling(
        @TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
        @NotNull @QueryParam("behandlingId") @Valid BehandlingReferanse behandlingReferanse) {
        Behandling behandling = hentBehandling(behandlingReferanse);
        if (behandling.erAvsluttet() || behandling.isBehandlingPåVent()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        LOG.info("Fortsett behandling. Oppretter task for å fortsettelse av behandlingId={}", behandling.getUuid());
        var prosessTaskData = opprettFortsettBehandlingTask(behandling);
        taskTjeneste.lagre(prosessTaskData);
        return Response.ok().build();
    }

    @POST
    @Path("/tving-koble-grunnlag")
    @Operation(
        tags = "FORVALTNING-behandling",
        description = "Tjeneste for å tvinge en behandling til å bruke et grunnlag. NB! Kun brukes på saker som venter på grunnlag!",
        responses = {
            @ApiResponse(responseCode = "200", description = "Tilkoblet behandling"),
            @ApiResponse(responseCode = "400", description = "Ulike problemer med request, typisk at man peker på feil XML eller behandling."),
            @ApiResponse(responseCode = "500", description = "Feilet pga ugyldig kravgrunnlag, eller ukjent feil.")
        })
    @BeskyttetRessurs(actionType = ActionType.CREATE, property = AbacProperty.DRIFT)
    public Response tvingkobleBehandlingTilGrunnlag(@Valid @NotNull KobleBehandlingTilGrunnlagDto behandlingTilGrunnlagDto) {
        try {
            kobleBehandling(behandlingTilGrunnlagDto);
            return Response.ok().build();
        } catch (UgyldigTvingKoblingForespørselException e) {
            String message = LoggerUtils.removeLineBreaks(e.getMessage());
            LOG.info(message);
            return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
        } catch (KravgrunnlagValidator.UgyldigKravgrunnlagException e) {
            String message = LoggerUtils.removeLineBreaks(
                "Kunne ikke koble behandling med behandlingId=" + behandlingTilGrunnlagDto.getBehandlingId() + " til kravgrunnlag med mottattXmlId="
                    + behandlingTilGrunnlagDto.getMottattXmlId() + " siden kravgrunnlaget ikke er gyldig: ");
            LOG.info(message, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message + e.getMessage()).build();
        }
    }

    @POST
    @Path("/korriger-henvisning")
    @Operation(
        tags = "FORVALTNING-behandling",
        description = "Tjeneste for å korrigere henvisningen til en behandling",
        responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Ulike problemer med request, typisk at man peker på feil eksternBehandlingUuid eller behandling!"),
            @ApiResponse(responseCode = "500", description = "Ukjent feil!")
        })
    @BeskyttetRessurs(actionType = ActionType.CREATE, property = AbacProperty.DRIFT)
    public Response korrigerHenvisning(@Valid @NotNull KorrigertHenvisningDto korrigertHenvisningDto) {
        Behandling behandling = behandlingRepository.hentBehandling(korrigertHenvisningDto.getBehandlingId());
        UUID eksternBehandlingUuid = korrigertHenvisningDto.getEksternBehandlingUuid();
        LOG.info("Korrigerer henvisning. Oppretter task for å korrigere henvisning={} behandlingId={}", eksternBehandlingUuid, behandling.getId());
        opprettKorrigertHenvisningTask(behandling, eksternBehandlingUuid);
        return Response.ok().build();
    }

    @POST
    @Path("/tilbakefør-behandling-til-fakta")
    @Operation(
        tags = "FORVALTNING-behandling",
        description = "Tjeneste for å tilbakeføre behandling til FAKTA steg",
        responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Behandling er avsluttet eller behandling er på vent"),
            @ApiResponse(responseCode = "500", description = "Ukjent feil!")
        })
    @BeskyttetRessurs(actionType = ActionType.CREATE, property = AbacProperty.DRIFT)
    public Response tilbakeførBehandlingTilFaktaSteg(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
                                                     @NotNull @QueryParam("behandlingId") @Valid BehandlingReferanse behandlingReferanse) {
        Behandling behandling = hentBehandling(behandlingReferanse);
        if (behandling.erAvsluttet()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Kan ikke flyttes, behandlingen er avsluttet!").build();
        } else if (behandling.isBehandlingPåVent()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Kan ikke flyttes, behandlingen er på vent!").build();
        }
        kravgrunnlagTjeneste.tilbakeførBehandlingTilFaktaSteg(behandling, null);
        return Response.ok().build();
    }

    @POST
    @Path("/avbryt-aapent-ap-avsluttet-behandling")
    @Operation(
        tags = "FORVALTNING-behandling",
        description = "Tjeneste for å avbryte åpne aksjonspunkt når behandling er avsluttet!",
        responses = {
            @ApiResponse(responseCode = "200", description = "Aksjonspunkt avbrut"),
            @ApiResponse(responseCode = "500", description = "ukjent feil.")
        })
    @BeskyttetRessurs(actionType = ActionType.CREATE, property = AbacProperty.DRIFT)
    public Response avbrytÅpentAksjonspunktForAvsluttetBehandling() {
        behandlingRepository.avbrytÅpentAksjonspunktForAvsluttetBehandling();
        return Response.ok().build();
    }

    @POST
    @Path("/resend-saksstatistikk-aapen-vent")
    @Operation(
        tags = "FORVALTNING-behandling",
        description = "Tjeneste for å avbryte åpne aksjonspunkt når behandling er avsluttet!",
        responses = {
            @ApiResponse(responseCode = "200", description = "Aksjonspunkt avbrut"),
            @ApiResponse(responseCode = "500", description = "ukjent feil.")
        })
    @BeskyttetRessurs(actionType = ActionType.CREATE, property = AbacProperty.DRIFT)
    public Response publiserSaksstatistikkForBehandlingPåVent() {
        behandlingRepository.finnBehandlingerPåVent().forEach(b -> {
            var tilstand = behandlingTilstandTjeneste.hentBehandlingensTilstand(b);
            var taskData = ProsessTaskData.forProsessTask(SendSakshendelserTilDvhTask.class);
            taskData.setPayload(BehandlingTilstandMapper.tilJsonString(tilstand));
            taskData.setBehandling(b.getSaksnummer().getVerdi(), b.getFagsakId(), b.getId());
            taskTjeneste.lagre(taskData);

        });
        return Response.ok().build();
    }

    @POST
    @Path("/sett-ansvarlig-saksbehandler")
    @Operation(
        tags = "FORVALTNING-behandling",
        description = "Tjeneste for å sette ansvarlig saksbehandler, på behandling hvor denne ikke er satt",
        responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "ukjent feil.")
        })
    @BeskyttetRessurs(actionType = ActionType.CREATE, property = AbacProperty.DRIFT)
    public Response settAnsvarligSaksbehandler(@Valid @NotNull ForvaltningBehandlingRestTjeneste.SettAnsvarligSaksbehandlerDto input) {
        var behandling = behandlingRepository.hentBehandling(input.getBehandlingId());

        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        if (behandling.erAvsluttet() || behandling.erUnderIverksettelse()) {
            throw new IllegalArgumentException("Kan ikke endre på behandling som er ferdig/under iverksettelse");
        }
        Set<String> saksbehandlerePåBehandlingen = behandling.getAksjonspunkter()
            .stream()
            .filter(a -> !a.erAutopunkt())
            .filter(a->a.erUtført())
            .filter(a->a.getAksjonspunktDefinisjon() != AksjonspunktDefinisjon.FATTE_VEDTAK)
            .map(a -> a.getEndretAv())
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        if (!saksbehandlerePåBehandlingen.contains(input.getSaksbehandlerIdent())){
            throw new IllegalArgumentException("Saksbehandler er ikke på behandlingen fra før, avbryter. Aktuelle er: " + saksbehandlerePåBehandlingen);
        }
        if (behandling.getAnsvarligSaksbehandler() != null) {
            throw new IllegalArgumentException("Behandligen har allerede en ansvarlg saksbehandler");
        }
        behandling.setAnsvarligSaksbehandler(input.getSaksbehandlerIdent());

        behandlingRepository.lagre(behandling, behandlingLås);

        return Response.ok().build();
    }

    @POST
    @Path("/sett-behanlingsårsak-fattet-av-annen-instans")
    @Operation(
        tags = "FORVALTNING-behandling",
        description = "Tjeneste for å sette på årsaken fattet av annen instans, brukes for å hindre utsending av vedtaksbrev",
        responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "ukjent feil.")
        })
    @BeskyttetRessurs(actionType = ActionType.CREATE, property = AbacProperty.DRIFT)
    public Response settBehandlingsårsakFattetAnnenInstans(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
                                                               @QueryParam("behandlingId") @NotNull @Valid BehandlingReferanse behandlingReferanse) {
        Behandling behandling = hentBehandling(behandlingReferanse);
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        if (behandling.erAvsluttet() || behandling.erUnderIverksettelse()) {
            throw new IllegalArgumentException("Kan ikke endre på behandling som er ferdig/under iverksettelse");
        }
        if (behandling.getBehandlingÅrsaker().stream().anyMatch(å->å.getBehandlingÅrsakType() == BehandlingÅrsakType.VEDTAK_FATTET_AV_ANNEN_INSTANS)){
            throw new IllegalArgumentException("Har allerede årsaken satt");
        }
        BehandlingÅrsak.builder(BehandlingÅrsakType.VEDTAK_FATTET_AV_ANNEN_INSTANS).buildFor(behandling);

        behandlingRepository.lagre(behandling, behandlingLås);

        return Response.ok().build();
    }

    public static class SettAnsvarligSaksbehandlerDto implements AbacDto {

        @NotNull
        @Min(0)
        @Max(Long.MAX_VALUE)
        private Long behandlingId;

        @NotNull
        @Pattern(regexp = "^[A-Z]\\d+$")
        private String saksbehandlerIdent;

        public SettAnsvarligSaksbehandlerDto() {
            // for CDI
        }

        public SettAnsvarligSaksbehandlerDto(Long behandlingId, String saksbehandlerIdent) {
            this.behandlingId = behandlingId;
            this.saksbehandlerIdent = saksbehandlerIdent;
        }

        public Long getBehandlingId() {
            return behandlingId;
        }

        public void setBehandlingId(Long behandlingId) {
            this.behandlingId = behandlingId;
        }

        public String getSaksbehandlerIdent() {
            return saksbehandlerIdent;
        }

        public void setSaksbehandlerIdent(String saksbehandlerIdent) {
            this.saksbehandlerIdent = saksbehandlerIdent;
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_ID, getBehandlingId());
        }
    }

    private void kobleBehandling(KobleBehandlingTilGrunnlagDto dto) {
        Long mottattXmlId = dto.getMottattXmlId();
        Long behandlingId = dto.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (!behandling.isBehandlingPåVent()) {
            throw new UgyldigTvingKoblingForespørselException("Behandling med id " + behandlingId + " er ikke på vent");
        }
        if (!Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG.equals(behandling.getVenteårsak()) && !Venteårsak.VENT_PÅ_BRUKERTILBAKEMELDING.equals(
            behandling.getVenteårsak())) {
            throw new UgyldigTvingKoblingForespørselException("Behandling med id " + behandlingId + " venter ikke på tilbakekrevingsgrunnlag");
        }
        ØkonomiXmlMottatt mottattXml = mottattXmlRepository.finnMottattXml(mottattXmlId);
        if (mottattXml == null) {
            throw new UgyldigTvingKoblingForespørselException("MottattXmlId=" + mottattXmlId + " finnes ikke");
        }
        if (mottattXml.getMottattXml().contains(TaskProperties.ROOT_ELEMENT_KRAV_VEDTAK_STATUS_XML)) {
            throw new UgyldigTvingKoblingForespørselException(
                "MottattXmlId=" + mottattXmlId + " peker ikke på et kravgrunnlag, men på en status-melding");
        }

        fjernKoblingTilHenlagteBehandlinger(mottattXml, behandlingId);
        if (mottattXmlRepository.erMottattXmlTilkoblet(mottattXmlId)) {
            throw new UgyldigTvingKoblingForespørselException(
                "Kravgrunnlaget med mottattXmlId=" + mottattXmlId + " er allerede koblet til en ikke-henlagt behandling");
        }
        if (mottattXml.getSaksnummer() != null && !behandling.getFagsak().getSaksnummer().getVerdi().equals(mottattXml.getSaksnummer())) {
            throw new UgyldigTvingKoblingForespørselException(
                "Kan ikke koble behandling med behandlingId=" + behandlingId + " til kravgrunnlag med mottattXmlId=" + mottattXml
                    + ". Kravgrunnlaget tilhører annen fagsak");
        }
        DetaljertKravgrunnlag kravgrunnlagDto = KravgrunnlagXmlUnmarshaller.unmarshall(mottattXmlId, mottattXml.getMottattXml(), true);
        Kravgrunnlag431 kravgrunnlag = kravgrunnlagMapper.mapTilDomene(kravgrunnlagDto);
        KravgrunnlagValidator.validerGrunnlag(kravgrunnlag);
        grunnlagRepository.lagre(behandlingId, kravgrunnlag);
        mottattXmlRepository.opprettTilkobling(mottattXmlId);
        LOG.info("Behandling med behandlingId={} ble tvunget koblet til kravgrunnlag med mottattXmlId={}", behandlingId, mottattXmlId);
    }

    @POST
    @Path("/hent-vedtak")
    @Operation(
        tags = "FORVALTNING-behandling",
        description = "Tjeneste for å hente tilbakekrevingsvedtak som sendes til oppdragsystemet. Kan kun brukes på behandling som er under iverksettelse",
        responses = {
            @ApiResponse(responseCode = "200", description = "Returnerer vedtak"),
            @ApiResponse(responseCode = "400", description = "Behandlingen har feil status"),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")
        })
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    public Response hentTilbakekrevingVedtak(
        @TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
        @QueryParam("behandlingId") @NotNull @Valid BehandlingReferanse behandlingReferanse) {
        Behandling behandling = hentBehandling(behandlingReferanse);
        if (behandling.getStatus() != BehandlingStatus.IVERKSETTER_VEDTAK) {
            LOG.info("Endepunktet brukes til feilsøking i iverksetting, men denne behandlingen har status {}", behandling.getStatus());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        TilbakekrevingVedtakDTO vedtak = tilbakekrevingsvedtakTjeneste.lagTilbakekrevingsvedtak(behandling.getId());
        return Response.ok(vedtak).build();
    }

    private void fjernKoblingTilHenlagteBehandlinger(ØkonomiXmlMottatt mottattXml, Long behandlingIdSomTilkobles) {
        List<EksternBehandling> koblingTilBehandlinger = eksternBehandlingRepository.hentFlereFraHenvisning(mottattXml.getHenvisning());
        boolean alleKoblingerFjernes = true;
        for (EksternBehandling eksternBehandling : koblingTilBehandlinger) {
            Long kobletBehandlingId = eksternBehandling.getInternId();
            Behandling behandling = behandlingRepository.hentBehandling(kobletBehandlingId);
            Optional<Behandlingsresultat> resultat = behandlingresultatRepository.hent(behandling);
            if (kobletBehandlingId.equals(behandlingIdSomTilkobles)) {
                //aktuell behandling skal ikke hindre frakobling, selv om ikke henlagt
                continue;
            }
            if (resultat.isPresent() && resultat.get().erBehandlingHenlagt()) {
                eksternBehandlingRepository.deaktivateTilkobling(eksternBehandling.getInternId());
                LOG.info("Deaktiverer kobling mellom behandlingId {} og mottattXmlId {} for {}", eksternBehandling.getInternId(), mottattXml.getId(),
                    mottattXml.getSaksnummer());
            } else {
                alleKoblingerFjernes = false;
                LOG.info("Kan ikke fjerne tilkobling til behandling {}, denne er ikke henlagt siden behandlingsresultat er {}", kobletBehandlingId,
                    resultat);
            }
        }
        if (alleKoblingerFjernes) {
            mottattXmlRepository.fjernTilkobling(mottattXml.getId());
            LOG.info("Fjener tilkobling ØkonomiMottattXml id {}", mottattXml.getId());
        }
    }

    static class UgyldigTvingKoblingForespørselException extends IllegalArgumentException {
        public UgyldigTvingKoblingForespørselException(String beskjed) {
            super(beskjed);
        }
    }

    private void opprettGjenopptaBehandlingTask(Behandling behandling) {
        var fortsettTaskData = opprettFortsettBehandlingTask(behandling);
        fortsettTaskData.setProperty(FortsettBehandlingTask.MANUELL_FORTSETTELSE, "true");
        if (behandling.getAktivtBehandlingSteg() != null) {
            fortsettTaskData.setProperty(FortsettBehandlingTask.GJENOPPTA_STEG, behandling.getAktivtBehandlingSteg().getKode());
        }
        taskTjeneste.lagre(fortsettTaskData);
    }

    private ProsessTaskData opprettFortsettBehandlingTask(Behandling behandling) {
        var fortsettTaskData = ProsessTaskData.forProsessTask(FortsettBehandlingTask.class);
        fortsettTaskData.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getFagsakId(), behandling.getId());
        return fortsettTaskData;
    }

    private void opprettHenleggBehandlingTask(Behandling behandling) {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(TvingHenlegglBehandlingTask.class);
        prosessTaskData.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getFagsakId(), behandling.getId());
        taskTjeneste.lagre(prosessTaskData);
    }

    private void opprettKorrigertHenvisningTask(Behandling behandling, UUID eksternBehandlingUuid) {
        ProsessTaskData prosessTaskData = ProsessTaskData.forProsessTask(KorrigertHenvisningTask.class);
        prosessTaskData.setBehandling(behandling.getSaksnummer().getVerdi(), behandling.getFagsakId(), behandling.getId());
        prosessTaskData.setProperty(KorrigertHenvisningTask.PROPERTY_EKSTERN_UUID, eksternBehandlingUuid.toString());
        taskTjeneste.lagre(prosessTaskData);
    }

    private Behandling hentBehandling(BehandlingReferanse behandlingReferanse) {
        Behandling behandling;
        if (behandlingReferanse.erInternBehandlingId()) {
            behandling = behandlingRepository.hentBehandling(behandlingReferanse.getBehandlingId());
        } else {
            behandling = behandlingRepository.hentBehandling(behandlingReferanse.getBehandlingUuid());
        }
        return behandling;
    }

}
