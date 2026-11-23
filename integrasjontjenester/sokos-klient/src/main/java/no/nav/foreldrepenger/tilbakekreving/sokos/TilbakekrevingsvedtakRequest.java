package no.nav.foreldrepenger.tilbakekreving.sokos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KodeAksjon;

public record TilbakekrevingsvedtakRequest(KodeAksjon kodeAksjon,
                                           Integer vedtakId,
                                           LocalDate vedtaksDato,
                                           String kodeHjemmel,
                                           boolean renterBeregnes,
                                           String enhetAnsvarlig,
                                           String kontrollfelt,
                                           String saksbehandlerId,
                                           LocalDate datoTilleggsfrist,
                                           List<Periode> perioder) {

    public record Periode(LocalDate periodeFom,
                          LocalDate periodeTom,
                          boolean renterPeriodeBeregnes,
                          BigDecimal belopRenter,
                          List<Postering> posteringer) {
    }

    public record Postering(String kodeKlasse,
                            BigDecimal belopOpprinneligUtbetalt,
                            BigDecimal belopNy,
                            BigDecimal belopTilbakekreves,
                            BigDecimal belopUinnkrevd,
                            BigDecimal belopSkatt,
                            String kodeResultat,
                            String kodeAarsak,
                            String kodeSkyld) {
    }
}
