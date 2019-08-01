package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "BeregningsGrunnlagTilstand")
@DiscriminatorValue(BeregningsGrunnlagTilstand.DISCRIMINATOR)
public class BeregningsGrunnlagTilstand extends Kodeliste{

    public static final String DISCRIMINATOR = "BEREGNINGSGRUNNLAG_TILSTAND";

    public static final BeregningsGrunnlagTilstand ENDRING_BEREGNINGSGRUNNLAGET = new BeregningsGrunnlagTilstand("ENDRING_BEREGNINGSGRUNNLAGET");

    public BeregningsGrunnlagTilstand() {
        // For Hibernate
    }

    public BeregningsGrunnlagTilstand(String kode) {
        super(kode, DISCRIMINATOR);
    }
}