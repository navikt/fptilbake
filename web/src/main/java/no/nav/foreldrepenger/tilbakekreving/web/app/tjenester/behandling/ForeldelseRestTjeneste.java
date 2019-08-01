package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.FeilutbetalingPerioderDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.dto.PeriodeDto;
import no.nav.foreldrepenger.tilbakekreving.behandling.impl.VurdertForeldelseTjeneste;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = "foreldelse")
@Path(ForeldelseRestTjeneste.PATH_FRAGMENT)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@ApplicationScoped
@Transaction
public class ForeldelseRestTjeneste {

    public static final String PATH_FRAGMENT = "/foreldelse";
    private VurdertForeldelseTjeneste vurdertForeldelseTjeneste;

    public ForeldelseRestTjeneste() {
        // For CDI
    }

    @Inject
    public ForeldelseRestTjeneste(VurdertForeldelseTjeneste vurdertForeldelseTjeneste) {
        this.vurdertForeldelseTjeneste = vurdertForeldelseTjeneste;
    }

    @GET
    @ApiOperation(value = "Henter perioder som skal vurderes for foreldelse")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public FeilutbetalingPerioderDto hentLogiskePerioder(@QueryParam("behandlingId") @NotNull @Valid BehandlingIdDto behandlingIdDto) {
        return vurdertForeldelseTjeneste.hentFaktaPerioder(behandlingIdDto.getBehandlingId());
    }

    @GET
    @ApiOperation(value = "Hente allerede vurdert foreldelse perioder")
    @Path("/vurdert")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public FeilutbetalingPerioderDto hentVurdertPerioder(@QueryParam("behandlingId") @NotNull @Valid BehandlingIdDto behandlingIdDto) {
        return vurdertForeldelseTjeneste.henteVurdertForeldelse(behandlingIdDto.getBehandlingId());
    }

    @POST
    @ApiOperation(value = "Beregn feilutbetalingsbeløp for oppgitte perioder")
    @Path("/belop")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public FeilutbetalingPerioderDto beregnBeløp(@NotNull @Valid FeilutbetalingPerioderDto perioderDto) {
        List<Periode> perioderFraDto = perioderDto.getPerioder().stream().map(PeriodeDto::tilPeriode).collect(Collectors.toList());
        Map<Periode, BigDecimal> feilutbetalinger = vurdertForeldelseTjeneste.beregnFeilutbetaltBeløpForPerioder(perioderDto.getBehandlingId(), perioderFraDto);
        for (PeriodeDto dto : perioderDto.getPerioder()) {
            dto.setBelop(feilutbetalinger.get(dto.tilPeriode()));
        }
        return perioderDto;
    }

}
