package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering.VilkårsvurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.DetaljerteFeilutbetalingsperioderDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.VilkårsvurderteDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.BehandlingReferanseAbacAttributter;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@Path(VilkårsvurderingRestTjeneste.PATH_FRAGMENT)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@ApplicationScoped
public class VilkårsvurderingRestTjeneste {

    public static final String PATH_FRAGMENT = "/vilkarsvurdering";
    private VilkårsvurderingTjeneste vilkårsvurderingTjeneste;
    private BehandlingTjeneste behandlingTjeneste;
    private KravgrunnlagBeregningTjeneste beregningTjeneste;

    public VilkårsvurderingRestTjeneste() {
        // for CDI
    }

    @Inject
    public VilkårsvurderingRestTjeneste(VilkårsvurderingTjeneste vilkårsvurderingTjeneste,
                                        BehandlingTjeneste behandlingTjeneste,
                                        KravgrunnlagBeregningTjeneste beregningTjeneste) {
        this.vilkårsvurderingTjeneste = vilkårsvurderingTjeneste;
        this.behandlingTjeneste = behandlingTjeneste;
        this.beregningTjeneste = beregningTjeneste;
    }

    @GET
    @Path("/perioder")
    @Operation(tags = "vilkårsvurdering", description = "Henter perioder som skal vurderes for vilkårsvurdering")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    public DetaljerteFeilutbetalingsperioderDto hentDetailjertFeilutbetalingPerioder(
            @TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
            @QueryParam("uuid") @NotNull @Valid BehandlingReferanse behandlingReferanse) {
        var behandlingId = hentBehandlingId(behandlingReferanse);
        var behandling = behandlingTjeneste.hentBehandling(behandlingId);
        DetaljerteFeilutbetalingsperioderDto perioderDto = new DetaljerteFeilutbetalingsperioderDto();
        perioderDto.setPerioder(vilkårsvurderingTjeneste.hentDetaljertFeilutbetalingPerioder(hentBehandlingId(behandlingReferanse)));
        perioderDto.setRettsgebyr(beregningTjeneste.heltRettsgebyrFor(behandlingId, behandling.getOpprettetTidspunkt()));
        return perioderDto;
    }

    @GET
    @Path("/vurdert")
    @Operation(tags = "vilkårsvurdering", description = "Henter allerede vurdert vilkårsvurdering")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    public VilkårsvurderteDto hentVurdertPerioder(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class)
                                                  @QueryParam("uuid") @NotNull @Valid BehandlingReferanse behandlingReferanse) {
        VilkårsvurderteDto vilkårsvurderteDto = new VilkårsvurderteDto();
        vilkårsvurderteDto.setVilkarsVurdertePerioder(vilkårsvurderingTjeneste.hentVilkårsvurdering(hentBehandlingId(behandlingReferanse)));
        return vilkårsvurderteDto;
    }

    private Long hentBehandlingId(BehandlingReferanse behandlingReferanse) {
        return behandlingReferanse.erInternBehandlingId()
                ? behandlingReferanse.getBehandlingId()
                : behandlingTjeneste.hentBehandlingId(behandlingReferanse.getBehandlingUuid());
    }
}
