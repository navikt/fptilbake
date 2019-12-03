package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.brevmaler.DokumentBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto.BrevmalDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BehandlingIdDto;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.BestillBrevDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Path(BrevRestTjeneste.PATH_FRAGMENT)
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RequestScoped
@Transaction
public class BrevRestTjeneste {

    public static final String PATH_FRAGMENT = "/brev"; // NOSONAR

    private DokumentBehandlingTjeneste dokumentBehandlingTjeneste;

    public BrevRestTjeneste() {
        // CDI
    }

    @Inject
    public BrevRestTjeneste(DokumentBehandlingTjeneste dokumentBehandlingTjeneste) {
        this.dokumentBehandlingTjeneste = dokumentBehandlingTjeneste;
    }


    @GET
    @Timed
    @Path("/maler")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Operation(tags = "brev", description = "Henter liste over tilgjengelige brevtyper")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<BrevmalDto> hentMaler(@Valid @QueryParam("behandlingId") BehandlingIdDto behandlingIdDto) {
        return dokumentBehandlingTjeneste.hentBrevmalerFor(behandlingIdDto.getBehandlingId());
    }

    @POST
    @Timed
    @Path("/bestill")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Operation(tags = "brev", description = "bestiller brev")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response bestillBrev(@NotNull @Valid BestillBrevDto bestillBrevDto) {
        DokumentMalType malType = DokumentMalType.fraKode(bestillBrevDto.getBrevmalkode());
        dokumentBehandlingTjeneste.bestillBrev(bestillBrevDto.getBehandlingId(), malType, bestillBrevDto.getFritekst());
        return Response.ok().build();
    }

    @POST
    @Timed
    @Path("/forhandsvis")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(tags = "brev", description = "Returnerer en pdf som er en forhåndsvisning av brevet")
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response forhåndsvisBrev(@Parameter(description = "Inneholder kode til brevmal og data som skal flettes inn i brevet") @NotNull @Valid BestillBrevDto forhåndsvisBestillBrevDto) {
        DokumentMalType malType = DokumentMalType.fraKode(forhåndsvisBestillBrevDto.getBrevmalkode());
        String fritekst = forhåndsvisBestillBrevDto.getFritekst();
        long behandlingId = forhåndsvisBestillBrevDto.getBehandlingId();
        byte[] dokument = dokumentBehandlingTjeneste.forhåndsvisBrev(behandlingId, malType, fritekst);

        Response.ResponseBuilder responseBuilder = Response.ok(dokument);
        responseBuilder.type("application/pdf");
        responseBuilder.header("Content-Disposition", "filename=dokument.pdf");
        return responseBuilder.build();
    }

}
