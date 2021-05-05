package no.nav.foreldrepenger.tilbakekreving.web.app.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;

import no.nav.vedtak.exception.TekniskException;

public class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {

    private static final Logger log = LoggerFactory.getLogger(JsonParseExceptionMapper.class);

    @Override
    public Response toResponse(JsonParseException exception) {
        var feil = JsonMappingFeil.jsonParseFeil(exception.getMessage(), exception);
        log.warn(feil.getMessage(), feil);

        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(new FeilDto(feil.getMessage()))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    private static class JsonMappingFeil {
        static TekniskException jsonParseFeil(String feilmelding, JsonParseException e) {
            return new TekniskException("FPT-299955", String.format("JSON-parsing feil: %s", feilmelding), e);
        }
    }

}
