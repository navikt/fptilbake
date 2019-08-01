package no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår;

import static no.nav.vedtak.util.StringUtils.nullOrEmpty;
import static org.apache.cxf.common.util.CollectionUtils.isEmpty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
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

    @Size(min = 0,max = 5)
    @JsonProperty("sarligGrunner")
    @Valid
    private List<@ValidKodeverk SærligGrunn> særligeGrunner = new ArrayList<>();

    private boolean harGrunnerTilReduksjon;

    @Min(0)
    @Max(100)
    private Integer andelTilbakekreves;

    private Boolean ileggRenter;

    @Min(-999999999)
    @Max(Long.MAX_VALUE)
    @DecimalMin("-999999999.00")
    @DecimalMax("999999999.99")
    @Digits(integer = 9, fraction = 2)
    private BigDecimal tilbakekrevesBelop;

    private Boolean tilbakekrevSelvOmBeloepErUnder4Rettsgebyr;

    @Size(min=0)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String annetBegrunnelse;

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

    public Integer getAndelTilbakekreves() {
        return andelTilbakekreves;
    }

    public void setAndelTilbakekreves(Integer andelTilbakekreves) {
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

    @AssertTrue(message = "tilbakekreves beløp er ikke ordentlig")
    private boolean isTilbakekrevesBeløp() {
        return !(tilbakekrevesBelop != null && andelTilbakekreves != null);
    }

    @AssertTrue(message = "annetBegrunnelse er ikke ordentlig")
    private boolean isAnnetBegrunnelse() {
        return (nullOrEmpty(this.annetBegrunnelse) || isSærligGrunnerInnholdetAnnet()) &&
                (!nullOrEmpty(this.annetBegrunnelse) || !isSærligGrunnerInnholdetAnnet());
    }

    private boolean isNullOrFalse(Boolean verdi) {
        return verdi == null || !verdi;
    }

    private boolean isSærligGrunnerInnholdetAnnet() {
        return !isEmpty(this.særligeGrunner) && this.getSærligeGrunner().contains(SærligGrunn.ANNET);
    }

}
