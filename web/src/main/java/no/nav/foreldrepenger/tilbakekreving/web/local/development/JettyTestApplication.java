package no.nav.foreldrepenger.tilbakekreving.web.local.development;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/jetty")
public class JettyTestApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(JettyLoginResource.class);
        return classes;
    }
}
