package no.nav.foreldrepenger.tilbakekreving.sokos;

public record KravgrunnlagAnnullerResponse(Integer status,
                                           String melding,
                                           Integer vedtakId,
                                           String saksbehandlerId) {
}
