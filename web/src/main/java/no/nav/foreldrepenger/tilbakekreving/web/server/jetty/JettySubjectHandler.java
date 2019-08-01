package no.nav.foreldrepenger.tilbakekreving.web.server.jetty;

import javax.security.auth.Subject;

import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.Request;

import no.nav.vedtak.sikkerhet.context.SubjectHandler;

/**
 * SubjectHandler som kan benyttes til testing lokalt på Jetty.
 * <p/>
 * For å benytte denne subjecthandleren må følgende konfigureres i oppstart av Jetty:
 *
 * @see SubjectHandler
 */
public class JettySubjectHandler extends SubjectHandler {

    private static ThreadLocal<Subject> subjectHolder = new ThreadLocal<>();

    @SuppressWarnings("resource")
    @Override
    public Subject getSubject() {
        HttpConnection httpConnection = HttpConnection.getCurrentConnection(); // NOSONAR kan ikke closes i denne metoden
        if (httpConnection != null) {
            hentSubjectFraRequest(httpConnection);
        }
        return subjectHolder.get();
    }

    private void hentSubjectFraRequest(HttpConnection httpConnection) {
        Request request = httpConnection.getHttpChannel().getRequest();
        Authentication authentication = request.getAuthentication();

        if (authentication instanceof Authentication.User) {
            subjectHolder.set(((Authentication.User) authentication).getUserIdentity().getSubject());
        }
    }

    public void setSubject(Subject subject) {
        subjectHolder.set(subject);
    }

    public void removeSubject(){
        subjectHolder.remove();
    }
}
