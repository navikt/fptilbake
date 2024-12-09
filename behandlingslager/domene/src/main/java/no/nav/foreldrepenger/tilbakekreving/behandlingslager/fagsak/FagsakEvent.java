package no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak;


import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

/**
 * Marker interface for events fyrt på en Fagsak.
 * Disse fyres ved hjelp av CDI Events.
 */
public interface FagsakEvent {

    Long getFagsakId();

    Saksnummer getSaksnummer();

}
