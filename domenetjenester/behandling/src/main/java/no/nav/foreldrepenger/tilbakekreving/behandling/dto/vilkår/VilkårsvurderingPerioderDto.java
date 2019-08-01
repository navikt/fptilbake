package no.nav.foreldrepenger.tilbakekreving.behandling.dto.vilkår;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.ValidKodeverk;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk.VilkårResultat;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.vedtak.util.InputValideringRegex;

public class VilkårsvurderingPerioderDto {

    @NotNull
    private LocalDate fom;

    @NotNull
    private LocalDate tom;

    @NotNull
    @JsonProperty("vilkarResultat")
    @ValidKodeverk
    private VilkårResultat vilkårResultat;

    @NotNull
    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String begrunnelse;

    @NotNull
    @Valid
    private VilkårResultatInfoDto vilkarResultatInfo;

    @Min(-999999999)
    @Max(Long.MAX_VALUE)
    @Digits(integer = 9, fraction = 2)
    private BigDecimal feilutbetalingBelop;

    public LocalDate getFom() {
        return fom;
    }

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }

    public VilkårResultat getVilkårResultat() {
        return vilkårResultat;
    }

    public void setVilkårResultat(VilkårResultat vilkårResultat) {
        this.vilkårResultat = vilkårResultat;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    public VilkårResultatInfoDto getVilkarResultatInfo() {
        return vilkarResultatInfo;
    }

    public void setVilkarResultatInfo(VilkårResultatInfoDto vilkarResultatInfo) {
        this.vilkarResultatInfo = vilkarResultatInfo;
    }

    public BigDecimal getFeilutbetalingBelop() {
        return feilutbetalingBelop;
    }

    public void setFeilutbetalingBelop(BigDecimal feilutbetalingBelop) {
        this.feilutbetalingBelop = feilutbetalingBelop;
    }

    public void setPeriode(Periode periode) {
        this.fom = periode.getFom();
        this.tom = periode.getTom();
    }
}
