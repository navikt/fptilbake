package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp;

import no.nav.vedtak.sikkerhet.abac.AbacResultat;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;

public record Tilgangsbeslutning(AbacResultat beslutningKode,
                                 BeskyttetRessursAttributter beskyttetRessursAttributter,
                                 AppRessursData appRessursData) {
}
