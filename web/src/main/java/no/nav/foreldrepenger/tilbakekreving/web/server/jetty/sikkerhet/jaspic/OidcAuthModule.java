package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.sikkerhet.jaspic;

import static jakarta.security.auth.message.AuthStatus.FAILURE;
import static jakarta.security.auth.message.AuthStatus.SEND_CONTINUE;
import static jakarta.security.auth.message.AuthStatus.SEND_SUCCESS;
import static jakarta.security.auth.message.AuthStatus.SUCCESS;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import jakarta.security.auth.message.AuthStatus;
import jakarta.security.auth.message.MessageInfo;
import jakarta.security.auth.message.MessagePolicy;
import jakarta.security.auth.message.callback.CallerPrincipalCallback;
import jakarta.security.auth.message.config.ServerAuthContext;
import jakarta.security.auth.message.module.ServerAuthModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.jetty.ee10.security.jaspi.JaspiMessageInfo;
import org.eclipse.jetty.server.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.sikkerhet.context.containers.BrukerNavnType;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.sikkerhet.loginmodule.LoginContextConfiguration;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.klient.http.CommonHttpHeaders;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;
import no.nav.vedtak.sikkerhet.kontekst.RequestKontekst;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;
import no.nav.vedtak.sikkerhet.oidc.validator.JwtUtil;

/**
 * Stjålet mye fra https://github.com/omnifaces/omnisecurity
 * <p>
 * Klassen er og må være thread-safe da den vil brukes til å kalle på subjects
 * samtidig. Se {@link ServerAuthModule}. Skal derfor ikke inneholde felter som
 * holder Subject, token eller andre parametere som kommer med en request eller
 * session.
 */
public class OidcAuthModule implements ServerAuthModule {

    private static final String OIDC_LOGIN_CONFIG = "OIDC";
    private static final Logger LOG = LoggerFactory.getLogger(OidcAuthModule.class);

    private static final Class<?>[] SUPPORTED_MESSAGE_TYPES = new Class[]{HttpServletRequest.class, HttpServletResponse.class};

    private final TokenLocator tokenLocator;
    private final Configuration loginConfiguration;

    private CallbackHandler containerCallbackHandler;

    public OidcAuthModule() {
        this.tokenLocator = new TokenLocator();
        this.loginConfiguration = new LoginContextConfiguration();
    }

    /**
     * used for unit-testing
     */
    OidcAuthModule(TokenLocator tokenLocator, Configuration loginConfiguration) {
        this.tokenLocator = tokenLocator;
        this.loginConfiguration = loginConfiguration;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy, CallbackHandler handler, Map options) {
        this.containerCallbackHandler = handler;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class[] getSupportedMessageTypes() {
        return SUPPORTED_MESSAGE_TYPES;
    }

    @Override
    public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject) {
        HttpServletRequest originalRequest = (HttpServletRequest) messageInfo.getRequestMessage();
        setCallAndConsumerId(originalRequest);
        validateCleanSubjectAndKontekst(clientSubject);
        // Skipp sjekk på unprotected siden NPE i JaspiMessageInfo/getMap. Legg unprotected i web.xml
        AuthStatus authStatus = handleProtectedResource(clientSubject, originalRequest);

        if (FAILURE.equals(authStatus)) {
            // Vurder om trengs whitelisting av oppslag mot isAlive/isReady/metrics gitt oppførsel i prod - app må registrere unprotected
            // Sjekk av whitelist/unprotected mot HttpServletRequest / getPathInfo må foregå før valg av protected/unprotected
            try {
                if (messageInfo instanceof JaspiMessageInfo mi) {
                    Response.writeError(mi.getBaseRequest(), mi.getBaseResponse(), mi.getCallback(), 401);
                } else {
                    return FAILURE; // Vil gi 403
                }
            } catch (Exception e) {
                throw new TekniskException("F-396795", "Klarte ikke å sende respons", e); // Gir 500
            }
            return SEND_CONTINUE; // SEND_CONTINUE sørger for svar med 401. (SEND_)FAILURE gir 403
        }

        if (SUCCESS.equals(authStatus)) {
            messageInfo.setRequestMessage(new StatelessHttpServletRequest((HttpServletRequest) messageInfo.getRequestMessage()));
        }
        return authStatus;
    }

    protected void validateCleanSubjectAndKontekst(Subject subject) {
        // Skal vel egentlig ikke skje men logg for ordens skyld
        var sluttbrukere = Optional.ofNullable(subject).map(Subject::getPrincipals).orElse(Set.of()).stream()
            .filter(BrukerNavnType.class::isInstance)
            .toList();
        if (!sluttbrukere.isEmpty()) {
            LOG.trace("FPFELLES KONTEKST validateRequest: clientSubject inneholdt brukerNavnType {}", sluttbrukere);
            sluttbrukere.forEach(s -> subject.getPrincipals().remove(s));
        }

        if (KontekstHolder.harKontekst()) {
            final var kontekst = KontekstHolder.getKontekst();
            LOG.trace("FPFELLES KONTEKST validateRequest: Tråden inneholdt allerede kontekst for context {} bruker {} identType {}",
                kontekst.getContext(), kontekst.getUid(), kontekst.getIdentType());
            KontekstHolder.fjernKontekst();
        }
    }

