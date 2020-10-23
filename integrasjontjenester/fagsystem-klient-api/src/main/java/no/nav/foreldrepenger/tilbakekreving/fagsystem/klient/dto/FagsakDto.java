package no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FagsakDto {

    private String saksnummer;
    private FagsakYtelseType sakstype;
    private String aktoerId;

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public Saksnummer getSaksnummer() {
        return new Saksnummer(saksnummer);
    }

    public FagsakYtelseType getSakstype() {
        return sakstype;
    }

    public void setSakstype(FagsakYtelseType sakstype) {
        this.sakstype = sakstype;
    }

    public String getAktoerId() {
        return aktoerId;
    }

    public void setAktoerId(String aktoerId) {
        this.aktoerId = aktoerId;
    }
}
