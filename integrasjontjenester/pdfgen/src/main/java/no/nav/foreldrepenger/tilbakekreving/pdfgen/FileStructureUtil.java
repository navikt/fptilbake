package no.nav.foreldrepenger.tilbakekreving.pdfgen;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class FileStructureUtil {

    public static byte[] getColorProfile() {
        //colorprofile fra https://pippin.gimp.org/sRGBz/
        return readResource("colorprofile/sRGBz.icc");
    }

    public static byte[] readResource(String location) {
        InputStream is = FileStructureUtil.class.getClassLoader().getResourceAsStream(location);
        Objects.requireNonNull(is, "Fant ikke resource " + location);
        try {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Klarte ikke å lese resource " + location);
        }
    }

    public static String readResourceAsString(String location) {
        return new String(readResource(location), UTF_8);
    }

}
