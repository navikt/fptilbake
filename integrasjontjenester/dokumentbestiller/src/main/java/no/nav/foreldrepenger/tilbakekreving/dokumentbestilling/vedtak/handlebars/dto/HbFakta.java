package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.KodelisteSomKodeSerialiserer;
import no.nav.vedtak.util.Objects;

public class HbFakta {
    @JsonProperty("hendelsetype")
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private HendelseType hendelsetype;
    @JsonProperty("hendelseundertype")
    @JsonSerialize(using = KodelisteSomKodeSerialiserer.class)
    private HendelseUnderType hendelseundertype;
    @JsonProperty("fritekst-fakta")
    private String fritekstFakta;

    private HbFakta() {
    }

    public void setFritekstFakta(String fritekstFakta) {
        this.fritekstFakta = fritekstFakta;
    }

    public String getFritekstFakta() {
        return fritekstFakta;
    }

    public HendelseType getHendelsetype() {
        return hendelsetype;
    }

    public HendelseUnderType getHendelseundertype() {
        return hendelseundertype;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private HbFakta kladd = new HbFakta();

        public HbFakta.Builder medHendelsetype(HendelseType hendelsetype) {
            kladd.hendelsetype = hendelsetype;
            return this;
        }

        public HbFakta.Builder medHendelseUndertype(HendelseUnderType hendelseUnderType) {
            kladd.hendelseundertype = hendelseUnderType;
            return this;
        }

        public HbFakta.Builder medFritekstFakta(String fritekstFakta) {
            kladd.fritekstFakta = fritekstFakta;
            return this;
        }

        public HbFakta build() {
            Objects.check(kladd.hendelsetype != null, "hendelsetype er ikke satt");
            Objects.check(kladd.hendelseundertype != null, "hendelseundertype er ikke satt");
            return kladd;
        }
    }
}
