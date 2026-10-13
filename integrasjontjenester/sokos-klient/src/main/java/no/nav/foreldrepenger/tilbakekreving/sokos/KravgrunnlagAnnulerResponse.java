package no.nav.foreldrepenger.tilbakekreving.sokos;

public record KravgrunnlagAnnulerResponse(Integer status,
                                          String melding,
                                          Integer vedtakId,
                                          String saksbehandlerId) {
}
