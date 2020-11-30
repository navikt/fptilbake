package no.nav.foreldrepenger.tilbakekreving.behandling.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;
import no.nav.vedtak.util.InputValideringRegex;

public class ForeldelsePeriodeMedBeløpDto {

    @NotNull
    private LocalDate fom;

    @NotNull
    private LocalDate tom;

    @DecimalMin("-999999999.00")
    @DecimalMax("999999999.99")
    @Digits(integer = 9, fraction = 2)
    private BigDecimal belop;

    @Valid
    private ForeldelseVurderingType foreldelseVurderingType;

    private LocalDate foreldelsesfrist;
    private LocalDate oppdagelsesDato;

    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String begrunnelse;

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public Periode getPeriode() {
        return Periode.of(fom, tom);
    }

    public BigDecimal getBelop() {
        return belop;
    }

    public ForeldelseVurderingType getForeldelseVurderingType() {
        return foreldelseVurderingType;
    }

    public LocalDate getForeldelsesfrist() {
        return foreldelsesfrist;
    }

    public LocalDate getOppdagelsesDato() {
        return oppdagelsesDato;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }

    public void setPeriode(Periode periode) {
        this.fom = periode.getFom();
        this.tom = periode.getTom();
    }

    public Periode tilPeriode() {
        return Periode.of(fom, tom);
    }

    public void setBelop(BigDecimal belop) {
        this.belop = belop;
    }

    public void setForeldelseVurderingType(ForeldelseVurderingType foreldelseVurderingType) {
        this.foreldelseVurderingType = foreldelseVurderingType;
    }

    public void setForeldelsesfrist(LocalDate foreldelsesfrist) {
        this.foreldelsesfrist = foreldelsesfrist;
    }

    public void setOppdagelsesDato(LocalDate oppdagelsesDato) {
        this.oppdagelsesDato = oppdagelsesDato;
    }

    public void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

}
