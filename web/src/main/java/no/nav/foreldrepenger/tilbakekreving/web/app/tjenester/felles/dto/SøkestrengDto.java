package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

public class SøkestrengDto implements AbacDto {

    @NotNull
    @Size(min = 1, max = 50)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String søkestreng;

    public SøkestrengDto(String søkestreng) {
        this.søkestreng = søkestreng;
    }

    public String getSøkestreng() {
        return søkestreng;
    }

    public void setSøkestreng(String søkestreng) {
        this.søkestreng = søkestreng;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();
    }
}