    public void setCallAndConsumerId(HttpServletRequest request) {
        String callId = Optional.ofNullable(request.getHeader(CommonHttpHeaders.HEADER_NAV_CALLID))
            .orElseGet(() -> request.getHeader(CommonHttpHeaders.HEADER_NAV_ALT_CALLID));
        if (callId != null) {
            MDCOperations.putCallId(callId);
        } else {
            MDCOperations.putCallId();
        }

        String consumerId = request.getHeader(CommonHttpHeaders.HEADER_NAV_CONSUMER_ID);
        if (consumerId != null) {
            MDCOperations.putConsumerId(consumerId);
        }
    }

    protected AuthStatus handleProtectedResource(Subject clientSubject, HttpServletRequest request) {
        // Get token
        var oidcToken = tokenLocator.getToken(request);
        if (oidcToken.isEmpty()) {
            return FAILURE;
        }
        var claims = oidcToken.map(TokenString::token).map(JwtUtil::getClaims);
        var configuration = claims.map(JwtUtil::getIssuer).flatMap(ConfigProvider::getOpenIDConfiguration);
        if (configuration.isEmpty()) {
            return FAILURE;
        }

        var expiresAt = claims.map(JwtUtil::getExpirationTime).orElseGet(() -> Instant.now().plusSeconds(300));
        var token = new OpenIDToken(configuration.get().type(), OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE, oidcToken.get(), null, expiresAt.toEpochMilli());

        var valideringsResultat = OidcValidation.validerToken(token);
        var sluttbruker = valideringsResultat.isValid() ? valideringsResultat.subject() : null;
        if (sluttbruker != null) {
            KontekstHolder.setKontekst(RequestKontekst.forRequest(sluttbruker.uid(), sluttbruker.shortUid(), sluttbruker.identType(), token, sluttbruker.oid(), sluttbruker.grupper()));
        } else {
            return FAILURE;
        }

        // Dummy - finnes kun pga Jakarta Authentication 3.0 kap 6 LoginModule Bridge Profile.
        LoginContext loginContext = createLoginContext(clientSubject);
        try {
            loginContext.login();
        } catch (LoginException e) {
            return FAILURE;
        }

        clientSubject.getPrincipals().add(new BrukerNavnType(sluttbruker.uid(), sluttbruker.identType()));

        MDCOperations.putUserId(sluttbruker.uid());
        if (MDCOperations.getConsumerId() == null) {
            MDCOperations.putConsumerId(sluttbruker.uid());
        }

        // Handle result
        return notifyContainerAboutLogin(clientSubject, sluttbruker.uid());
    }

    private LoginContext createLoginContext(Subject clientSubject) {
        class NotUsedCallbackHandler implements CallbackHandler {
            @Override
            public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
                if (callbacks.length > 0) {
                    // Should never happen
                    throw new UnsupportedCallbackException(callbacks[0], "OIDC LoginModule skal ikke kalle tilbake");
                }
            }
        }

        CallbackHandler callbackHandler = new NotUsedCallbackHandler();
        try {
            return new LoginContext(OIDC_LOGIN_CONFIG, clientSubject, callbackHandler, loginConfiguration);
        } catch (LoginException le) {
            throw new TekniskException("F-651753", String.format("Kunne ikke finne konfigurasjonen for %s", OIDC_LOGIN_CONFIG), le);
        }
    }

    /**
     * Asks the container to register the given username.
     * <p>
     * Note that after this call returned, the authenticated identity will not be
     * immediately active. This will only take place (should not errors occur) after
     * the {@link ServerAuthContext} or {@link ServerAuthModule} in which this call
     * takes place return control back to the runtime.
     * <p>
     * As a convenience this method returns SUCCESS, so this method can be used in
     * one fluent return statement from an auth module.
     *
     * @param username the user name that will become the caller principal
     * @return {@link AuthStatus#SUCCESS}
     */
    private AuthStatus notifyContainerAboutLogin(Subject clientSubject, String username) {
        try {
            containerCallbackHandler.handle(new Callback[]{new CallerPrincipalCallback(clientSubject, username)});
        } catch (IOException | UnsupportedCallbackException e) {
            // Should not happen
            throw new IllegalStateException(e);
        }
        return SUCCESS;
    }

    @Override
    public AuthStatus secureResponse(MessageInfo messageInfo, Subject serviceSubject) {
        if (KontekstHolder.harKontekst()) {
            KontekstHolder.fjernKontekst();
        } else {
            LOG.trace("FPFELLES KONTEKST fant ikke kontekst som forventet i secureResponse");
        }
        MDC.clear();
        return SEND_SUCCESS;
    }

    @Override
    public void cleanSubject(MessageInfo messageInfo, Subject subject) {
        if (KontekstHolder.harKontekst()) {
            LOG.trace("FPFELLES KONTEKST hadde kontekst ved cleanSubject");
            KontekstHolder.fjernKontekst();
        }
        if (subject != null) {
            subject.getPrincipals().clear();
        }
    }

}
