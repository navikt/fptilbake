package no.nav.foreldrepenger.tilbakekreving.pdfgen.validering;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.verapdf.core.EncryptedPdfException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.core.ValidationException;
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.ValidationResult;

/**
 * bruker VeraPDF for å validere at produsert pdf er gyldig PDFA og dermed egnet for arkivering
 * <p>
 * For dokumentasjon, se https://docs.verapdf.org/develop/
 */
public class PdfaValidator {

    static {
        VeraGreenfieldFoundryProvider.initialise();
    }

    public static void validatePdf(byte[] pdf) {

        try {
            validatePdf(new ByteArrayInputStream(pdf));
        } catch (ModelParsingException e) {
            throw new PdfaValideringException("Feil ved parsing av pdf modell", e);
        } catch (EncryptedPdfException e) {
            throw new PdfaValideringException("Klarer ikke å håndtere kryptert pdf", e);
        } catch (IOException e) {
            throw new PdfaValideringException("IO exception ved validering av pdf", e);
        } catch (ValidationException e) {
            throw new PdfaValideringException("Validering av pdf feilet", e);
        }
    }

    private static void validatePdf(InputStream inputStream) throws ModelParsingException, EncryptedPdfException, IOException, ValidationException {
        PDFAFlavour flavour = PDFAFlavour.fromString("2b");
        try (PDFAValidator validator = Foundries.defaultInstance().createValidator(flavour, false)) {
            try (PDFAParser parser = Foundries.defaultInstance().createParser(inputStream, flavour)) {
                ValidationResult result = validator.validate(parser);
                if (!result.isCompliant()) {
                    throw new PdfaValideringException(result);
                }
            }
        }
    }
}
