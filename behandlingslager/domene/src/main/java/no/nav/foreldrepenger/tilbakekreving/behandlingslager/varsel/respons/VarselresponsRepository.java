package no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.respons;

import java.util.Optional;

public interface VarselresponsRepository {

    void lagre(Varselrespons varselrespons);
    Optional<Varselrespons> hentRespons(Long behandlingId);

}
