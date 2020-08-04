package no.nav.foreldrepenger.tilbakekreving.web.server.jetty.testtjenester;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class KravgrunnlagDto implements AbacDto {

    @NotNull
    @Valid
    private DetaljertKravgrunnlagDto kravGrunnlag;

    public DetaljertKravgrunnlagDto getKravGrunnlag() {
        return kravGrunnlag;
    }

    public void setKravGrunnlag(DetaljertKravgrunnlagDto kravGrunnlag) {
        this.kravGrunnlag = kravGrunnlag;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();
    }
}
