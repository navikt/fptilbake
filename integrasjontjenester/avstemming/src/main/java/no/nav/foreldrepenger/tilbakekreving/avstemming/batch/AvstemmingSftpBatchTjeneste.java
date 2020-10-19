package no.nav.foreldrepenger.tilbakekreving.avstemming.batch;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.resteasy.util.Base64;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import no.nav.foreldrepenger.tilbakekreving.avstemming.sftp.SftpKonfig;
import no.nav.foreldrepenger.tilbakekreving.avstemming.sftp.SftpTjeneste;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
class AvstemmingSftpBatchTjeneste extends SftpTjeneste {

    private SftpKonfig sftpKonfig;

    public AvstemmingSftpBatchTjeneste() {
        //for CDI proxy
    }

    @Inject
    public AvstemmingSftpBatchTjeneste(@KonfigVerdi(value = "AVSTEMMING_SFTP_USERNAME") String username,
                                       @KonfigVerdi(value = "AVSTEMMING_SFTP_HOST") String host,
                                       @KonfigVerdi(value = "AVSTEMMING_SFTP_PORT", defaultVerdi = "22") int port,
                                       @KonfigVerdi(value = "AVSTEMMING_SFTP_KEY_PRIVATE") String privateKey,
                                       @KonfigVerdi(value = "AVSTEMMING_SFTP_KEY_PUBLIC") String publicKey,
                                       @KonfigVerdi(value = "AVSTEMMING_SFTP_KEY_PASSPHRASE", required = false) String passphrase,
                                       @KonfigVerdi(value = "AVSTEMMING_SFTP_DIRECTORY", defaultVerdi = "inbound") String directory) throws Exception {
        SftpKonfig.Builder builder = SftpKonfig.builder(username, host)
            .medPort(port)
            .medDirectory(directory)
            .medKeyAsParams(base64Decode(privateKey), base64Decode(publicKey), passphrase);
        sftpKonfig = builder.build();
    }

    public void put(String filinnhold, String filnavn) throws SftpException, JSchException {
        put(filinnhold, filnavn, this.sftpKonfig);
    }

    private String base64Decode(String encoded) throws IOException {
        byte[] decoded = Base64.decode(encoded);
        return new String(decoded);
    }
}
