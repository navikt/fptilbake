package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.xacml.Advice;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.xacml.Decision;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.xacml.XacmlResponse;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.xacml.XacmlResponseMapper;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.sikkerhet.abac.AbacResultat;
import no.nav.vedtak.sikkerhet.abac.TokenProvider;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;

@ApplicationScoped
public class AppPdpKlientImpl {

    private static final Logger LOG = LoggerFactory.getLogger(AppPdpKlientImpl.class);

    private AppPdpConsumerImpl pdp;
    private TokenProvider tokenProvider;
    private K9AbacAuditlogger abacAuditlogger;

    public AppPdpKlientImpl() {
        // CDI
    }

    @Inject
    public AppPdpKlientImpl(AppPdpConsumerImpl pdp, TokenProvider tokenProvider, K9AbacAuditlogger abacAuditlogger) {
        this.pdp = pdp;
        this.tokenProvider = tokenProvider;
        this.abacAuditlogger = abacAuditlogger;
    }

    public Tilgangsbeslutning forespørTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter, String domene, AppRessursData appRessursData) {
        var token = Token.withOidcToken(tokenProvider.openIdToken());
        var request = XacmlRequestMapper.lagXacmlRequest(beskyttetRessursAttributter, domene, appRessursData, token);
        var response = pdp.evaluate(request);
        var hovedresultat = resultatFraResponse(response);
        abacAuditlogger.loggUtfall(hovedresultat, beskyttetRessursAttributter, appRessursData);
        return new Tilgangsbeslutning(hovedresultat, beskyttetRessursAttributter, appRessursData);
    }

    private static AbacResultat resultatFraResponse(XacmlResponse response) {
        var decisions = XacmlResponseMapper.getDecisions(response);

        for (var decision : decisions) {
            if (decision == Decision.Indeterminate) {
                throw new TekniskException("F-080281",
                    String.format("Decision %s fra PDP, dette skal aldri skje. Full JSON response: %s", decision, response));
            }
        }

        var biasedDecision = createAggregatedDecision(decisions);
        handlObligation(response);

        if (biasedDecision == Decision.Permit) {
            return AbacResultat.GODKJENT;
        }

        var denyAdvice = XacmlResponseMapper.getAdvice(response);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Deny fra PDP, advice var: {}", LoggerUtils.toStringWithoutLineBreaks(denyAdvice));
        }
        if (denyAdvice.contains(Advice.DENY_KODE_6)) {
            return AbacResultat.AVSLÅTT_KODE_6;
        }
        if (denyAdvice.contains(Advice.DENY_KODE_7)) {
            return AbacResultat.AVSLÅTT_KODE_7;
        }
        if (denyAdvice.contains(Advice.DENY_EGEN_ANSATT)) {
            return AbacResultat.AVSLÅTT_EGEN_ANSATT;
        }
        return AbacResultat.AVSLÅTT_ANNEN_ÅRSAK;
    }

    private static Decision createAggregatedDecision(List<Decision> decisions) {
        for (var decision : decisions) {
            if (decision != Decision.Permit) {
                return Decision.Deny;
            }
        }
        return Decision.Permit;
    }

    private static void handlObligation(XacmlResponse response) {
        var obligations = XacmlResponseMapper.getObligations(response);
        if (!obligations.isEmpty()) {
            throw new TekniskException("F-576027", String.format("Mottok ukjente obligations fra PDP: %s", obligations));
        }
    }
}
