package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak.handlebars.dto.periode;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.LocalDateTilKortNorskFormatSerialiserer;
import no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.handlebars.LocalDateTilLangtNorskFormatSerialiserer;
import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class HbPeriode {

    @JsonProperty("fom")
    @JsonSerialize(using = LocalDateTilLangtNorskFormatSerialiserer.class)
    private LocalDate fom;
    @JsonProperty("tom")
    @JsonSerialize(using = LocalDateTilLangtNorskFormatSerialiserer.class)
    private LocalDate tom;

    @JsonProperty("fom-kompakt")
    @JsonSerialize(using = LocalDateTilKortNorskFormatSerialiserer.class)
    private LocalDate fomKompakt;
    @JsonProperty("tom-kompakt")
    @JsonSerialize(using = LocalDateTilKortNorskFormatSerialiserer.class)
    private LocalDate tomKompakt;

    private HbPeriode(LocalDate fom, LocalDate tom) {
        this.fom = fom;
        this.tom = tom;
        this.fomKompakt = fom;
        this.tomKompakt = tom;
    }

    public static HbPeriode of(Periode periode) {
        return new HbPeriode(periode.getFom(), periode.getTom());
    }

    public static HbPeriode of(LocalDate fom, LocalDate tom) {
        return new HbPeriode(fom, tom);
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
