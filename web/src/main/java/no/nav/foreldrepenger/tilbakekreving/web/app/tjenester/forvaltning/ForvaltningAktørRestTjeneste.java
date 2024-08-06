package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AbacProperty.DRIFT;

import java.util.function.Function;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning.dto.ByttAktørRequest;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@Path("/forvaltning/aktør")
@ApplicationScoped
@Transactional
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ForvaltningAktørRestTjeneste {

    private static final String GAMMEL = "gammel";
    private static final String GJELDENDE = "gjeldende";

    private EntityManager entityManager;

    public ForvaltningAktørRestTjeneste() {
        // For CDI
    }

    @Inject
    public ForvaltningAktørRestTjeneste(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    @POST
    @Path("/oppdaterAktoerId")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "MERGE: Oppdaterer aktørid for bruker i nødvendige tabeller", tags = "FORVALTNING", responses = {@ApiResponse(responseCode = "200", description = "Forekomster av utgått aktørid erstattet.")})
    @BeskyttetRessurs(actionType = ActionType.CREATE, property = DRIFT)
    public Response oppdaterAktoerId(@TilpassetAbacAttributt(supplierClass = AktørRequestAbacDataSupplier.class) @NotNull @Valid ByttAktørRequest request) {
        int antall = oppdaterAktørIdFor(request.getUtgåttAktør().getId(), request.getGyldigAktør().getId());
        return Response.ok(antall).build();
    }

    private int oppdaterAktørIdFor(String gammel, String gjeldende) {
        int antall = 0;
        antall += entityManager.createNativeQuery("UPDATE VERGE SET aktoer_id = :gjeldende WHERE aktoer_id = :gammel")
            .setParameter(GJELDENDE, gjeldende)
            .setParameter(GAMMEL, gammel)
            .executeUpdate();
        antall += entityManager.createNativeQuery("UPDATE BRUKER SET aktoer_id = :gjeldende WHERE aktoer_id = :gammel")
            .setParameter(GJELDENDE, gjeldende)
            .setParameter(GAMMEL, gammel)
            .executeUpdate();
        entityManager.flush();
        return antall;
    }


    public static class AktørRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        public AktørRequestAbacDataSupplier() {
            // Jackson
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (ByttAktørRequest) obj;
            return AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.AKTØR_ID, req.getGyldigAktør().getId());
        }
    }


}
