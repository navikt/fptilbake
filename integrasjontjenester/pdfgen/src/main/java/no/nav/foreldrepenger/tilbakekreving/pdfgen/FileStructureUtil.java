package no.nav.foreldrepenger.tilbakekreving.pdfgen;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileStructureUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileStructureUtil.class);

    public static File getFont(String fontName) {
        String location = "fonts/" + fontName;
        ClassLoader cl = FileStructureUtil.class.getClassLoader();
        URL resource = cl.getResource(location);
        String filnavn = resource.getFile();
        File fil = new File(filnavn);
        logger.info("Fil for font {} på path {} finnes? {}, størrelse {}", fontName, filnavn, fil.exists(), fil.length());
        return fil;
    }

    public static byte[] getColorProfile() {
        String location = "colorprofile/sRGB2014.icc";
        return readResource(location);
    }

    public static byte[] readResource(String location) {
        InputStream is = FileStructureUtil.class.getClassLoader().getResourceAsStream(location);
        Objects.requireNonNull(is, "Fant ikke resource " + location);
        try {
            byte[] bytes = is.readAllBytes();
            logger.info("Leste {} bytes fra {}", bytes.length, location);
            return bytes;
        } catch (IOException e) {
            throw new IllegalArgumentException("Klarte ikke å lese resource " + location);
        }
    }

    public static String readResourceAsString(String location) {
        return new String(readResource(location), UTF_8);
    }

}
