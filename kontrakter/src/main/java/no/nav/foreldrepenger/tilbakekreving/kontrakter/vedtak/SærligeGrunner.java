package no.nav.foreldrepenger.tilbakekreving.kontrakter.vedtak;

import java.util.List;

import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SærligeGrunner {
    @JsonProperty(value = "erSaerligeGrunnerTilReduksjon")
    private boolean erSærligeGrunnerTilReduksjon;
    @JsonProperty(value = "saerligeGrunner")
    @Size(max = 5)
    private List<SærligGrunn> særligeGrunner;

    public boolean isErSærligeGrunnerTilReduksjon() {
        return erSærligeGrunnerTilReduksjon;
    }

    public void setErSærligeGrunnerTilReduksjon(boolean erSærligeGrunnerTilReduksjon) {
        this.erSærligeGrunnerTilReduksjon = erSærligeGrunnerTilReduksjon;
    }

    public List<SærligGrunn> getSærligeGrunner() {
        return særligeGrunner;
    }

    public void setSærligeGrunner(List<SærligGrunn> særligeGrunner) {
        this.særligeGrunner = særligeGrunner;
    }
}
