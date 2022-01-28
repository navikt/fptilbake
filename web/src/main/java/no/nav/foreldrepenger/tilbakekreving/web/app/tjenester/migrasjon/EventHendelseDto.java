package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.migrasjon;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

public class EventHendelseDto implements AbacDto {

    @NotNull
    @Size(min = 1, max = 150)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String hendelse;

    public EventHendelseDto(String hendelse) {
        this.hendelse = hendelse;
    }

    public String getHendelse() {
        return hendelse;
    }

    public void setHendelse(String hendelse) {
        this.hendelse = hendelse;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();
    }
}
