package no.nav.foreldrepenger.tilbakekreving.pdfgen.validering;

import java.util.List;
import java.util.stream.Collectors;

import org.verapdf.pdfa.results.TestAssertion;
import org.verapdf.pdfa.results.ValidationResult;

public class PdfaValideringException extends RuntimeException {

    public PdfaValideringException(String message) {
        super(message);
    }

    public PdfaValideringException(String message, Throwable cause) {
        super(message, cause);
    }

    public PdfaValideringException(ValidationResult result) {
        this(formater(result));
    }

    private static String formater(ValidationResult result) {
        List<String> feilmeldinger = result.getTestAssertions().stream()
            .filter(ta -> ta.getStatus() != TestAssertion.Status.PASSED)
            .map(ta -> ta.getStatus() + ":" + ta.getMessage())
            .collect(Collectors.toList());

        return "Validering av pdf feilet. Validerer versjon " + result.getPDFAFlavour()  + " feil er: " + String.join(", ", feilmeldinger);
    }
}
