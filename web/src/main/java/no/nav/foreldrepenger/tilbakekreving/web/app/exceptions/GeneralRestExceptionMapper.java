package no.nav.foreldrepenger.tilbakekreving.web.app.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.jpa.TomtResultatException;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.sikkerhet.abac.PepNektetTilgangException;

@Provider
public class GeneralRestExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private static final Logger LOG = LoggerFactory.getLogger(GeneralRestExceptionMapper.class);

    @Override
    public Response toResponse(WebApplicationException exception) {
        var cause = exception.getCause();
        if (cause instanceof TomtResultatException) {
            return handleTomtResultatFeil((TomtResultatException) cause);
        }

        loggTilApplikasjonslogg(cause);
        var callId = MDCOperations.getCallId();

        if (cause instanceof VLException) {
            return handleVLException((VLException) cause, callId);
        }

        return handleGenerellFeil(cause, callId);
    }

    private Response handleTomtResultatFeil(TomtResultatException tomtResultatException) {
        return Response.status(Response.Status.NOT_FOUND)
            .entity(new FeilDto(FeilType.TOMT_RESULTAT_FEIL, tomtResultatException.getMessage()))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    private Response handleVLException(VLException vlException, String callId) {
        if (vlException instanceof ManglerTilgangException) {
            return ikkeTilgang(vlException);
        }
        return serverError(callId, vlException);
    }

    private Response serverError(String callId, VLException feil) {
        var feilmelding = getVLExceptionFeilmelding(callId, feil);
        var feilType = FeilType.GENERELL_FEIL;
        return Response.serverError()
            .entity(new FeilDto(feilType, feilmelding))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    private Response ikkeTilgang(VLException feil) {
        var feilmelding = feil.getMessage();
        var feilType = FeilType.MANGLER_TILGANG_FEIL;
        return Response.status(Response.Status.FORBIDDEN)
            .entity(new FeilDto(feilType, feilmelding))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    private String getVLExceptionFeilmelding(String callId, VLException feil) {
        var feilbeskrivelse = feil.getMessage();
        if (feil instanceof FunksjonellException) {
            var løsningsforslag = ((FunksjonellException) feil).getLøsningsforslag();
            return "Det oppstod en feil: " + avsluttMedPunktum(feilbeskrivelse) + avsluttMedPunktum(løsningsforslag)
                + ". Referanse-id: " + callId;
        }
        return "Det oppstod en serverfeil: " + avsluttMedPunktum(feilbeskrivelse)
            + ". Meld til support med referanse-id: " + callId;
    }

    private Response handleGenerellFeil(Throwable cause, String callId) {
        var generellFeilmelding =
            "Det oppstod en serverfeil: " + cause.getMessage() + ". Meld til support med referanse-id: " + callId;
        return Response.serverError()
            .entity(new FeilDto(FeilType.GENERELL_FEIL, generellFeilmelding))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    private String avsluttMedPunktum(String tekst) {
        return tekst + (tekst.endsWith(".") ? " " : ". ");
    }

    private void loggTilApplikasjonslogg(Throwable cause) {
        if (cause instanceof PepNektetTilgangException) {
            LOG.info(cause.getMessage(), cause);
        } else if (cause instanceof VLException) {
            LOG.warn(cause.getMessage(), cause);
        } else {
            var message = cause.getMessage() != null ? LoggerUtils.removeLineBreaks(cause.getMessage()) : "";
            LOG.error("Fikk uventet feil:" + message, cause);
        }

        // key for å tracke prosess -- nullstill denne
        MDC.remove("prosess");
    }

}
