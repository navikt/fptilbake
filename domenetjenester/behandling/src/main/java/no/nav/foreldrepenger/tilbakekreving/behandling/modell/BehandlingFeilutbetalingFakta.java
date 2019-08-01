package no.nav.foreldrepenger.tilbakekreving.behandling.modell;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BehandlingFeilutbetalingFakta {

    private String årsakRevurdering;
    private String resultatRevurdering;
    private String resultatFeilutbetaling;
    private BigDecimal tidligereVarseltBeløp;
    private BigDecimal aktuellFeilUtbetaltBeløp;
    private LocalDate datoForVarselSendt;
    private LocalDate datoForRevurderingsvedtak;
    private LocalDate totalPeriodeFom;
    private LocalDate totalPeriodeTom;
    private List<UtbetaltPeriode> perioder;

    private BehandlingFeilutbetalingFakta() {
        // bygges med builder
    }

    public String getÅrsakRevurdering() {
        return årsakRevurdering;
    }

    public String getResultatRevurdering() {
        return resultatRevurdering;
    }

    public String getResultatFeilutbetaling() {
        return resultatFeilutbetaling;
    }

    public BigDecimal getTidligereVarseltBeløp() {
        return tidligereVarseltBeløp;
    }

    public BigDecimal getAktuellFeilUtbetaltBeløp() {
        return aktuellFeilUtbetaltBeløp;
    }

    public LocalDate getDatoForVarselSendt() {
        return datoForVarselSendt;
    }

    public LocalDate getDatoForRevurderingsvedtak() {
        return datoForRevurderingsvedtak;
    }

    public LocalDate getTotalPeriodeFom() {
        return totalPeriodeFom;
    }

    public LocalDate getTotalPeriodeTom() {
        return totalPeriodeTom;
    }

    public List<UtbetaltPeriode> getPerioder() {
        return perioder;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private BehandlingFeilutbetalingFakta behandlingFeilutbetalingFakta;

        public Builder() {
            this.behandlingFeilutbetalingFakta = new BehandlingFeilutbetalingFakta();
        }

        public Builder medÅrsakRevurdering(String årsakRevurdering) {
            this.behandlingFeilutbetalingFakta.årsakRevurdering = årsakRevurdering;
            return this;
        }

        public Builder medResultatRevurdering(String resultatRevurdering) {
            this.behandlingFeilutbetalingFakta.resultatRevurdering = resultatRevurdering;
            return this;
        }

        public Builder medResultatFeilutbetaling(String resultatFeilutbetaling) {
            this.behandlingFeilutbetalingFakta.resultatFeilutbetaling = resultatFeilutbetaling;
            return this;
        }

        public Builder medTidligereVarsletBeløp(BigDecimal tidligereVarsletBeløp) {
            this.behandlingFeilutbetalingFakta.tidligereVarseltBeløp = tidligereVarsletBeløp;
            return this;
        }

        public Builder medAktuellFeilUtbetaltBeløp(BigDecimal aktuellFeilUtbetaltBeløp) {
            this.behandlingFeilutbetalingFakta.aktuellFeilUtbetaltBeløp = aktuellFeilUtbetaltBeløp;
            return this;
        }

        public Builder medDatoForVarselSendt(LocalDate datoForVarselSendt) {
            this.behandlingFeilutbetalingFakta.datoForVarselSendt = datoForVarselSendt;
            return this;
        }

        public Builder medDatoForRevurderingsvedtak(LocalDate datoForRevurderingsvedtak) {
            this.behandlingFeilutbetalingFakta.datoForRevurderingsvedtak = datoForRevurderingsvedtak;
            return this;
        }

        public Builder medTotalPeriodeFom(LocalDate totalPeriodeFom) {
            this.behandlingFeilutbetalingFakta.totalPeriodeFom = totalPeriodeFom;
            return this;
        }

        public Builder medTotalPeriodeTom(LocalDate totalPeriodeTom) {
            this.behandlingFeilutbetalingFakta.totalPeriodeTom = totalPeriodeTom;
            return this;
        }

        public Builder medPerioder(List<UtbetaltPeriode> perioder) {
            this.behandlingFeilutbetalingFakta.perioder = perioder;
            return this;
        }

        public BehandlingFeilutbetalingFakta build() {
            return behandlingFeilutbetalingFakta;
        }
    }
}
