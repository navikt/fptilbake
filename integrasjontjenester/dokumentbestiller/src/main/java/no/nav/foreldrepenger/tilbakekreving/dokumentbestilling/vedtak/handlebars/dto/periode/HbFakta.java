package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.KodeverdiSomKodeSerialiserer;

public class HbFakta {
    @JsonProperty("hendelsetype")
    @JsonSerialize(using = KodeverdiSomKodeSerialiserer.class)
    private HendelseType hendelsetype;
    @JsonProperty("hendelseundertype")
    @JsonSerialize(using = KodeverdiSomKodeSerialiserer.class)
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
            Objects.requireNonNull(kladd.hendelsetype, "hendelsetype er ikke satt");
            Objects.requireNonNull(kladd.hendelseundertype, "hendelseundertype er ikke satt");
            return kladd;
        }
    }
}
