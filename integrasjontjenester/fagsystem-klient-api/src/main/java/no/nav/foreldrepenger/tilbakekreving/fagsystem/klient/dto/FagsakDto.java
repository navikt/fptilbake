package no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FagsakDto {

    private String saksnummer;
    private FagsakYtelseType sakstype; // Fra k9-sak?
    private FagsakYtelseType fagsakYtelseType;  // Fra fpsak

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public Saksnummer getSaksnummer() {
        return new Saksnummer(saksnummer);
    }

    public FagsakYtelseType getSakstype() {
        return Optional.ofNullable(sakstype).orElse(fagsakYtelseType);
    }

    public void setSakstype(FagsakYtelseType sakstype) {
        this.sakstype = sakstype;
    }

    public FagsakYtelseType getFagsakYtelseType() {
        return fagsakYtelseType != null ? fagsakYtelseType : sakstype;
    }

    public void setFagsakYtelseType(FagsakYtelseType fagsakYtelseType) {
        this.fagsakYtelseType = fagsakYtelseType;
    }

}
