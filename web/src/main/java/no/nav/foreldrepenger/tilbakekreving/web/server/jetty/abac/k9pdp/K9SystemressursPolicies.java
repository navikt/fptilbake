package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp;

import java.util.Optional;

import no.nav.foreldrepenger.konfig.Cluster;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.sikkerhet.abac.AbacResultat;
import no.nav.vedtak.sikkerhet.abac.beskyttet.AvailabilityType;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.oidc.config.AzureProperty;

/**
 * Inneholder tilgangspolicies for innkommende kall fra Systemressurs (Client Credentials)
 * - ConsumerId fra andre cluster-klasser er ikke tillatt (prod - dev)
 * - ConsumerId fra andre namespace er bare tillatt når endepunktet har AvailabilityType = ALL
 */
public class K9SystemressursPolicies {

    private static final Environment ENV = Environment.current();

    // Format: json array av objekt("name", "clientId");
    private static final String PRE_AUTHORIZED = Optional.ofNullable(ENV.getProperty(AzureProperty.AZURE_APP_PRE_AUTHORIZED_APPS.name()))
        .orElseGet(() -> ENV.getProperty(AzureProperty.AZURE_APP_PRE_AUTHORIZED_APPS.name().toLowerCase().replace('_', '.')));
    private static final Cluster RESIDENT_CLUSTER = ENV.getCluster();
    private static final String RESIDENT_NAMESPACE = ENV.namespace();

    private K9SystemressursPolicies() {
        // Hindre instans
    }

    public static AbacResultat vurderTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter) {
        if (!beskyttetRessursAttributter.getIdentType().erSystem()) {
            return AbacResultat.AVSLÅTT_ANNEN_ÅRSAK;
        }
        if (!riktigClusterNamespacePreAuth(beskyttetRessursAttributter.getBrukerId(), beskyttetRessursAttributter.getAvailabilityType())) {
            return AbacResultat.AVSLÅTT_ANNEN_ÅRSAK;
        }
        // Skal ikke utføre videre kontroll (fagtilgang, populasjonstilgang) for System.
        return AbacResultat.GODKJENT;
    }

    public static boolean riktigClusterNamespacePreAuth(String consumerId, AvailabilityType availabilityType) {
        if (consumerId == null || !PRE_AUTHORIZED.contains(consumerId)) {
            return false;
        }
        var splittConsumerId = consumerId.split(":");
        return erISammeClusterKlasse(splittConsumerId) && erISammeNamespace(splittConsumerId, availabilityType);
    }

    private static boolean erISammeClusterKlasse(String[] elementer) {
        return elementer.length > 0 && RESIDENT_CLUSTER.isSameClass(Cluster.of(elementer[0]));
    }

    private static boolean erISammeNamespace(String[] elementer, AvailabilityType availabilityType) {
        return AvailabilityType.ALL.equals(availabilityType) || elementer.length > 1 && RESIDENT_NAMESPACE.equals(elementer[1]);
    }


}
