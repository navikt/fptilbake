package no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak;

import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

/**
 * Lås, fungerer som token som indikerer at write-lock er tatt ut. Kreves av lagre metoder.
 * Er knyttet til underliggende lock på database rad (LockModeType#PESSIMISTIC_WRITE).
 * Går out-of-scope og vil ikke være gyldig transaksjonen er ferdig.
 * <p>
 * Låsen initialiseres utelukkende via {@link FagsakRepository}. Og verfisers også her senere ved lagring som
 * påvirker {@link Fagsak}.
 * <p>
 * NB: Kan kun holdes per request/transaksjon.
 * <p>
 * Når lagring skjer vil relevante entiteter også få sin versjon inkrementert
 */
public record FagsakLås(Saksnummer saksnummer) {

    /**
     * protected, unngå å opprette utenfor denne pakken. Kan overstyres kun til test
     */
    public FagsakLås {
        Objects.requireNonNull(saksnummer, "saksnummer");
    }
}
