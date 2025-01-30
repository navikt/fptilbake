package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac;

import static no.nav.vedtak.sikkerhet.abac.AbacResultat.AVSLÅTT_ANNEN_ÅRSAK;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.tilbakekreving.fagsystem.ApplicationName;
import no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.AppPdpKlientImpl;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;
import no.nav.vedtak.sikkerhet.abac.PepImpl;
import no.nav.vedtak.sikkerhet.abac.Tilgangsbeslutning;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.pdp.PdpKlientImpl;
import no.nav.vedtak.sikkerhet.tilgang.AnsattGruppeKlient;
import no.nav.vedtak.sikkerhet.tilgang.PopulasjonKlient;

@Default
@Alternative
@Priority(Interceptor.Priority.APPLICATION + 1)
public class AppPepImpl extends PepImpl {

    private static final Logger LOG = LoggerFactory.getLogger(PepImpl.class);
    private static final String PIP = ForeldrepengerAttributter.RESOURCE_TYPE_INTERNAL_PIP;

    private final PdpRequestBuilder pdpRequestBuilder;
    private final AppPdpKlientImpl lokalPdpKlient;


    @Inject
    public AppPepImpl(PdpKlientImpl pdpKlient,
                      PopulasjonKlient populasjonKlient,
                      AnsattGruppeKlient ansattGruppeKlient,
                      PdpRequestBuilder pdpRequestBuilder,
                      AppPdpKlientImpl appPdpKlient) {
        super(pdpKlient, populasjonKlient, ansattGruppeKlient, pdpRequestBuilder);
        this.pdpRequestBuilder = pdpRequestBuilder;
        this.lokalPdpKlient = appPdpKlient;
    }

    @Override
    public Tilgangsbeslutning vurderTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter) {
        var applikasjon = ApplicationName.hvilkenTilbake();
        switch (applikasjon) {
            case FPTILBAKE -> {
                return super.vurderTilgang(beskyttetRessursAttributter);
            }
            case K9TILBAKE -> {
                var appRessurser = pdpRequestBuilder.lagAppRessursData(beskyttetRessursAttributter.getDataAttributter());

                if (IdentType.Systemressurs.equals(beskyttetRessursAttributter.getIdentType())) {
                    return super.vurderLokalTilgang(beskyttetRessursAttributter, appRessurser);
                } else if (PIP.equals(beskyttetRessursAttributter.getResourceType())) { // pip tilgang bør vurderes kun lokalt
                    return new Tilgangsbeslutning(AVSLÅTT_ANNEN_ÅRSAK, beskyttetRessursAttributter, appRessurser);
                } else {
                    return lokalPdpKlient.forespørTilgang(beskyttetRessursAttributter, "k9", appRessurser);
                }
            }
            default -> throw new IllegalStateException("applikasjonsnavn er satt til " + applikasjon + " som ikke er en støttet verdi");
        }

    }

}
