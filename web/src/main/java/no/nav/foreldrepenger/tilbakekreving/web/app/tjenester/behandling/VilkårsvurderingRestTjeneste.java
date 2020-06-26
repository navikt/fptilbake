package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

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
import no.nav.foreldrepenger.tilbakekreving.behandling.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.vilkårsvurdering.VilkårsvurderingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.felles.Rettsgebyr;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.DetaljerteFeilutbetalingsperioderDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.VilkårsvurderteDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Path(VilkårsvurderingRestTjeneste.PATH_FRAGMENT)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@ApplicationScoped
public class VilkårsvurderingRestTjeneste {

    public static final String PATH_FRAGMENT = "/vilkarsvurdering";
    private VilkårsvurderingTjeneste vilkårsvurderingTjeneste;
    private BehandlingTjeneste behandlingTjeneste;
    private Integer rettsgebyr;

    public VilkårsvurderingRestTjeneste() {
        // for CDI
    }

    @Inject
    public VilkårsvurderingRestTjeneste(VilkårsvurderingTjeneste vilkårsvurderingTjeneste,
                                        BehandlingTjeneste behandlingTjeneste) {
        this.vilkårsvurderingTjeneste = vilkårsvurderingTjeneste;
        this.behandlingTjeneste = behandlingTjeneste;
        this.rettsgebyr = new Rettsgebyr().getGebyr();
    }

    @GET
    @Path("/perioder")
    @Operation(tags = "vilkårsvurdering", description = "Henter perioder som skal vurderes for vilkårsvurdering")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public DetaljerteFeilutbetalingsperioderDto hentDetailjertFeilutbetalingPerioder(@QueryParam("behandlingUuid") @NotNull @Valid BehandlingReferanse behandlingReferanse) {
        DetaljerteFeilutbetalingsperioderDto perioderDto = new DetaljerteFeilutbetalingsperioderDto();
        perioderDto.setPerioder(vilkårsvurderingTjeneste.hentDetaljertFeilutbetalingPerioder(hentBehandlingId(behandlingReferanse)));
        perioderDto.setRettsgebyr(BigDecimal.valueOf(rettsgebyr));
        return perioderDto;
    }

    @GET
    @Path("/vurdert")
    @Operation(tags = "vilkårsvurdering", description = "Henter allerede vurdert vilkårsvurdering")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public VilkårsvurderteDto hentVurdertPerioder(@QueryParam("behandlingUuid") @NotNull @Valid BehandlingReferanse behandlingReferanse) {
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
