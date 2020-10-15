package no.nav.foreldrepenger.tilbakekreving.avstemming.sftp;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

import no.nav.vedtak.util.Objects;

public class SftpKonfig {

    public static final String JSCH_CHANNEL_TYPE_SFTP = "sftp";
    public static final int JSCH_DEFAULT_PORT = 22;
    
    private String username;
    private String host;
    private int port = SftpKonfig.JSCH_DEFAULT_PORT;

    private String password;

    private boolean hasKeyFile = false;
    private byte[] keyFilePassphrase;

    private String keyFileUrl;

    private boolean isKeyFileByParams = false;
    private String privateKey;
    private String publicKey;

    private String directory;

    private boolean strictHostKeyChecking = false;

    private SftpKonfig() {

    }

    public String getUsername() {
        return username;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean hasKeyFile() {
        return hasKeyFile;
    }

    public void medIdentiy(JSch jSch) throws JSchException {
        if (keyFileUrl != null) {
            if (keyFilePassphrase != null) {
                jSch.addIdentity(keyFileUrl, keyFilePassphrase);
            }  else {
                jSch.addIdentity(keyFileUrl);
            }
        } else if (isKeyFileByParams) {
            jSch.addIdentity(username, privateKey.getBytes(), publicKey.getBytes(), keyFilePassphrase);
        } else {
            throw new RuntimeException("Mangler identity konfigurering");
        }
    }

    public String hentFulltFilnavn(String filnavn) {
        if (directory != null) {
            return directory + (!directory.endsWith("/") ? "/" : "") + filnavn;
        }
        return filnavn;
    }

    public boolean hasPassword() {
        return password != null && password.length() > 0;
    }

    public String getPassword() {
        return password;
    }

    public boolean isNotStrictHostKeyChecking() {
        return !strictHostKeyChecking;
    }

    public static Builder builder(String username, String host) {
        return new Builder(username, host);
    }

    public static class Builder {
        private SftpKonfig kladd = new SftpKonfig();

        private Builder(String username, String host) {
            kladd.username = username;
            kladd.host = host;
        }

        public Builder medPort(int port) {
            kladd.port = port;
            return this;
        }

        public Builder medPassword(String password) {
            kladd.password = password;
            return this;
        }

        public Builder medKeyFile(String keyFileUrl, String passphrase) {
            kladd.hasKeyFile = true;
            kladd.keyFilePassphrase = getKeyFilePassphrase(passphrase);
            kladd.keyFileUrl = keyFileUrl;
            return this;
        }


        public Builder medKeyAsParams(String privateKey, String publicKey, String passphrase) {
            kladd.hasKeyFile = true;
            kladd.isKeyFileByParams = true;
            kladd.privateKey = privateKey;
            kladd.publicKey = publicKey;
            kladd.keyFilePassphrase = getKeyFilePassphrase(passphrase);
            return this;
        }

        public Builder medDirectory(String directory) {
            kladd.directory = directory;
            return this;
        }

        public Builder medStrictHostKeyChecking() {
            kladd.strictHostKeyChecking = true;
            return this;
        }

        public SftpKonfig build() {
            Objects.check(kladd.username != null, "må ha username for sftp");
            Objects.check(kladd.host != null, "må ha host for sftp");
            if (kladd.hasKeyFile) {
                if (kladd.isKeyFileByParams) {
                    Objects.check(kladd.privateKey != null, "må ha privateKey for sftp med nøkkel");
                    Objects.check(kladd.publicKey != null, "må ha publicKey for sftp med nøkkel");
                } else {
                    Objects.check(kladd.keyFileUrl != null, "må ha keyFile for sftp med fil");
                }
            } else {
                Objects.check(kladd.password != null, "må ha password for sftp");
            }
            return kladd;
        }

        private byte[] getKeyFilePassphrase(String passphrase) {
            return passphrase != null && passphrase.length() > 0 ? passphrase.getBytes() : null;
        }
    }
}
