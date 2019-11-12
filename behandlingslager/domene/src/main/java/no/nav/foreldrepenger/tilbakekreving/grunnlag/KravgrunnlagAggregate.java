package no.nav.foreldrepenger.tilbakekreving.grunnlag;

public interface KravgrunnlagAggregate {
    Kravgrunnlag431 getGrunnlagØkonomi();

    Long getBehandlingId();

    boolean isSperret();
}
