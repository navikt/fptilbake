package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.felles.Periode;

public class Avsnitt {
    private String overskrift;
    private List<Underavsnitt> underavsnittsliste;
    private Avsnittstype avsnittstype;
    private LocalDate fom;
    private LocalDate tom;

    public enum Avsnittstype {
        OPPSUMMERING,
        PERIODE,
        TILLEGGSINFORMASJON
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public String getOverskrift() {
        return overskrift;
    }

    public List<Underavsnitt> getUnderavsnittsliste() {
        return underavsnittsliste;
    }

    public Avsnittstype getAvsnittstype() {
        return avsnittstype;
    }

    public static class Builder {
        private Avsnitt avsnitt = new Avsnitt();

        public Builder medUnderavsnittsliste(List<Underavsnitt> underavsnittsliste) {
            this.avsnitt.underavsnittsliste = underavsnittsliste;
            return this;
        }

        public Builder leggTilUnderavsnitt(Underavsnitt underavsnitt) {
            if (avsnitt.underavsnittsliste == null) {
                avsnitt.underavsnittsliste = new ArrayList<>();
            }
            avsnitt.underavsnittsliste.add(underavsnitt);
            return this;
        }

        public Builder medOverskrift(String overskrift) {
            this.avsnitt.overskrift = overskrift;
            return this;
        }

        public Builder medAvsnittstype(Avsnittstype avsnittstype) {
            this.avsnitt.avsnittstype = avsnittstype;
            return this;
        }

        public Builder medPeriode(Periode periode) {
            this.avsnitt.fom = periode.getFom();
            this.avsnitt.tom = periode.getTom();
            return this;
        }

        public Builder medFom(LocalDate fom) {
            this.avsnitt.fom = fom;
            return this;
        }

        public Builder medTom(LocalDate tom) {
            this.avsnitt.tom = tom;
            return this;
        }

        public Avsnitt build() {
            return avsnitt;
        }

        public boolean harOverskrift() {
            return avsnitt.overskrift != null;
        }
    }
}
