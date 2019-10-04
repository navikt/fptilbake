package no.nav.foreldrepenger.tilbakekreving.behandling.dto;

import java.math.BigDecimal;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

public class DetaljertKravgrunnlagBelopDto implements AbacDto {

    @NotNull
    @Size(max = 20)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String klasseKode;

    @NotNull
    @Size(max = 4)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String klasseType;

    @Min(-999999999)
    @Max(Long.MAX_VALUE)
    @Digits(integer = 9, fraction = 2)
    private BigDecimal opprUtbetBelop;

    @NotNull
    @Min(-999999999)
    @Max(Long.MAX_VALUE)
    @Digits(integer = 9, fraction = 2)
    private BigDecimal nyBelop;

    @Min(-999999999)
    @Max(Long.MAX_VALUE)
    @Digits(integer = 9, fraction = 2)
    private BigDecimal tilbakekrevesBelop;

    @Min(-999999999)
    @Max(Long.MAX_VALUE)
    @Digits(integer = 9, fraction = 2)
    private BigDecimal uinnkrevdBelop;

    @Min(-999999999)
    @Max(Long.MAX_VALUE)
    @Digits(integer = 7, fraction = 4)
    private BigDecimal skattProsent;

    @Size(max = 20)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String resultatKode;

    @Size(max = 20)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String årsakKode;

    @Size(max = 20)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String skyldKode;

    public String getKlasseKode() {
        return klasseKode;
    }

    public void setKlasseKode(String klasseKode) {
        this.klasseKode = klasseKode;
    }

    public String getKlasseType() {
        return klasseType;
    }

    public void setKlasseType(String klasseType) {
        this.klasseType = klasseType;
    }

    public BigDecimal getOpprUtbetBelop() {
        return opprUtbetBelop;
    }

    public void setOpprUtbetBelop(BigDecimal opprUtbetBelop) {
        this.opprUtbetBelop = opprUtbetBelop;
    }

    public BigDecimal getNyBelop() {
        return nyBelop;
    }

    public void setNyBelop(BigDecimal nyBelop) {
        this.nyBelop = nyBelop;
    }

    public BigDecimal getTilbakekrevesBelop() {
        return tilbakekrevesBelop;
    }

    public void setTilbakekrevesBelop(BigDecimal tilbakekrevesBelop) {
        this.tilbakekrevesBelop = tilbakekrevesBelop;
    }

    public BigDecimal getUinnkrevdBelop() {
        return uinnkrevdBelop;
    }

    public void setUinnkrevdBelop(BigDecimal uinnkrevdBelop) {
        this.uinnkrevdBelop = uinnkrevdBelop;
    }

    public BigDecimal getSkattProsent() {
        return skattProsent;
    }

    public void setSkattProsent(BigDecimal skattProsent) {
        this.skattProsent = skattProsent;
    }

    public String getResultatKode() {
        return resultatKode;
    }

    public void setResultatKode(String resultatKode) {
        this.resultatKode = resultatKode;
    }

    public String getÅrsakKode() {
        return årsakKode;
    }

    public void setÅrsakKode(String årsakKode) {
        this.årsakKode = årsakKode;
    }

    public String getSkyldKode() {
        return skyldKode;
    }

    public void setSkyldKode(String skyldKode) {
        this.skyldKode = skyldKode;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();
    }
}
