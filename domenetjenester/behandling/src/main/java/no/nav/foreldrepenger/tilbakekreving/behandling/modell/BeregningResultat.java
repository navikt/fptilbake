package no.nav.foreldrepenger.tilbakekreving.behandling.modell;

import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandling.beregning.BeregningResultatPeriode;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;

public class BeregningResultat {
    private List<BeregningResultatPeriode> beregningResultatPerioder;
    private VedtakResultatType vedtakResultatType;
    // TODO hardkoded for nå. Må løse når vedtak dato er tilgjengelig
    private String behandlingKonsekvens = "Revurdering med vedtak 20.05.2018";

    public List<BeregningResultatPeriode> getBeregningResultatPerioder() {
        return beregningResultatPerioder;
    }

    public void setBeregningResultatPerioder(List<BeregningResultatPeriode> beregningResultatPerioder) {
        this.beregningResultatPerioder = beregningResultatPerioder;
    }

    public VedtakResultatType getVedtakResultatType() {
        return vedtakResultatType;
    }

    public void setVedtakResultatType(VedtakResultatType vedtakResultatType) {
        this.vedtakResultatType = vedtakResultatType;
    }

    public String getBehandlingKonsekvens() {
        return behandlingKonsekvens;
    }

    public void setBehandlingKonsekvens(String behandlingKonsekvens) {
        this.behandlingKonsekvens = behandlingKonsekvens;
    }
}
