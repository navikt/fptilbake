package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.tilbakekrevingsgrunnlag;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import no.nav.foreldrepenger.tilbakekreving.behandling.dto.DetaljertKravgrunnlagDto;
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
