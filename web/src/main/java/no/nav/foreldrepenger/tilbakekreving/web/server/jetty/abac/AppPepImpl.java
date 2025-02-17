package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;

import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.AppPdpKlientImpl;
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
                if (IdentType.Systemressurs.equals(beskyttetRessursAttributter.getIdentType())) {
                    return K9SystemressursPolicies.vurderTilgang(beskyttetRessursAttributter);
                } else if (ResourceType.PIP.equals(beskyttetRessursAttributter.getResourceType())) { // pip tilgang bør vurderes kun lokalt
                    return AbacResultat.AVSLÅTT_ANNEN_ÅRSAK;
                } else {
                    return lokalPdpKlient.forespørTilgang(beskyttetRessursAttributter);
                }
            }
            default -> throw new IllegalStateException("applikasjonsnavn er satt til " + applikasjon + " som ikke er en støttet verdi");
        }

    }

}
