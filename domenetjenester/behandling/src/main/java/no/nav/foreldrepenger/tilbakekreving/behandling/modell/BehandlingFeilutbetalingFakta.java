package no.nav.foreldrepenger.tilbakekreving.behandling.modell;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.BehandlingsresultatDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.BehandlingÅrsakDto;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto.TilbakekrevingValgDto;

public class BehandlingFeilutbetalingFakta {

    private BigDecimal tidligereVarseltBeløp;
    private BigDecimal aktuellFeilUtbetaltBeløp;
    private LocalDate datoForRevurderingsvedtak;
    private LocalDate totalPeriodeFom;
    private LocalDate totalPeriodeTom;
    private List<UtbetaltPeriode> perioder;
    private BehandlingsresultatDto behandlingsresultat;
    private List<BehandlingÅrsakDto> behandlingÅrsaker;
    private TilbakekrevingValgDto tilbakekrevingValg;
    private String begrunnelse;

    private BehandlingFeilutbetalingFakta() {
        // bygges med builder
    }

    public BigDecimal getTidligereVarseltBeløp() {
        return tidligereVarseltBeløp;
    }

    public BigDecimal getAktuellFeilUtbetaltBeløp() {
        return aktuellFeilUtbetaltBeløp;
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

    public BehandlingsresultatDto getBehandlingsresultat() {
        return behandlingsresultat;
    }

    public List<BehandlingÅrsakDto> getBehandlingÅrsaker() {
        return behandlingÅrsaker;
    }

    public TilbakekrevingValgDto getTilbakekrevingValg() {
        return tilbakekrevingValg;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private BehandlingFeilutbetalingFakta behandlingFeilutbetalingFakta;

        public Builder() {
            this.behandlingFeilutbetalingFakta = new BehandlingFeilutbetalingFakta();
        }

        public Builder medTidligereVarsletBeløp(BigDecimal tidligereVarsletBeløp) {
            this.behandlingFeilutbetalingFakta.tidligereVarseltBeløp = tidligereVarsletBeløp;
            return this;
        }

        public Builder medAktuellFeilUtbetaltBeløp(BigDecimal aktuellFeilUtbetaltBeløp) {
            this.behandlingFeilutbetalingFakta.aktuellFeilUtbetaltBeløp = aktuellFeilUtbetaltBeløp;
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

        public Builder medBehandlingsResultat(BehandlingsresultatDto behandlingsresultat) {
            this.behandlingFeilutbetalingFakta.behandlingsresultat = behandlingsresultat;
            return this;
        }

        public Builder medBehandlingÅrsaker(List<BehandlingÅrsakDto> behandlingÅrsaker) {
            this.behandlingFeilutbetalingFakta.behandlingÅrsaker = behandlingÅrsaker;
            return this;
        }

        public Builder medTilbakekrevingValg(TilbakekrevingValgDto tilbakekrevingValg) {
            this.behandlingFeilutbetalingFakta.tilbakekrevingValg = tilbakekrevingValg;
            return this;
        }

        public Builder medBegrunnelse(String begrunnelse) {
            this.behandlingFeilutbetalingFakta.begrunnelse = begrunnelse;
            return this;
        }

        public BehandlingFeilutbetalingFakta build() {
            return behandlingFeilutbetalingFakta;
        }
    }
}
