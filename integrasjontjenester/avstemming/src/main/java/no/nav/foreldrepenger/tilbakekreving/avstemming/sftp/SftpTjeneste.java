package no.nav.foreldrepenger.tilbakekreving.avstemming.sftp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public abstract class SftpTjeneste {

    protected void put(String filInnhold, String filNavn, SftpKonfig konfig) throws JSchException, SftpException {
        put(new ByteArrayInputStream(filInnhold.getBytes()), filNavn, konfig);
    }

    private void put(InputStream fileContent, String fileName, SftpKonfig konfig) throws JSchException, SftpException {
        JSch jSch = new JSch();

        Session session = null;
        ChannelSftp channel = null;
        try {
            if (konfig.hasKeyFile()) {
                konfig.medIdentiy(jSch);
            }

            session = jSch.getSession(konfig.getUsername(), konfig.getHost(), konfig.getPort());

            if (konfig.hasPassword()) {
                session.setPassword(konfig.getPassword());
            }

            if (konfig.isNotStrictHostKeyChecking()) {
                session.setConfig("StrictHostKeyChecking", "no");
            }

            session.connect();

            channel = (ChannelSftp) session.openChannel(SftpKonfig.JSCH_CHANNEL_TYPE_SFTP);
            channel.connect();

            channel.put(fileContent, konfig.hentFulltFilnavn(fileName));
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }
}
