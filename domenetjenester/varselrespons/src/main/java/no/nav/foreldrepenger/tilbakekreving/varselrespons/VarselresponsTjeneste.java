package no.nav.foreldrepenger.tilbakekreving.varselrespons;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.varsel.respons.Varselrespons;

public interface VarselresponsTjeneste {

    void lagreRespons(long behandlingId, ResponsKanal responsKanal, Boolean akseptertFaktagrunnlag);

    void lagreRespons(long behandlingId, ResponsKanal kanal);

    Optional<Varselrespons> hentRespons(long behandlingId);

}
