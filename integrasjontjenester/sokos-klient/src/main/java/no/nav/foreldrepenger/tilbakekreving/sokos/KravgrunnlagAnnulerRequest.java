package no.nav.foreldrepenger.tilbakekreving.sokos;

import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KodeAksjon;

public record KravgrunnlagAnnulerRequest(KodeAksjon kodeAksjon,
                                         Integer vedtakId,
                                         String enhetAnsvarlig,
                                         String saksbehandlerId) {
}
