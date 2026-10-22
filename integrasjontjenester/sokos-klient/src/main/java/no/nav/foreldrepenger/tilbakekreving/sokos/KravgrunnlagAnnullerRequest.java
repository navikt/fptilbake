package no.nav.foreldrepenger.tilbakekreving.sokos;

import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KodeAksjon;

public record KravgrunnlagAnnullerRequest(KodeAksjon kodeAksjon,
                                          Integer vedtakId,
                                          String enhetAnsvarlig,
                                          String saksbehandlerId) {
}
