package no.nav.foreldrepenger.tilbakekreving.avstemming.sftp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

/**
 * POC-testing av SFTP-funksjonalitet. Tester 4 scenarioer:
 * - Scenario med brukernavn og passord
 * - Scenario med nøkkelfil og uten passphrase på nøkkelfilen
 * - Scenario med nøkkelfil og med passphrase på nøkkelfilen
 * - Scenario med private og public nøkkel i minnet og passphrase på nøkkelen. Aktuelt når private og public nøkkel ligger i VAULT
 */
@Ignore("Må starte docker-kontaineren sftp-test manuelt. Testen brukt under utvikling og til eksempel ved seinere implementering av sftp-tjenester")
public class SftpTjenesteTest {

    public static final String HOST = "localhost";
    public static final int TEST_PORT = 50522;

    private String keyDir = "../../test-ressurser/sftp-test/keys/";

    @Test
    public void testSftpMedPassord() throws Exception {
        String username = "foo";
        String bar = "bar";

        SftpKonfig konfig = SftpKonfigImpl.builder(username, HOST)
            .medPort(TEST_PORT)
            .medPassword(bar)
            .medDirectory("share")
            .build();

        long timeMillis = System.currentTimeMillis();
        TestSftpTjeneste sftpTjeneste = new TestSftpTjeneste(konfig);
        sftpTjeneste.put("Dette er innholdet", "testfil_test1_" + timeMillis + ".txt");
    }

    @Test
    public void testSftpMedKeyfile() throws Exception {
        String username = "twin";

        String keyFileUrl = keyDir + "twin_key";

        SftpKonfig konfig = SftpKonfigImpl.builder(username, HOST)
            .medPort(TEST_PORT)
            .medDirectory("another")
            .medKeyFile(keyFileUrl, null)
            .build();

        long timeMillis = System.currentTimeMillis();
        TestSftpTjeneste sftpTjeneste = new TestSftpTjeneste(konfig);
        sftpTjeneste.put("Dette er noe annet innhold", "testfil_test2_" + timeMillis + ".txt");
    }

    @Test
    public void testSftpMedKeyfileAndPassphrase() throws Exception {
        String username = "third";

        String keyFileUrl = keyDir + "third_key";
        String passphrase = "test123";

        SftpKonfig konfig = SftpKonfigImpl.builder(username, HOST)
            .medPort(TEST_PORT)
            .medDirectory("share")
            .medKeyFile(keyFileUrl, passphrase)
            .build();

        long timeMillis = System.currentTimeMillis();
        TestSftpTjeneste sftpTjeneste = new TestSftpTjeneste(konfig);
        sftpTjeneste.put("Dette er igjen noe annet innhold", "testfil_test3_" + timeMillis + ".txt");
    }

    @Test
    public void testSftpMedKeyAsParamAndPassphrase() throws Exception {
        String username = "fourth";

        String privkeyFileUrl = keyDir + "fourth_key";
        String passphrase = "test123";

        Path privKeyPath = Paths.get(privkeyFileUrl);
        Path pubKeyPath = Paths.get(privkeyFileUrl + ".pub");
        String privKey = new String(Files.readAllBytes(privKeyPath));
        String pubKey = new String(Files.readAllBytes(pubKeyPath));

        SftpKonfig konfig = SftpKonfigImpl.builder(username, HOST)
            .medPort(TEST_PORT)
            .medDirectory("share")
            .medKeyAsParams(privKey, pubKey, passphrase)
            .build();

        long timeMillis = System.currentTimeMillis();
        TestSftpTjeneste sftpTjeneste = new TestSftpTjeneste(konfig);
        sftpTjeneste.put("Dette er igjen noe annet innhold", "testfil_test4_" + timeMillis + ".txt");
    }

    static class TestSftpTjeneste extends SftpTjeneste {

        private SftpKonfig sftpKonfig;

        TestSftpTjeneste(SftpKonfig sftpKonfig) {
            this.sftpKonfig = sftpKonfig;
        }

        void put(String filInnhold, String filNavn) throws SftpException, JSchException {
            put(filInnhold, filNavn, this.sftpKonfig);
        }
    }
}
