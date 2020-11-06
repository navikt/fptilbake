package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto.totrinn;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;

public class TotrinnskontrollAksjonspunkterDto {
    @JsonProperty("aksjonspunktKode")
    private String aksjonspunktKode;

    @JsonProperty("besluttersBegrunnelse")
    private String besluttersBegrunnelse;

    @JsonProperty("totrinnskontrollGodkjent")
    private Boolean totrinnskontrollGodkjent;

    @JsonProperty("vurderPaNyttArsaker")
    private Set<VurderÅrsak> arsaker;


    public String getAksjonspunktKode() {
        return aksjonspunktKode;
    }


    public String getBesluttersBegrunnelse() {
        return besluttersBegrunnelse;
    }

    public Boolean getTotrinnskontrollGodkjent() {
        return totrinnskontrollGodkjent;
    }

    public Set<VurderÅrsak> getArsaker() {
        return arsaker;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TotrinnskontrollAksjonspunkterDto kladd = new TotrinnskontrollAksjonspunkterDto();

        public Builder medAksjonspunktKode(String aksjonspunktKode) {
            kladd.aksjonspunktKode = aksjonspunktKode;
            return this;
        }

        public Builder medBesluttersBegrunnelse(String besluttersBegrunnelse) {
            kladd.besluttersBegrunnelse = besluttersBegrunnelse;
            return this;
        }

        public Builder medTotrinnskontrollGodkjent(Boolean totrinnskontrollGodkjent) {
            kladd.totrinnskontrollGodkjent = totrinnskontrollGodkjent;
            return this;
        }

        public Builder medVurderPaNyttArsaker(Set<VurderÅrsak> vurderPaNyttArsaker) {
            kladd.arsaker = vurderPaNyttArsaker;
            return this;
        }

        public TotrinnskontrollAksjonspunkterDto build() {
            return kladd;
        }

    }
}
