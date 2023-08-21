package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.vedtak.util.InputValideringRegex;

public class PeriodeMedTekstDto {

    @NotNull
    private LocalDate fom;

    @NotNull
    private LocalDate tom;

    @Size(max = 4000, message = "Fritekst for fakta er for lang")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String faktaAvsnitt;

    @Size(max = 4000, message = "Fritekst for foreldelse er for lang")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    @JsonProperty("foreldelseAvsnitt")
    private String foreldelseAvsnitt;

    @Size(max = 4000, message = "Fritekst for vilkår er for lang")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    @JsonProperty("vilkaarAvsnitt")
    private String vilkårAvsnitt;

    @Size(max = 4000, message = "Fritekst for særlige grunner er for lang")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    @JsonProperty("saerligeGrunnerAvsnitt")
    private String særligeGrunnerAvsnitt;

    @Size(max = 4000, message = "Fritekst for særlige grunner annet er for lang")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    @JsonProperty("saerligeGrunnerAnnetAvsnitt")
    private String særligeGrunnerAnnetAvsnitt;

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

    public Periode getPeriode() {
        return Periode.of(fom, tom);
    }

    public String getFaktaAvsnitt() {
        return faktaAvsnitt;
    }

    public void setFaktaAvsnitt(String faktaAvsnitt) {
        this.faktaAvsnitt = faktaAvsnitt;
    }

    public String getForeldelseAvsnitt() {
        return foreldelseAvsnitt;
    }

    public void setForeldelseAvsnitt(String foreldelseAvsnitt) {
        this.foreldelseAvsnitt = foreldelseAvsnitt;
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

    public String getSærligeGrunnerAnnetAvsnitt() {
        return særligeGrunnerAnnetAvsnitt;
    }

    public void setSærligeGrunnerAnnetAvsnitt(String særligeGrunnerAnnetAvsnitt) {
        this.særligeGrunnerAnnetAvsnitt = særligeGrunnerAnnetAvsnitt;
    }
}
