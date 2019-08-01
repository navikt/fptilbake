package no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

public interface FagsakRepository extends BehandlingslagerRepository {

    Fagsak finnEksaktFagsak(long fagsakId);

    Optional<Fagsak> finnUnikFagsak(long fagsakId);

    Long lagre(Fagsak fagsak);

    List<Fagsak> hentForBruker(AktørId aktørId);

    Optional<Fagsak> hentSakGittSaksnummer(Saksnummer saksnummer);

    Fagsak hentEksaktFagsakForGittSaksnummer(Saksnummer saksnummer);

    List<Fagsak> hentForBrukerAktørId(AktørId aktørId);

    /**
     * Oppderer status på fagsak.
     *
     * @param fagsakId - id på fagsak
     * @param status   - ny status
     */
    void oppdaterFagsakStatus(Long fagsakId, FagsakStatus status);

    List<Fagsak> hentForStatus(FagsakStatus fagsakStatus);
}
