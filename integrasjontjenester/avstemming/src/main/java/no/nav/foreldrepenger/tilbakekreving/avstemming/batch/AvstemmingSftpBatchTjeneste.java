package no.nav.foreldrepenger.tilbakekreving.avstemming.batch;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.resteasy.util.Base64;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import no.nav.foreldrepenger.tilbakekreving.avstemming.sftp.SftpKonfig;
import no.nav.foreldrepenger.tilbakekreving.avstemming.sftp.SftpKonfigImpl;
import no.nav.foreldrepenger.tilbakekreving.avstemming.sftp.SftpTjeneste;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
class AvstemmingSftpBatchTjeneste extends SftpTjeneste {

    private SftpKonfig sftpKonfig;

    public AvstemmingSftpBatchTjeneste() {
        //for CDI proxy
    }

    public AvstemmingSftpBatchTjeneste(@KonfigVerdi(value = "avstemming.sftp.username") String username,
                                       @KonfigVerdi(value = "avstemming.sftp.host") String host,
                                       @KonfigVerdi(value = "avstemming.sftp.port", defaultVerdi = "22") int port,
                                       @KonfigVerdi(value = "avstemming.sftp.key.private") String privateKey,
                                       @KonfigVerdi(value = "avstemming.sftp.key.public") String publicKey,
                                       @KonfigVerdi(value = "avstemming.sftp.key.passphrase", required = false) String passphrase) throws Exception {
        SftpKonfigImpl.Builder builder = SftpKonfigImpl.builder(username, host)
            .medPort(port)
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
