package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FagsakDto {
    private KodeDto sakstype;

    public KodeDto getSakstype() {
        return sakstype;
    }

    public void setSakstype(KodeDto sakstype) {
        this.sakstype = sakstype;
    }
}
