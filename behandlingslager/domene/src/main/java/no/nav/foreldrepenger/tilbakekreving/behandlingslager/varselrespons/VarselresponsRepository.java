package no.nav.foreldrepenger.tilbakekreving.behandlingslager.varselrespons;

import java.util.Optional;

public interface VarselresponsRepository {

    void lagre(Varselrespons varselrespons);
    Optional<Varselrespons> hentRespons(Long behandlingId);

}
