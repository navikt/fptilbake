package no.nav.foreldrepenger.tilbakekreving.web.app.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.spi.ApplicationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Level;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.log.util.MemoryAppender;

public class GeneralRestExceptionMapperTest {
    private static final MemoryAppender logSniffer = MemoryAppender.sniff(GeneralRestExceptionMapper.class);

    private final GeneralRestExceptionMapper generalRestExceptionMapper = new GeneralRestExceptionMapper();

    @AfterAll
    static void afterAll() {
        logSniffer.reset();
    }

    @Test
    public void skalMappeValideringsfeil() {
        FeltFeilDto feltFeilDto = new FeltFeilDto("Et feltnavn", "En feilmelding");
        Valideringsfeil valideringsfeil = new Valideringsfeil(Collections.singleton(feltFeilDto));

        Response response = generalRestExceptionMapper.toResponse(new ApplicationException(valideringsfeil));

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
        FeilDto feilDto = (FeilDto) response.getEntity();

        assertThat(feilDto.getFeilmelding()).contains("Det oppstod en valideringsfeil på felt [Et feltnavn]. Vennligst kontroller at alle feltverdier er korrekte.");
        assertThat(feilDto.getFeltFeil()).hasSize(1);
        assertThat(feilDto.getFeltFeil().iterator().next()).isEqualTo(feltFeilDto);
    }

    @Test
    public void skalMappeManglerTilgangFeil() {
        var manglerTilgangFeil = TestFeil.manglerTilgangFeil();

        Response response = generalRestExceptionMapper.toResponse(new ApplicationException(manglerTilgangFeil));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
        FeilDto feilDto = (FeilDto) response.getEntity();

        assertThat(feilDto.getType()).isEqualTo(FeilType.MANGLER_TILGANG_FEIL);
        assertThat(feilDto.getFeilmelding()).contains("ManglerTilgangFeilmeldingKode");
        assertThat(logSniffer.search("ManglerTilgangFeilmeldingKode")).hasSize(1);
    }

    @Test
    public void skalMappeFunksjonellFeil() {
        var funksjonellFeil = TestFeil.funksjonellFeil();

        Response response = generalRestExceptionMapper.toResponse(new ApplicationException(funksjonellFeil));

        assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
        FeilDto feilDto = (FeilDto) response.getEntity();

        assertThat(feilDto.getFeilmelding()).contains("FUNK_FEIL");
        assertThat(feilDto.getFeilmelding()).contains("en funksjonell feilmelding");
        assertThat(feilDto.getFeilmelding()).contains("et løsningsforslag");
        assertThat(logSniffer.search("en funksjonell feilmelding")).hasSize(1);
    }

    @Test
    public void skalMappeVLException() {
        VLException vlException = TestFeil.tekniskFeil();

        Response response = generalRestExceptionMapper.toResponse(new ApplicationException(vlException));

        assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
        FeilDto feilDto = (FeilDto) response.getEntity();

        assertThat(feilDto.getFeilmelding()).contains("TEK_FEIL");
        assertThat(feilDto.getFeilmelding()).contains("en teknisk feilmelding");
        assertThat(logSniffer.search("en teknisk feilmelding")).hasSize(1);
    }

    @Test
    public void skalMappeGenerellFeil() {
        String feilmelding = "en helt generell feil";
        RuntimeException generellFeil = new RuntimeException(feilmelding);

        Response response = generalRestExceptionMapper.toResponse(new ApplicationException(generellFeil));

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
        FeilDto feilDto = (FeilDto) response.getEntity();

        assertThat(feilDto.getFeilmelding()).contains(feilmelding);
        logSniffer.contains("en helt generell feil", Level.ERROR);
    }

    private static class TestFeil {

        static FunksjonellException funksjonellFeil() {
            return new FunksjonellException("FUNK_FEIL", "en funksjonell feilmelding", "et løsningsforslag");
        }

        static TekniskException tekniskFeil() {
            return new TekniskException("TEK_FEIL", "en teknisk feilmelding");
        }

        static ManglerTilgangException manglerTilgangFeil() {
            return new ManglerTilgangException("MANGLER_TILGANG_FEIL", "ManglerTilgangFeilmeldingKode");
        }
    }
}
