package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.forvaltning;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.HttpHeaders;

import jakarta.ws.rs.core.MediaType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.Fagsystem;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;

import org.hibernate.jpa.AvailableHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.AbacProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

import static jakarta.ws.rs.core.Response.Status.NOT_IMPLEMENTED;


@Path("/forvaltningUttrekk")
@ApplicationScoped
@Transactional
public class ForvaltningUttrekkRestTjeneste {
    private static final Logger LOG = LoggerFactory.getLogger(ForvaltningUttrekkRestTjeneste.class);
    private static final DateTimeFormatter datetimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private EntityManager entityManager;

    ForvaltningUttrekkRestTjeneste() {
        // CDI
    }

    @Inject
    public ForvaltningUttrekkRestTjeneste(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM) // returnerer 'feil' type for å støtte lenke i swagger-ui
    @Operation(
        tags = "FORVALTNING-uttrekk",
        description = "Uttrekk til enhetene i csv-format",
        responses = {
            @ApiResponse(responseCode = "200", description = "Uttrekk"),
            @ApiResponse(responseCode = "500", description = "Feilet av ukjent årsak")
        })
    @BeskyttetRessurs(actionType = ActionType.READ, property = AbacProperty.DRIFT)
    public Response uttrekk() {
        if (ApplicationName.hvilkenTilbake() != Fagsystem.FPTILBAKE) {
            return Response.status(NOT_IMPLEMENTED).build();
        }
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                            SELECT
                                B.FAGSAK_ID              AS FAGSAK_ID,
                                F.SAKSNUMMER             AS SAKSNUMMER,
                                B.ID                     AS BEHANDLING_ID,
                                to_char(B.OPPRETTET_TID, 'YYYY-MM-DD HH24:MM') AS BEHANDLING_OPPRETTET,
                                B.BEHANDLING_TYPE        AS BEHANDLING_TYPE,
                                B.BEHANDLENDE_ENHET      AS BEHANDLENDE_ENHET,
                                B.BEHANDLING_STATUS      AS BEHANDLING_STATUS,
                                A.AKSJONSPUNKT_DEF       AS AUTOP_KODE,
                                A.VENT_AARSAK            AS VENT_AARSAK,
                                to_char(A.OPPRETTET_TID, 'YYYY-MM-DD HH24:MM') AS AUTOP_OPPRETTET,
                                to_char(A.FRIST_TID, 'YYYY-MM-DD HH24:MM') AS AUTOP_FRIST,
                                A.AKSJONSPUNKT_STATUS    AS AUTOP_STATUS,
                                COUNT(A_MAN.ID) AS ANTALL_AP,
                                null as FORSTE_UTTAKSDATO
                            FROM BEHANDLING B
                                     LEFT JOIN AKSJONSPUNKT A ON A.BEHANDLING_ID = B.ID AND A.AKSJONSPUNKT_STATUS NOT IN ('UTFO','AVBR') AND SUBSTR(A.AKSJONSPUNKT_DEF,1,1) NOT IN ('5','6')
                                     LEFT JOIN AKSJONSPUNKT A_MAN ON A_MAN.BEHANDLING_ID = B.ID AND A_MAN.AKSJONSPUNKT_STATUS NOT IN ('UTFO','AVBR') AND SUBSTR(A_MAN.AKSJONSPUNKT_DEF,1,1) NOT IN ('6','7')
                                     INNER JOIN FAGSAK F ON B.FAGSAK_ID = F.ID
                            WHERE B.BEHANDLING_STATUS != 'AVSLU'
                              AND YTELSE_TYPE = 'FP'
                            GROUP BY B.FAGSAK_ID,F.SAKSNUMMER, B.ID, B.OPPRETTET_TID, B.BEHANDLING_TYPE, B.BEHANDLENDE_ENHET,
                                     B.BEHANDLING_STATUS, A.OPPRETTET_TID, A.FRIST_TID, A.VENT_AARSAK, A.AKSJONSPUNKT_DEF,
                                     A.AKSJONSPUNKT_STATUS
                """)
            .setHint(AvailableHints.HINT_READ_ONLY, true)
            .getResultList();
        var csvBuilder = new StringBuilder();
        for (var row : rows) {
            var rowString = Arrays.stream(row)
                .map(o -> o != null ? o.toString() : "")
                .collect(Collectors.joining(","));
            csvBuilder.append(rowString);
            csvBuilder.append("\n");
        }
        LOG.info("FORVALTNING: uttrekk av {} rader til enheter", rows.size());
        return Response.ok(csvBuilder.toString())
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDispositionValue())
            .build();
    }

    private static String contentDispositionValue() {
        return "attachment; filename=" + datetimeFormatter.format(LocalDateTime.now()) + "_FPTILBAKE.csv";
    }

}
