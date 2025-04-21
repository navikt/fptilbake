package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;

import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.AppPdpKlientImpl;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.K9AbacResultat;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.K9SystemressursPolicies;
import no.nav.vedtak.sikkerhet.abac.AbacAuditlogger;
import no.nav.vedtak.sikkerhet.abac.AbacResultat;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;
import no.nav.vedtak.sikkerhet.abac.PepImpl;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.tilgang.AnsattGruppeKlient;
import no.nav.vedtak.sikkerhet.tilgang.PopulasjonKlient;

@Default
@Alternative
@Priority(Interceptor.Priority.APPLICATION + 1)
public class AppPepImpl extends PepImpl {

    private final AppPdpKlientImpl lokalPdpKlient;

    @Inject
    public AppPepImpl(PopulasjonKlient populasjonKlient,
                      AnsattGruppeKlient ansattGruppeKlient,
                      AbacAuditlogger abacAuditlogger,
                      PdpRequestBuilder pdpRequestBuilder,
                      AppPdpKlientImpl appPdpKlient) {
        super(abacAuditlogger, populasjonKlient, ansattGruppeKlient, pdpRequestBuilder);
        this.lokalPdpKlient = appPdpKlient;
    }

    @Override
    public AbacResultat vurderTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter) {
        var applikasjon = ApplicationName.hvilkenTilbake();
        switch (applikasjon) {
            case FPTILBAKE -> {
                return super.vurderTilgang(beskyttetRessursAttributter);
            }
            case K9TILBAKE -> {
                var vurdering = vurderK9Tilbake(beskyttetRessursAttributter);
                return mapK9AbacResultat(vurdering);
            }
            default -> throw new IllegalStateException("applikasjonsnavn er satt til " + applikasjon + " som ikke er en støttet verdi");
        }

    }

    private K9AbacResultat vurderK9Tilbake(BeskyttetRessursAttributter beskyttetRessursAttributter) {
        if (IdentType.Systemressurs.equals(beskyttetRessursAttributter.getIdentType())) {
            return K9SystemressursPolicies.vurderTilgang(beskyttetRessursAttributter);
        } else if (ResourceType.PIP.equals(beskyttetRessursAttributter.getResourceType())) { // pip tilgang bør vurderes kun lokalt
            return K9AbacResultat.AVSLÅTT_ANNEN_ÅRSAK;
        } else {
            return lokalPdpKlient.forespørTilgang(beskyttetRessursAttributter);
        }
    }

    private static AbacResultat mapK9AbacResultat(K9AbacResultat resultat) {
        return switch (resultat) {
            case GODKJENT -> AbacResultat.GODKJENT;
            case AVSLÅTT_KODE_7 -> AbacResultat.AVSLÅTT_KODE_7;
            case AVSLÅTT_KODE_6 -> AbacResultat.AVSLÅTT_KODE_6;
            case AVSLÅTT_EGEN_ANSATT -> AbacResultat.AVSLÅTT_EGEN_ANSATT;
            case AVSLÅTT_ANNEN_ÅRSAK -> AbacResultat.AVSLÅTT_ANNEN_ÅRSAK;
        };
    }

}
