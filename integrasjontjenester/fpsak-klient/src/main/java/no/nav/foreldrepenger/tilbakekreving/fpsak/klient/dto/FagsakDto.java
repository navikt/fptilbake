package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FagsakDto {

    private Long saksnummer;

    public Saksnummer getSaksnummer() {
        return new Saksnummer(Long.toString(saksnummer));
    }

    public void setSaksnummer(Long saksnummer) {
        this.saksnummer = saksnummer;
    }
}
