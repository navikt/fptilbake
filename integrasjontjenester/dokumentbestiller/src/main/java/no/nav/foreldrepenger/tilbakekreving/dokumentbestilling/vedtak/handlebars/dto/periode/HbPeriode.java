package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.LocalDateTilStrengMedNorskFormatSerialiserer;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class HbPeriode {

    @JsonProperty("fom")
    @JsonSerialize(using = LocalDateTilStrengMedNorskFormatSerialiserer.class)
    private LocalDate fom;
    @JsonProperty("tom")
    @JsonSerialize(using = LocalDateTilStrengMedNorskFormatSerialiserer.class)
    private LocalDate tom;

    private HbPeriode(LocalDate fom, LocalDate tom) {
        this.fom = fom;
        this.tom = tom;
    }

    public static HbPeriode of(Periode periode) {
        return new HbPeriode(periode.getFom(), periode.getTom());
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public Periode tilPeriode() {
        return new Periode(fom, tom);
    }
}
