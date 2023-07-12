package no.nav.foreldrepenger.tilbakekreving.pdfgen.validering;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PdfaValidatorTest {

    @Test
    @DisplayName("Tenkt bare for å sjekke om gitt Vera versjon er kompatibel med java versjon. Kaster NoClassDefFoundError om noe mangler.")
    void test_pdf_invalid() {
        var pdf = "some_invalid_pdf".getBytes(StandardCharsets.UTF_8);

        var ex = assertThrows(PdfaValideringException.class, () -> PdfaValidator.validatePdf(pdf));
        assertThat(ex.getMessage()).contains("Feil ved parsing av pdf modell");
    }
}
