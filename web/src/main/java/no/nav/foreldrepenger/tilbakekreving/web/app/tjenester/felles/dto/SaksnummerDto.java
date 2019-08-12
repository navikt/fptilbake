package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.felles.dto;

import java.util.Objects;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

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
    @Digits(integer = 18, fraction = 0)
    private final String saksnummer;

    public SaksnummerDto(Long saksnummer) {
        Objects.requireNonNull(saksnummer, "saksnummer");
        this.saksnummer = saksnummer.toString();
    }

    public SaksnummerDto(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public SaksnummerDto(Saksnummer saksnummer) {
        this.saksnummer = saksnummer.getVerdi();
    }


    public String getVerdi() {
        return saksnummer;
    }

    public Long getVerdiSomLong() {
        return Long.parseLong(saksnummer);
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
