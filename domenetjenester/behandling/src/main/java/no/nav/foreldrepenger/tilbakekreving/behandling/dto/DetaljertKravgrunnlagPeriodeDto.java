package no.nav.foreldrepenger.tilbakekreving.behandling.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class DetaljertKravgrunnlagPeriodeDto implements AbacDto {

    private LocalDate fom;
    private LocalDate tom;

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
