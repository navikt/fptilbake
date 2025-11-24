package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp;

import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;


public class XacmlRequestMapper {

    static String mapActionType(ActionType actionType) {
        return switch (actionType) {
            case READ -> "read";
            case UPDATE -> "update";
            case CREATE -> "create";
            case DELETE -> "delete";
            case DUMMY -> null;
        };
    }

    static String mapResourceType(ResourceType resourceType) {
        return switch (resourceType) {
            case APPLIKASJON -> "no.nav.abac.attributter.k9";
            case FAGSAK -> "no.nav.abac.attributter.k9.fagsak";
            case VENTEFRIST -> "no.nav.abac.attributter.k9.fagsak.ventefrist";
            case DRIFT -> "no.nav.abac.attributter.k9.drift";
            default -> throw new IllegalStateException("Unexpected value: " + resourceType);
        };
    }
}
