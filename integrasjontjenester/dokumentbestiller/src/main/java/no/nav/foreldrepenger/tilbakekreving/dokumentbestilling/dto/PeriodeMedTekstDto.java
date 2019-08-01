package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.vedtak.util.InputValideringRegex;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

public class PeriodeMedTekstDto {

    private LocalDate fom;

    private LocalDate tom;

    @Size(max = 1500, message = "Fritekst for fakta er for lang")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String faktaAvsnitt;

    @Size(max = 1500, message = "Fritekst for vilkår er for lang")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    @JsonProperty("vilkaarAvsnitt")
    private String vilkårAvsnitt;

    @Size(max = 1500, message = "Fritekst for særlige grunner er for lang")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    @JsonProperty("saerligeGrunnerAvsnitt")
    private String særligeGrunnerAvsnitt;

    public LocalDate getFom() {
        return fom;
    }

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public String getFaktaAvsnitt() {
        return faktaAvsnitt;
    }

    public void setFaktaAvsnitt(String faktaAvsnitt) {
        this.faktaAvsnitt = faktaAvsnitt;
    }

    public String getVilkårAvsnitt() {
        return vilkårAvsnitt;
    }

    public void setVilkårAvsnitt(String vilkårAvsnitt) {
        this.vilkårAvsnitt = vilkårAvsnitt;
    }

    public String getSærligeGrunnerAvsnitt() {
        return særligeGrunnerAvsnitt;
    }

    public void setSærligeGrunnerAvsnitt(String særligeGrunnerAvsnitt) {
        this.særligeGrunnerAvsnitt = særligeGrunnerAvsnitt;
    }
}
