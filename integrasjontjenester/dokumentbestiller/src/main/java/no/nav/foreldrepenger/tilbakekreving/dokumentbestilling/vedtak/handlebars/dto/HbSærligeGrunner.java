package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;

public class HbSærligeGrunner {
    @JsonProperty("størrelse")
    private boolean særligGrunnStørrelse;
    @JsonProperty("annet")
    private boolean særligGrunnAnnet;
    @JsonProperty("nav-feil")
    private boolean særligGrunnNav;
    @JsonProperty("tid")
    private boolean særligGrunnTid;
    @JsonProperty("fritekst")
    private String fritekstSærligeGrunner;
    @JsonProperty("fritekst-annet")
    private String fritekstSærligeGrunnerAnnet;

    private HbSærligeGrunner() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public void setFritekstSærligeGrunner(String fritekstSærligeGrunner) {
        this.fritekstSærligeGrunner = fritekstSærligeGrunner;
    }

    public String getFritekstSærligeGrunner() {
        return fritekstSærligeGrunner;
    }

    public String getFritekstSærligeGrunnerAnnet() {
        return fritekstSærligeGrunnerAnnet;
    }

    public void setFritekstSærligeGrunnerAnnet(String fritekstSærligeGrunnerAnnet) {
        this.fritekstSærligeGrunnerAnnet = fritekstSærligeGrunnerAnnet;
    }

    public static class Builder {
        private HbSærligeGrunner kladd = new HbSærligeGrunner();

        public HbSærligeGrunner.Builder medFritekstSærligeGrunner(String fritekstSærligeGrunner) {
            kladd.fritekstSærligeGrunner = fritekstSærligeGrunner;
            return this;
        }

        public HbSærligeGrunner.Builder medFritekstSærligeGrunnerAnnet(String fritekstSærligGrunnAnnet) {
            kladd.fritekstSærligeGrunnerAnnet = fritekstSærligGrunnAnnet;
            return this;
        }

        public HbSærligeGrunner.Builder medSærligeGrunner(Collection<SærligGrunn> særligeGrunner) {
            Set<SærligGrunn> grunner = new HashSet<>(særligeGrunner);
            kladd.særligGrunnAnnet = grunner.remove(SærligGrunn.ANNET);
            kladd.særligGrunnNav = grunner.remove(SærligGrunn.HELT_ELLER_DELVIS_NAVS_FEIL);
            kladd.særligGrunnStørrelse = grunner.remove(SærligGrunn.STØRRELSE_BELØP);
            kladd.særligGrunnTid = grunner.remove(SærligGrunn.TID_FRA_UTBETALING);
            grunner.remove(SærligGrunn.GRAD_AV_UAKTSOMHET);
            if (!grunner.isEmpty()) {
                throw new IllegalArgumentException("Ukjent særlig grunn: " + grunner);
            }
            return this;
        }

        public HbSærligeGrunner build() {
            return kladd;
        }
    }
}
