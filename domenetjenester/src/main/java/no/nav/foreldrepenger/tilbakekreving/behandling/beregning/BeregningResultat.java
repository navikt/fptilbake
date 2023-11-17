package no.nav.foreldrepenger.tilbakekreving.behandling.beregning;

import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;

public class BeregningResultat {
    private VedtakResultatType vedtakResultatType;
    private List<BeregningResultatPeriode> beregningResultatPerioder;
    public BeregningResultat(VedtakResultatType vedtakResultatType, List<BeregningResultatPeriode> beregningResultatPerioder) {
        this.vedtakResultatType = vedtakResultatType;
        this.beregningResultatPerioder = beregningResultatPerioder;
    }

    public List<BeregningResultatPeriode> getBeregningResultatPerioder() {
        return beregningResultatPerioder;
    }

    public VedtakResultatType getVedtakResultatType() {
        return vedtakResultatType;
    }

    @Override
    public String toString() {
        return "BeregningResultat{" +
            "vedtakResultatType=" + vedtakResultatType +
            ", beregningResultatPerioder=" + beregningResultatPerioder +
            '}';
    }
}
