package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.abac.fp;

import java.util.Set;

import javax.validation.Valid;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;

public record PipDto(@Valid Set<AktørId> aktørIder, String fagsakStatus, String behandlingStatus) {
}
