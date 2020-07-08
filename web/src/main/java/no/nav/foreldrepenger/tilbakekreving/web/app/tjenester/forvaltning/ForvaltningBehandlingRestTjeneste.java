package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.DRIFT;

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTask;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.TaskProperty;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagMapper;
import no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.førstegang.KravgrunnlagXmlUnmarshaller;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagValidator;
import no.nav.foreldrepenger.tilbakekreving.integrasjon.økonomi.TilbakekrevingsvedtakMarshaller;
import no.nav.foreldrepenger.tilbakekreving.iverksettevedtak.tjeneste.TilbakekrevingsvedtakTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.MeldingType;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiMottattXmlRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiSendtXmlRepository;
import no.nav.foreldrepenger.tilbakekreving.økonomixml.ØkonomiXmlMottatt;
import no.nav.okonomi.tilbakekrevingservice.TilbakekrevingsvedtakRequest;
import no.nav.tilbakekreving.kravgrunnlag.detalj.v1.DetaljertKravgrunnlag;
import no.nav.tilbakekreving.tilbakekrevingsvedtak.vedtak.v1.TilbakekrevingsvedtakDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Path("/forvaltningBehandling")
@ApplicationScoped
@Transactional
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ForvaltningBehandlingRestTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(ForvaltningBehandlingRestTjeneste.class);

    private BehandlingRepository behandlingRepository;
    private ProsessTaskRepository prosessTaskRepository;
    private ØkonomiMottattXmlRepository mottattXmlRepository;
    private KravgrunnlagRepository grunnlagRepository;
    private KravgrunnlagMapper kravgrunnlagMapper;
    private ØkonomiSendtXmlRepository økonomiSendtXmlRepository;
    private TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste;

    public ForvaltningBehandlingRestTjeneste() {
        // for CDI
    }

    @Inject
    public ForvaltningBehandlingRestTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                             ProsessTaskRepository prosessTaskRepository,
                                             ØkonomiMottattXmlRepository mottattXmlRepository,
                                             KravgrunnlagMapper kravgrunnlagMapper,
                                             ØkonomiSendtXmlRepository økonomiSendtXmlRepository,
                                             TilbakekrevingsvedtakTjeneste tilbakekrevingsvedtakTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.prosessTaskRepository = prosessTaskRepository;
        this.mottattXmlRepository = mottattXmlRepository;
        this.grunnlagRepository = repositoryProvider.getGrunnlagRepository();
        this.kravgrunnlagMapper = kravgrunnlagMapper;
        this.økonomiSendtXmlRepository = økonomiSendtXmlRepository;
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
    @BeskyttetRessurs(action = CREATE, ressurs = DRIFT)
    public Response tvingHenleggelseBehandling(@QueryParam("behandlingId") @NotNull @Valid BehandlingReferanse behandlingReferanse) {
        Behandling behandling = hentBehandling(behandlingReferanse);
        if (behandling.erAvsluttet()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        logger.info("Tving henleggelse. Oppretter task for å henlegge behandlingId={}", behandlingReferanse.getBehandlingId());
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
    @BeskyttetRessurs(action = CREATE, ressurs = DRIFT)
    public Response tvingGjenopptaBehandling(@NotNull @QueryParam("behandlingId") @Valid BehandlingReferanse behandlingReferanse) {
        Behandling behandling = hentBehandling(behandlingReferanse);
        if (behandling.erAvsluttet() || !behandling.isBehandlingPåVent()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        logger.info("Tving gjenoppta. Oppretter task for å gjenoppta behandlingId={}", behandlingReferanse.getBehandlingId());
        opprettGjenopptaBehandlingTask(behandling);

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
    @BeskyttetRessurs(action = CREATE, ressurs = DRIFT)
    public Response tvingkobleBehandlingTilGrunnlag(@Valid @NotNull KobleBehandlingTilGrunnlagDto behandlingTilGrunnlagDto) {
        try {
            kobleBehandling(behandlingTilGrunnlagDto);
            return Response.ok().build();
        } catch (UgyldigTvingKoblingForespørselException e) {
            String message = LoggerUtils.removeLineBreaks(e.getMessage());
            logger.info(message);
            return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
        } catch (KravgrunnlagValidator.UgyldigKravgrunnlagException e) {
            String message = LoggerUtils.removeLineBreaks("Kunne ikke koble behandling med behandlingId=" + behandlingTilGrunnlagDto.getBehandlingId() + " til kravgrunnlag med mottattXmlId=" + behandlingTilGrunnlagDto.getMottattXmlId() + " siden kravgrunnlaget ikke er gyldig: ");
            logger.info(message, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message + e.getMessage()).build();
        }
    }

    @POST
    @Path("/hent-korrigert-grunnlag")
    @Operation(
        tags = "FORVALTNING-behandling",
        description = "Tjeneste for å hente korrigert grunnlag for en behandling",
        responses = {
            @ApiResponse(responseCode = "200", description = "Hent korrigerte grunnlag og tilkoblet det med en behandling"),
            @ApiResponse(responseCode = "400", description = "Behandling er avsluttet eller ikke gyldig")
        })
    @BeskyttetRessurs(action = CREATE, ressurs = DRIFT)
    public Response hentKorrigertKravgrunnlag(@Valid @NotNull HentKorrigertKravgrunnlagDto hentKorrigertKravgrunnlagDto) {
        Behandling behandling = behandlingRepository.hentBehandling(hentKorrigertKravgrunnlagDto.getBehandlingId());
        if (behandling.erAvsluttet()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Kan ikke hente korrigert kravbrunnlag, behandlingen er avsluttet").build();
        }
        logger.info("Oppretter task for å hente korrigert kravgrunnlag for behandlingId={}", behandling.getId());
        opprettHentKorrigertGrunnlagTask(behandling, hentKorrigertKravgrunnlagDto.getKravgrunnlagId());
        return Response.ok().build();
    }

    @POST
    @Path("/hent-oko-xml-feilet-iverksetting")
    @Operation(
        tags = "FORVALTNING-behandling",
        description = "Tjeneste for å hente xml til økonomi ved feilet iverksetting",
        responses = {
            @ApiResponse(responseCode = "200", description = "Hent xml til økonomi"),
            @ApiResponse(responseCode = "400", description = "Behandling eksisterer ikke")
        })
    @BeskyttetRessurs(action = READ, ressurs = DRIFT)
    public Response hentOkoXmlForFeiletIverksetting(@NotNull @QueryParam("behandlingId") @Valid BehandlingReferanse behandlingReferanse) {
        String behandlingRef = behandlingReferanse.erInternBehandlingId()
            ? behandlingReferanse.getBehandlingId().toString()
            : behandlingReferanse.getBehandlingUuid().toString();
        logger.info("Henter xml til økonomi for behandling: {}", behandlingRef);

        Behandling behandling = hentBehandling(behandlingReferanse);
        if (behandling == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Long behandlingId = behandling.getId();
        Collection<String> meldinger = økonomiSendtXmlRepository.finnXml(behandlingId, MeldingType.VEDTAK);
        if (meldinger.isEmpty()) {
            logger.info("Xml til økonomi ikke lagret i databasen for behandling: {}", behandlingRef);
            String xml = lagXmlTilØkonomi(behandlingId);
            return Response.ok()
                .type(MediaType.APPLICATION_XML)
                .entity(xml)
                .build();
        } else if (meldinger.size() == 1) {
            logger.info("Fant lagret xml til økonomi for behandling: {}", behandlingRef);
            return Response.ok()
                .type(MediaType.APPLICATION_XML)
                .entity(meldinger.toArray()[0])
                .build();
        } else {
            logger.info("Fant {} lagrede xmler til økonomi for behandling: {}", meldinger.size(), behandlingRef);
            return Response.ok()
                .entity(meldinger)
                .build();
        }
    }

    private String lagXmlTilØkonomi(Long behandlingId) {
        TilbakekrevingsvedtakDto tilbakekrevingsvedtak = tilbakekrevingsvedtakTjeneste.lagTilbakekrevingsvedtak(behandlingId);
        TilbakekrevingsvedtakRequest request = new TilbakekrevingsvedtakRequest();
        request.setTilbakekrevingsvedtak(tilbakekrevingsvedtak);
        return TilbakekrevingsvedtakMarshaller.marshall(behandlingId, request);
    }

    private void kobleBehandling(KobleBehandlingTilGrunnlagDto dto) {
        Long mottattXmlId = dto.getMottattXmlId();
        Long behandlingId = dto.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        if (!behandling.isBehandlingPåVent()) {
            throw new UgyldigTvingKoblingForespørselException("Behandling med id " + behandlingId + " er ikke på vent");
        }
        if (!Venteårsak.VENT_PÅ_TILBAKEKREVINGSGRUNNLAG.equals(behandling.getVenteårsak())) {
            throw new UgyldigTvingKoblingForespørselException("Behandling med id " + behandlingId + " venter ikke på tilbakekrevingsgrunnlag");
        }
        ØkonomiXmlMottatt mottattXml = mottattXmlRepository.finnMottattXml(mottattXmlId);
        if (mottattXml == null) {
            throw new UgyldigTvingKoblingForespørselException("MottattXmlId=" + mottattXmlId + " finnes ikke");
        }
        if (mottattXml.getMottattXml().contains(TaskProperty.ROOT_ELEMENT_KRAV_VEDTAK_STATUS_XML)) {
            throw new UgyldigTvingKoblingForespørselException("MottattXmlId=" + mottattXmlId + " peker ikke på et kravgrunnlag, men på en status-melding");
        }
        if (mottattXmlRepository.erMottattXmlTilkoblet(mottattXmlId)) {
            throw new UgyldigTvingKoblingForespørselException("Kravgrunnlaget med mottattXmlId=" + mottattXmlId + " er allerede koblet til en behandling");
        }
        if (mottattXml.getSaksnummer() != null && !behandling.getFagsak().getSaksnummer().getVerdi().equals(mottattXml.getSaksnummer())) {
            throw new UgyldigTvingKoblingForespørselException("Kan ikke koble behandling med behandlingId=" + behandlingId + " til kravgrunnlag med mottattXmlId=" + mottattXml + ". Kravgrunnlaget tilhører annen fagsak");
        }
        DetaljertKravgrunnlag kravgrunnlagDto = KravgrunnlagXmlUnmarshaller.unmarshall(mottattXmlId, mottattXml.getMottattXml());
        Kravgrunnlag431 kravgrunnlag = kravgrunnlagMapper.mapTilDomene(kravgrunnlagDto);
        KravgrunnlagValidator.validerGrunnlag(kravgrunnlag);
        grunnlagRepository.lagre(behandlingId, kravgrunnlag);
        mottattXmlRepository.opprettTilkobling(mottattXmlId);
        logger.info("Behandling med behandlingId={} ble tvunget koblet til kravgrunnlag med mottattXmlId={}", behandlingId, mottattXmlId);
    }

    static class UgyldigTvingKoblingForespørselException extends IllegalArgumentException {
        public UgyldigTvingKoblingForespørselException(String beskjed) {
            super(beskjed);
        }
    }

    private void opprettGjenopptaBehandlingTask(Behandling behandling) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(GjenopptaBehandlingTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskRepository.lagre(prosessTaskData);
    }

    private void opprettHenleggBehandlingTask(Behandling behandling) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(TvingHenlegglBehandlingTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskRepository.lagre(prosessTaskData);
    }

    private void opprettHentKorrigertGrunnlagTask(Behandling behandling, String kravgrunnlagId) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(HentKorrigertKravgrunnlagTask.TASKTYPE);
        prosessTaskData.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        prosessTaskData.setProperty("KRAVGRUNNLAG_ID", kravgrunnlagId);
        prosessTaskRepository.lagre(prosessTaskData);
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
