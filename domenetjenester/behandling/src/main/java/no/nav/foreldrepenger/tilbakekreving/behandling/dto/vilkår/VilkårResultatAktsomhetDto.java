package no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.ValidKodeverk;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.SærligGrunn;
import no.nav.vedtak.util.InputValideringRegex;

public class VilkårResultatAktsomhetDto {

    @Size(max = 5)
    @JsonProperty("sarligGrunner")
    @Valid
    private List<@ValidKodeverk SærligGrunn> særligeGrunner = new ArrayList<>();

    private boolean harGrunnerTilReduksjon;

    @DecimalMin("0.01")
    @DecimalMax("99.99")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal andelTilbakekreves;

    private Boolean ileggRenter;

    @Min(-999999999)
    @Max(Long.MAX_VALUE)
    @DecimalMin("-999999999.00")
    @DecimalMax("999999999.99")
    @Digits(integer = 9, fraction = 2)
    private BigDecimal tilbakekrevesBelop;

    private Boolean tilbakekrevSelvOmBeloepErUnder4Rettsgebyr;

    @Size(max = 1500)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String annetBegrunnelse;

    @Size(max = 1500)
    @JsonProperty("sarligGrunnerBegrunnelse")
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String særligGrunnerBegrunnelse;

    public List<SærligGrunn> getSærligeGrunner() {
        return særligeGrunner;
    }

    public void setSærligeGrunner(List<SærligGrunn> særligeGrunner) {
        this.særligeGrunner = særligeGrunner;
    }

    public boolean isHarGrunnerTilReduksjon() {
        return harGrunnerTilReduksjon;
    }

    public void setHarGrunnerTilReduksjon(boolean harGrunnerTilReduksjon) {
        this.harGrunnerTilReduksjon = harGrunnerTilReduksjon;
    }

    public BigDecimal getAndelTilbakekreves() {
        return andelTilbakekreves;
    }

    public void setAndelTilbakekreves(BigDecimal andelTilbakekreves) {
        this.andelTilbakekreves = andelTilbakekreves;
    }

    public Boolean isIleggRenter() {
        return ileggRenter;
    }

    public void setIleggRenter(Boolean ileggRenter) {
        this.ileggRenter = ileggRenter;
    }

    public BigDecimal getTilbakekrevesBelop() {
        return tilbakekrevesBelop;
    }

    public void setTilbakekrevesBelop(BigDecimal tilbakekrevesBelop) {
        this.tilbakekrevesBelop = tilbakekrevesBelop;
    }

    public Boolean isTilbakekrevSelvOmBeloepErUnder4Rettsgebyr() {
        return tilbakekrevSelvOmBeloepErUnder4Rettsgebyr;
    }

    public void setTilbakekrevSelvOmBeloepErUnder4Rettsgebyr(Boolean tilbakekrevSelvOmBeloepErUnder4Rettsgebyr) {
        this.tilbakekrevSelvOmBeloepErUnder4Rettsgebyr = tilbakekrevSelvOmBeloepErUnder4Rettsgebyr;
    }

    public String getAnnetBegrunnelse() {
        return annetBegrunnelse;
    }

    public void setAnnetBegrunnelse(String annetBegrunnelse) {
        this.annetBegrunnelse = annetBegrunnelse;
    }

    public String getSærligGrunnerBegrunnelse() {
        return særligGrunnerBegrunnelse;
    }

    public void setSærligGrunnerBegrunnelse(String særligGrunnerBegrunnelse) {
        this.særligGrunnerBegrunnelse = særligGrunnerBegrunnelse;
    }
}
