package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import java.math.BigDecimal;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering.VilkårsvurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.felles.Rettsgebyr;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.DetaljerteFeilutbetalingsperioderDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.VilkårsvurderteDto;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Path(VilkårsvurderingRestTjeneste.PATH_FRAGMENT)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@ApplicationScoped
public class VilkårsvurderingRestTjeneste {

    public static final String PATH_FRAGMENT = "/vilkarsvurdering";
    private VilkårsvurderingTjeneste vilkårsvurderingTjeneste;
    private Integer rettsgebyr;

    public VilkårsvurderingRestTjeneste() {
        // for CDI
    }

    @Inject
    public VilkårsvurderingRestTjeneste(VilkårsvurderingTjeneste vilkårsvurderingTjeneste) {
        this.vilkårsvurderingTjeneste = vilkårsvurderingTjeneste;
        this.rettsgebyr = new Rettsgebyr().getGebyr();
    }

    @GET
    @Path("/perioder")
    @Operation(tags = "vilkårsvurdering", description = "Henter perioder som skal vurderes for vilkårsvurdering")
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    public DetaljerteFeilutbetalingsperioderDto hentDetailjertFeilutbetalingPerioder(@QueryParam("behandlingId") @NotNull @Valid BehandlingIdDto behandlingIdDto) {
        DetaljerteFeilutbetalingsperioderDto perioderDto = new DetaljerteFeilutbetalingsperioderDto();
        perioderDto.setPerioder(vilkårsvurderingTjeneste.hentDetaljertFeilutbetalingPerioder(behandlingIdDto.getBehandlingId()));
        perioderDto.setRettsgebyr(BigDecimal.valueOf(rettsgebyr));
        return perioderDto;
    }

    @GET
    @Path("/vurdert")
    @Operation(tags = "vilkårsvurdering", description = "Henter allerede vurdert vilkårsvurdering")
    @BeskyttetRessurs(action = READ, property = AbacProperty.FAGSAK)
    public VilkårsvurderteDto hentVurdertPerioder(@QueryParam("behandlingId") @NotNull @Valid BehandlingIdDto behandlingIdDto) {
        VilkårsvurderteDto vilkårsvurderteDto = new VilkårsvurderteDto();
        vilkårsvurderteDto.setVilkarsVurdertePerioder(vilkårsvurderingTjeneste.hentVilkårsvurdering(behandlingIdDto.getBehandlingId()));
        return vilkårsvurderteDto;
    }
}
