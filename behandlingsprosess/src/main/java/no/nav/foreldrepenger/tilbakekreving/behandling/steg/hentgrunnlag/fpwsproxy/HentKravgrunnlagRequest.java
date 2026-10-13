package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag.fpwsproxy;

import java.math.BigInteger;
import java.util.Objects;

import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KodeAksjon;


public record HentKravgrunnlagRequest(KodeAksjon kodeAksjon,
                                      BigInteger kravgrunnlagId, // Endre til integer etter omlegging
                                      String saksbehId,
                                      String enhetAnsvarlig) {

    public HentKravgrunnlagRequest {
        Objects.requireNonNull(kodeAksjon, "kodeAksjon");
        Objects.requireNonNull(kravgrunnlagId, "kravgrunnlagId");
        Objects.requireNonNull(saksbehId, "saksbehId");
        Objects.requireNonNull(enhetAnsvarlig, "enhetAnsvarlig");
    }

    public static class Builder {
        private KodeAksjon kodeAksjon;
        private BigInteger kravgrunnlagId;
        private String saksbehId;
        private String enhetAnsvarlig;

        public Builder kodeAksjon(KodeAksjon kodeAksjon) {
            this.kodeAksjon = kodeAksjon;
            return this;
        }

        public Builder kravgrunnlagId(BigInteger kravgrunnlagId) {
            this.kravgrunnlagId = kravgrunnlagId;
            return this;
        }

        public Builder saksbehId(String saksbehId) {
            this.saksbehId = saksbehId;
            return this;
        }

        public Builder enhetAnsvarlig(String enhetAnsvarlig) {
            this.enhetAnsvarlig = enhetAnsvarlig;
            return this;
        }

        public HentKravgrunnlagRequest build() throws IllegalStateException {
            return new HentKravgrunnlagRequest(kodeAksjon, kravgrunnlagId, saksbehId, enhetAnsvarlig);
        }
    }
}
