package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.AppPdpKlientImpl;
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

    private static final Logger LOG = LoggerFactory.getLogger(AppPepImpl.class);

    private final PdpRequestBuilder pdpRequestBuilder;
    private final AppPdpKlientImpl lokalPdpKlient;

    @Inject
    public AppPepImpl(PopulasjonKlient populasjonKlient,
                      AnsattGruppeKlient ansattGruppeKlient,
                      AbacAuditlogger abacAuditlogger,
                      PdpRequestBuilder pdpRequestBuilder,
                      AppPdpKlientImpl appPdpKlient) {
        super(abacAuditlogger, populasjonKlient, ansattGruppeKlient, pdpRequestBuilder);
        this.pdpRequestBuilder = pdpRequestBuilder;
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
                var appRessurser = pdpRequestBuilder.lagAppRessursData(beskyttetRessursAttributter.getDataAttributter());

                if (IdentType.Systemressurs.equals(beskyttetRessursAttributter.getIdentType())) {
                    var vurdering = super.forespørTilgang(beskyttetRessursAttributter, appRessurser);
                    return vurdering.tilgangResultat();
                } else if (ResourceType.PIP.equals(beskyttetRessursAttributter.getResourceType())) { // pip tilgang bør vurderes kun lokalt
                    return AbacResultat.AVSLÅTT_ANNEN_ÅRSAK;
                } else {
                    var resultat = lokalPdpKlient.forespørTilgang(beskyttetRessursAttributter, "k9", appRessurser);
                    return resultat.beslutningKode();
                }
            }
            default -> throw new IllegalStateException("applikasjonsnavn er satt til " + applikasjon + " som ikke er en støttet verdi");
        }

    }

}
