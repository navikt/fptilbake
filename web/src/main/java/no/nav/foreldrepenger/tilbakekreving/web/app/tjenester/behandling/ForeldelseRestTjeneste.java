package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.BehandlingReferanse;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FeilutbetalingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.ForeldelsePeriodeMedBeløpDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.BehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.KravgrunnlagBeregningTjeneste;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto.BehandlingReferanseAbacAttributter;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.felles.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@Path(ForeldelseRestTjeneste.PATH_FRAGMENT)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@ApplicationScoped
@Transactional
public class ForeldelseRestTjeneste {

    public static final String PATH_FRAGMENT = "/foreldelse";
    private VurdertForeldelseTjeneste vurdertForeldelseTjeneste;
    private KravgrunnlagBeregningTjeneste kravgrunnlagBeregningTjeneste;
    private BehandlingTjeneste behandlingTjeneste;

    public ForeldelseRestTjeneste() {
        // For CDI
    }

    @Inject
    public ForeldelseRestTjeneste(VurdertForeldelseTjeneste vurdertForeldelseTjeneste,
                                  KravgrunnlagBeregningTjeneste kravgrunnlagBeregningTjeneste,
                                  BehandlingTjeneste behandlingTjeneste) {
        this.vurdertForeldelseTjeneste = vurdertForeldelseTjeneste;
        this.kravgrunnlagBeregningTjeneste = kravgrunnlagBeregningTjeneste;
        this.behandlingTjeneste = behandlingTjeneste;
    }

    @GET
    @Operation(tags = "foreldelse", description = "Henter perioder som skal vurderes for foreldelse")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    public FeilutbetalingPerioderDto hentLogiskePerioder(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class) @QueryParam("uuid") @NotNull @Valid BehandlingReferanse behandlingReferanse) {
        return vurdertForeldelseTjeneste.hentFaktaPerioder(hentBehandlingId(behandlingReferanse));
    }

    @GET
    @Operation(tags = "foreldelse", description = "Hente allerede vurdert foreldelse perioder")
    @Path("/vurdert")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    public FeilutbetalingPerioderDto hentVurdertPerioder(@TilpassetAbacAttributt(supplierClass = BehandlingReferanseAbacAttributter.AbacDataBehandlingReferanse.class) @QueryParam("uuid") @NotNull @Valid BehandlingReferanse behandlingReferanse) {
        return vurdertForeldelseTjeneste.henteVurdertForeldelse(hentBehandlingId(behandlingReferanse));
    }

    @POST
    @Operation(tags = "foreldelse", description = "Beregn feilutbetalingsbeløp for oppgitte perioder")
    @Path("/belop")
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.FAGSAK)
    public FeilutbetalingPerioderDto beregnBeløp(@TilpassetAbacAttributt(supplierClass = AbacDataPerioder.class) @NotNull @Valid FeilutbetalingPerioderDto perioderDto) {
        var perioderFraDto = perioderDto.getPerioder()
                .stream()
                .map(ForeldelsePeriodeMedBeløpDto::tilPeriode)
                .collect(Collectors.toList());
        var behandlingId = perioderDto.getBehandlingId() != null ? perioderDto.getBehandlingId()
                : behandlingTjeneste.hentBehandlingId(perioderDto.getBehandlingUuid());
        var feilutbetalinger = kravgrunnlagBeregningTjeneste.beregnFeilutbetaltBeløp(behandlingId, perioderFraDto);
        for (var dto : perioderDto.getPerioder()) {
            dto.setBelop(feilutbetalinger.get(dto.tilPeriode()));
        }
        return perioderDto;
    }

    private Long hentBehandlingId(BehandlingReferanse behandlingReferanse) {
        return behandlingReferanse.erInternBehandlingId() ? behandlingReferanse.getBehandlingId() : behandlingTjeneste.hentBehandlingId(
                behandlingReferanse.getBehandlingUuid());
    }

    public static class AbacDataPerioder implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (FeilutbetalingPerioderDto) obj;
            var attributter = AbacDataAttributter.opprett();
            if (req.getBehandlingUuid() != null) {
                attributter.leggTil(StandardAbacAttributtType.BEHANDLING_UUID, req.getBehandlingUuid());
            }
            if (req.getBehandlingId() != null) {
                attributter.leggTil(StandardAbacAttributtType.BEHANDLING_ID, req.getBehandlingId());
            }
            return attributter;
        }
    }
}
