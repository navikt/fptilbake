package no.nav.foreldrepenger.tilbakekreving.avstemming.batch;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jose4j.base64url.Base64;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.jcraft.jsch.JSchException;

@Disabled("Må starte docker-kontaineren sftp-test manuelt. Testen brukt under utvikling og til eksempel ved seinere implementering av sftp-tjenester")
public class AvstemmingSftpBatchTjenesteTest {

    private static final String HOST = "localhost";
    private static final int TEST_PORT = 50522;
    private static final String DIRECTORY = "inbound";

    private final String testDir = "../../test-ressurser/sftp-test/";
    private String keyDir = testDir + "keys/";

    @Test
    public void lagreAvstemmingsfil() throws Exception {
        String username = "fptilbake";
        String tmpDir = testDir + "tmp/" + username + "/";
        String privkeyFileUrl = keyDir + "fptilbake_key";

        Path privKeyPath = Paths.get(privkeyFileUrl);
        Path pubKeyPath = Paths.get(privkeyFileUrl + ".pub");
        String privKey = new String(Files.readAllBytes(privKeyPath));
        String pubKey = new String(Files.readAllBytes(pubKeyPath));

        long timeMillis = System.currentTimeMillis();
        String filnavn = "avstemming_" + timeMillis + ".txt";

        AvstemmingSftpBatchTjeneste sftpTjeneste = new AvstemmingSftpBatchTjeneste(username, HOST, TEST_PORT, encodeString(privKey), encodeString(pubKey), null, DIRECTORY);
        sftpTjeneste.put("Dette skulle vært avstemmingsinformasjon, men er bare en tekst", filnavn);

        Path opprettetFil = Paths.get(tmpDir + filnavn);
        assertTrue(Files.exists(opprettetFil), "Filen skulle nå ha blitt opprettet");
    }

    @Test
    public void lagreAvstemmingsfil_base64_encoded_cmd() throws Exception {
        String username = "fptilbake2";
        String passphrase = "test123";

        String tmpDir = testDir + "tmp/" + username + "/";
        String privkeyFileUrl = keyDir + "fptilbake2_encoded";

        Path privKeyPath = Paths.get(privkeyFileUrl);
        Path pubKeyPath = Paths.get(privkeyFileUrl + ".pub");
        String privKey = new String(Files.readAllBytes(privKeyPath));
        String pubKey = new String(Files.readAllBytes(pubKeyPath));

        long timeMillis = System.currentTimeMillis();
        String filnavn = "avstemming_" + timeMillis + ".txt";

        AvstemmingSftpBatchTjeneste sftpTjeneste = new AvstemmingSftpBatchTjeneste(username, HOST, TEST_PORT, privKey, pubKey, passphrase, DIRECTORY);
        sftpTjeneste.put("Dette skulle vært avstemmingsinformasjon, men er bare en tekst", filnavn);

        Path opprettetFil = Paths.get(tmpDir + filnavn);
        assertTrue(Files.exists(opprettetFil), "Filen skulle nå ha blitt opprettet");
    }

    @Test
    public void lagreAvstemmingsfil_feiler_med_feil_port() throws Exception {
        String privkeyFileUrl = keyDir + "fptilbake_key";

        Path privKeyPath = Paths.get(privkeyFileUrl);
        Path pubKeyPath = Paths.get(privkeyFileUrl + ".pub");
        String privKey = new String(Files.readAllBytes(privKeyPath));
        String pubKey = new String(Files.readAllBytes(pubKeyPath));

        long timeMillis = System.currentTimeMillis();
        String filnavn = "avstemming_" + timeMillis + ".txt";

        AvstemmingSftpBatchTjeneste sftpTjeneste = new AvstemmingSftpBatchTjeneste("fptilbake", HOST, 22, encodeString(privKey), encodeString(pubKey), null, DIRECTORY);
        assertThrows(JSchException.class, () -> sftpTjeneste.put("Dette skulle vært avstemmingsinformasjon, men er bare en tekst", filnavn));
    }

    private String encodeString(String decoded) {
        return Base64.encode(decoded.getBytes());
    }
}
