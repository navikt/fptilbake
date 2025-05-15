package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.k9pdp.sifabacpdp.dto.resultat;

import java.util.Set;

public record Tilgangsbeslutning(boolean harTilgang, Set<IkkeTilgangÅrsak> årsakerForIkkeTilgang) {
}
