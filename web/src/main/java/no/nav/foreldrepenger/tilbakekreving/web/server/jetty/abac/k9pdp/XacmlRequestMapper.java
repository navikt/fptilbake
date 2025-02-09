package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.xacml.Category;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.xacml.NavFellesAttributter;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.xacml.XacmlRequest;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;


public class XacmlRequestMapper {

    public static XacmlRequest lagXacmlRequest(BeskyttetRessursAttributter beskyttetRessursAttributter,
                                               String domene, AppRessursData appRessursData, Token token) {
        var actionAttributes = new XacmlRequest.Attributes(List.of(actionInfo(beskyttetRessursAttributter)));

        List<XacmlRequest.AttributeAssignment> envList = new ArrayList<>();
        envList.add(getPepIdInfo());
        envList.addAll(getTokenEnvironmentAttrs(token));

        var envAttributes = new XacmlRequest.Attributes(envList);

        List<XacmlRequest.Attributes> resourceAttributes = new ArrayList<>();
        var identer = hentIdenter(appRessursData);
        if (identer.isEmpty()) {
            resourceAttributes.add(resourceInfo(beskyttetRessursAttributter, domene, appRessursData, null));
        } else {
            identer.forEach(ident -> resourceAttributes.add(resourceInfo(beskyttetRessursAttributter, domene, appRessursData, ident)));
        }

        Map<Category, List<XacmlRequest.Attributes>> requestMap = new HashMap<>();
        requestMap.put(Category.Action, List.of(actionAttributes));
        requestMap.put(Category.Environment, List.of(envAttributes));
        requestMap.put(Category.Resource, resourceAttributes);
        return new XacmlRequest(requestMap);
    }

    private static XacmlRequest.Attributes resourceInfo(BeskyttetRessursAttributter beskyttetRessursAttributter,
                                                        String domene,
                                                        AppRessursData appRessursData,
                                                        Ident ident) {
        List<XacmlRequest.AttributeAssignment> attributes = new ArrayList<>();

        attributes.add(new XacmlRequest.AttributeAssignment(NavFellesAttributter.RESOURCE_FELLES_DOMENE, domene));
        attributes.add(
            new XacmlRequest.AttributeAssignment(NavFellesAttributter.RESOURCE_FELLES_RESOURCE_TYPE, mapResourceType(beskyttetRessursAttributter.getResourceType())));

        appRessursData.getResources()
            .values()
            .stream()
            .map(ressursData -> new XacmlRequest.AttributeAssignment(ressursData.nøkkel().getKey(), ressursData.verdi()))
            .forEach(attributes::add);

        if (ident != null) {
            attributes.add(new XacmlRequest.AttributeAssignment(ident.key(), ident.ident()));
        }
        return new XacmlRequest.Attributes(attributes);
    }

    private static XacmlRequest.AttributeAssignment actionInfo(final BeskyttetRessursAttributter beskyttetRessursAttributter) {
        return new XacmlRequest.AttributeAssignment(NavFellesAttributter.XACML10_ACTION_ID, mapActionType(beskyttetRessursAttributter.getActionType()));
    }

    private static XacmlRequest.AttributeAssignment getPepIdInfo() {
        return new XacmlRequest.AttributeAssignment(NavFellesAttributter.ENVIRONMENT_FELLES_PEP_ID, ApplicationName.hvilkenTilbakeAppName());
    }

    private static List<XacmlRequest.AttributeAssignment> getTokenEnvironmentAttrs(Token token) {
        String envTokenBodyAttributt = switch (token.getTokenType()) {
            case OIDC -> NavFellesAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
            case TOKENX -> NavFellesAttributter.ENVIRONMENT_FELLES_TOKENX_TOKEN_BODY;
        };
        var assignement = new XacmlRequest.AttributeAssignment(envTokenBodyAttributt, token.getTokenBody());
        return List.of(assignement);
    }

    private static List<Ident> hentIdenter(AppRessursData appRessursData) {
        List<Ident> identer = new ArrayList<>();
        appRessursData.getAktørIdSet()
            .stream()
            .map(it -> new Ident(NavFellesAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, it))
            .forEach(identer::add);

        appRessursData.getFødselsnumre().stream().map(it -> new Ident(NavFellesAttributter.RESOURCE_FELLES_PERSON_FNR, it)).forEach(identer::add);

        return identer;
    }

    public record Ident(String key, String ident) {
    }

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
            case FAGSAK -> "nno.nav.abac.attributter.k9.fagsak";
            case VENTEFRIST -> "no.nav.abac.attributter.k9.fagsak.ventefrist";
            case DRIFT -> "no.nav.abac.attributter.k9.drift";
            default -> throw new IllegalStateException("Unexpected value: " + resourceType);
        };
    }
}
