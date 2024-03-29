package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public class SaksnummerDto implements AbacDto {

    @JsonProperty("saksnummer")
    @NotNull
    @Size(min = 1, max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Saksnummer ${validatedValue} matcher ikke tillatt pattern '{regexp}'")
    private final String saksnummer;

    public SaksnummerDto(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public SaksnummerDto(Long saksnummer) {
        this.saksnummer = String.valueOf(saksnummer);
    }

    public SaksnummerDto(Saksnummer saksnummer) {
        this.saksnummer = saksnummer.getVerdi();
    }

    public String getVerdi() {
        return saksnummer;
    }

    @Override
    public String toString() {
        return "SaksnummerDto{" +
                "saksnummer='" + saksnummer + '\'' +
                '}';
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.SAKSNUMMER, getVerdi());
    }
}
