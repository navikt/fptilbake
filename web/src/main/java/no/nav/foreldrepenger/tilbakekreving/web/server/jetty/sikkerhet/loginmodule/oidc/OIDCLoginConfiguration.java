package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.sikkerhet.loginmodule.oidc;

import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;

import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.sikkerhet.loginmodule.LoginConfiguration;

public class OIDCLoginConfiguration implements LoginConfiguration {

    private static final AppConfigurationEntry[] OIDC_CONFIGURATION = new AppConfigurationEntry[]{new AppConfigurationEntry(
        "no.nav.foreldrepenger.tilbakekreving.web.server.jetty.sikkerhet.loginmodule.DummyLoginModule", AppConfigurationEntry.LoginModuleControlFlag.REQUISITE, Map.of())};

    @Override
    public String getName() {
        return "OIDC";
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry() {
        return OIDC_CONFIGURATION;
    }

}
