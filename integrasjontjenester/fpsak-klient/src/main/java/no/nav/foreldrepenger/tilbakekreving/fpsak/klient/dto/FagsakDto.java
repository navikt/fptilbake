package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FagsakDto {

    private Long saksnummer;
    private FagsakYtelseType sakstype;

    public Saksnummer getSaksnummer() {
        return new Saksnummer(Long.toString(saksnummer));
    }

    public void setSaksnummer(Long saksnummer) {
        this.saksnummer = saksnummer;
    }

    public FagsakYtelseType getSakstype() {
        return sakstype;
    }

    public void setSakstype(FagsakYtelseType sakstype) {
        this.sakstype = sakstype;
    }
}
