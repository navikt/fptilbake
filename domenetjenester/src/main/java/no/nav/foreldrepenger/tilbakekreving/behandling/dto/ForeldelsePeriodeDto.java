package no.nav.foreldrepenger.tilbakekreving.behandling.dto;

import java.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ForeldelseVurderingType;
import no.nav.vedtak.util.InputValideringRegex;

public class ForeldelsePeriodeDto {

    private LocalDate fraDato;
    private LocalDate tilDato;
    @Valid
    private ForeldelseVurderingType foreldelseVurderingType;

    @NotNull
    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String begrunnelse;

    public ForeldelsePeriodeDto() {
        // trengs for deserialisering av JSON
    }

    public ForeldelsePeriodeDto(LocalDate fraDato, LocalDate tilDato, ForeldelseVurderingType foreldelseVurderingType, String begrunnelse) {
        this.fraDato = fraDato;
        this.tilDato = tilDato;
        this.foreldelseVurderingType = foreldelseVurderingType;
        this.begrunnelse = begrunnelse;
    }

    public LocalDate getFraDato() {
        return fraDato;
    }

    public void setFraDato(LocalDate fraDato) {
        this.fraDato = fraDato;
    }

    public LocalDate getTilDato() {
        return tilDato;
    }

    public void setTilDato(LocalDate tilDato) {
        this.tilDato = tilDato;
    }

    public ForeldelseVurderingType getForeldelseVurderingType() {
        return foreldelseVurderingType;
    }

    public void setForeldelseVurderingType(ForeldelseVurderingType foreldelseVurderingType) {
        this.foreldelseVurderingType = foreldelseVurderingType;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public void setBegrunnelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }
}
