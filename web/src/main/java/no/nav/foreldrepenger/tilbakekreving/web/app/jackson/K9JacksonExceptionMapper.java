package no.nav.foreldrepenger.tilbakekreving.web.app.jackson;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import com.fasterxml.jackson.core.JacksonException;

import no.nav.vedtak.feil.Feilkode;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.server.rest.RestServerFeilUtils;

public class K9JacksonExceptionMapper implements ExceptionMapper<JacksonException> {

    @Override
    public Response toResponse(JacksonException exception) {
        RestServerFeilUtils.ensureCallId();
        var feil = "FP-252294 JSON-feil: " + LoggerUtils.removeLineBreaks(exception.getMessage());
        RestServerFeilUtils.loggWarning(feil);
        return RestServerFeilUtils.responseFra(Response.Status.BAD_REQUEST, Feilkode.GENERELL, feil);
    }
}
