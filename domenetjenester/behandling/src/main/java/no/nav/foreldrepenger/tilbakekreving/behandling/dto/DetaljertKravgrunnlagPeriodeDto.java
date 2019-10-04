package no.nav.foreldrepenger.tilbakekreving.behandling.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class DetaljertKravgrunnlagPeriodeDto implements AbacDto {

    private LocalDate fom;
    private LocalDate tom;

    @Min(-999999999)
    @Max(Long.MAX_VALUE)
    @Digits(integer = 12, fraction = 2)
    private BigDecimal beløpSkattMnd;

    @Size(min = 1)
    @Valid
    private List<DetaljertKravgrunnlagBelopDto> posteringer = new ArrayList<>();

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

    public BigDecimal getBeløpSkattMnd() {
        return beløpSkattMnd;
    }

    public void setBeløpSkattMnd(BigDecimal beløpSkattMnd) {
        this.beløpSkattMnd = beløpSkattMnd;
    }

    public List<DetaljertKravgrunnlagBelopDto> getPosteringer() {
        return posteringer;
    }

    public void leggTilPostering(DetaljertKravgrunnlagBelopDto postering) {
        posteringer.add(postering);
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();
    }
}
