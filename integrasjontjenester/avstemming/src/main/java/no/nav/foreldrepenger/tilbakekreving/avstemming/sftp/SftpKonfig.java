package no.nav.foreldrepenger.tilbakekreving.avstemming.sftp;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

public interface SftpKonfig {

    String JSCH_CHANNEL_TYPE_SFTP = "sftp";
    int JSCH_DEFAULT_PORT = 22;

    String getUsername();
    String getHost();
    int getPort();

    boolean hasKeyFile();

    void medIdentiy(JSch jSch) throws JSchException;

    String hentFulltFilnavn(String filnavn);

    boolean hasPassword();

    String getPassword();

    boolean isNotStrictHostKeyChecking();
}
