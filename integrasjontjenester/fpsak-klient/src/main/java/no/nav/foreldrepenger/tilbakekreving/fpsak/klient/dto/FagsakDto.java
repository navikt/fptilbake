package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FagsakDto {

    private Long saksnummer;
    private String sakstype;

    public Saksnummer getSaksnummer() {
        return new Saksnummer(Long.toString(saksnummer));
    }

    public void setSaksnummer(Long saksnummer) {
        this.saksnummer = saksnummer;
    }

    public String getSakstype() {
        return sakstype;
    }

    public void setSakstype(String sakstype) {
        this.sakstype = sakstype;
    }
}
