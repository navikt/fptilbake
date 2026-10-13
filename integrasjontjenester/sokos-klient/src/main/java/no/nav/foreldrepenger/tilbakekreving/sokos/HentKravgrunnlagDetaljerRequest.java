package no.nav.foreldrepenger.tilbakekreving.sokos;

import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KodeAksjon;

public record HentKravgrunnlagDetaljerRequest(KodeAksjon kodeAksjon,
                                              Integer kravgrunnlagId,
                                              String enhetAnsvarlig,
                                              String saksbehandlerId) {
}
