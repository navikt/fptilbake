package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.beregningsresultat;

import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.vedtak.VedtakResultatType;

public class BeregningResultatDto {
    private VedtakResultatType vedtakResultatType;
    private List<BeregningResultatPeriodeDto> beregningResultatPerioder;

    public BeregningResultatDto(VedtakResultatType vedtakResultatType, List<BeregningResultatPeriodeDto> beregningResultatPerioder) {
        this.vedtakResultatType = vedtakResultatType;
        this.beregningResultatPerioder = beregningResultatPerioder;
    }

    public List<BeregningResultatPeriodeDto> getBeregningResultatPerioder() {
        return beregningResultatPerioder;
    }

    public VedtakResultatType getVedtakResultatType() {
        return vedtakResultatType;
    }

}
