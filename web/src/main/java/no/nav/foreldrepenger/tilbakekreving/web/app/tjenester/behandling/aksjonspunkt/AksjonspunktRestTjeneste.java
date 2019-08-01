package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.TotrinnRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.AksjonspunktDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.AksjonspunktDtoMapper;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftedeAksjonspunkterDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.Redirect;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = {"aksjonspunkt"})
@Path("/behandling/aksjonspunkt")
@RequestScoped
@Transaction
public class AksjonspunktRestTjeneste {

    private BehandlingRepository behandlingRepository;
    private TotrinnRepository totrinnRepository;
    private BehandlingTjeneste behandlingTjeneste;
    private AksjonspunktApplikasjonTjeneste aksjonspunktApplikasjonTjeneste;

    public AksjonspunktRestTjeneste() {
        // Bare for RESTeasy
    }

    @Inject
    public AksjonspunktRestTjeneste(BehandlingRepository behandlingRepository,
                                    TotrinnRepository totrinnRepository,
                                    BehandlingTjeneste behandlingTjeneste,
                                    AksjonspunktApplikasjonTjeneste aksjonspunktApplikasjonTjeneste) {
        this.behandlingRepository = behandlingRepository;
        this.totrinnRepository = totrinnRepository;
        this.behandlingTjeneste = behandlingTjeneste;
        this.aksjonspunktApplikasjonTjeneste = aksjonspunktApplikasjonTjeneste;
    }

    @GET
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Hent aksjonspunter for en behandling", response = AksjonspunktDto.class, responseContainer = "Set")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response getAksjonspunkter(@NotNull @QueryParam("behandlingId") @Valid BehandlingIdDto behandlingIdDto) throws URISyntaxException { // NOSONAR
        Behandling behandling = behandlingRepository.hentBehandling(behandlingIdDto.getBehandlingId());
        Collection<Totrinnsvurdering> totrinnsvurderinger = totrinnRepository.hentTotrinnsvurderinger(behandling);
        Set<AksjonspunktDto> dto = AksjonspunktDtoMapper.lagAksjonspunktDto(behandling, totrinnsvurderinger);
        CacheControl cc = new CacheControl();
        cc.setNoCache(true);
        cc.setNoStore(true);
        cc.setMaxAge(0);
        return Response.ok(dto).cacheControl(cc).build();
    }

    /**
     * Håndterer prosessering av aksjonspunkt og videre behandling.
     * <p>
     * MERK: Det skal ikke ligge spesifikke sjekker som avhenger av status på behanlding, steg eller knytning til
     * spesifikke aksjonspunkter idenne tjenesten.
     *
     * @throws URISyntaxException
     */
    @POST
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Lagre endringer gitt av aksjonspunktene og rekjør behandling fra gjeldende steg")
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response bekreft(@ApiParam("Liste over aksjonspunkt som skal bekreftes, inklusiv data som trengs for å løse de.") @Valid BekreftedeAksjonspunkterDto apDto) throws URISyntaxException { // NOSONAR
        Long behandlingId = apDto.getBehandlingId().getBehandlingId();
        Collection<BekreftetAksjonspunktDto> bekreftedeAksjonspunktDtoer = apDto.getBekreftedeAksjonspunktDtoer();
        behandlingTjeneste.kanEndreBehandling(behandlingId, apDto.getBehandlingVersjon());
        aksjonspunktApplikasjonTjeneste.bekreftAksjonspunkter(bekreftedeAksjonspunktDtoer, behandlingId);
        return Redirect.tilBehandlingPollStatus(behandlingId);
    }
}