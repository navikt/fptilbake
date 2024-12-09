package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.sikkerhet.jaspic;

import java.util.Set;
import java.util.UUID;

import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.validator.OidcTokenValidatorConfig;

public final class OidcValidation {

    private OidcValidation() {
    }

    public record SluttBruker(String uid, String shortUid, IdentType identType, UUID oid, Set<AnsattGruppe> grupper) {}

    public record Resultat(boolean isValid, SluttBruker subject, String errorMessage) {}

    public static Resultat validerToken(OpenIDToken openIDToken) {
        if (openIDToken == null || openIDToken.provider() == null) {
            return new Resultat(false, null, null);
        }
        var tokenValidator = OidcTokenValidatorConfig.instance().getValidator(openIDToken.provider());
        var validateResult = tokenValidator.validate(openIDToken.primary());
        if (validateResult.isValid()) {
            return new Resultat(true, new SluttBruker(validateResult.getSubject(), validateResult.getCompactSubject(),
                validateResult.getIdentType(), validateResult.oid(), validateResult.getGrupper()),null);
        }
        return new Resultat(false, null, validateResult.getErrorMessage());
    }



}
