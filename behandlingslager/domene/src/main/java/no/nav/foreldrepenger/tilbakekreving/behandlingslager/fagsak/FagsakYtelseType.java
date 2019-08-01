package no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak;

import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "FagsakYtelseType")
@DiscriminatorValue(FagsakYtelseType.DISCRIMINATOR)
public class FagsakYtelseType extends Kodeliste {

    public static final String DISCRIMINATOR = "FAGSAK_YTELSE"; //$NON-NLS-1$
    public static final FagsakYtelseType ENGANGSTØNAD = new FagsakYtelseType("ES"); //$NON-NLS-1$
    public static final FagsakYtelseType FORELDREPENGER = new FagsakYtelseType("FP"); //$NON-NLS-1$
    public static final FagsakYtelseType SVANGERSKAPSPENGER = new FagsakYtelseType("SVP"); //$NON-NLS-1$
    public static final FagsakYtelseType UDEFINERT = new FagsakYtelseType("-"); //$NON-NLS-1$

    private static final Map<String, FagsakYtelseType> YTELSE_TYPER = Map.of(
            ENGANGSTØNAD.getKode(), ENGANGSTØNAD,
            FORELDREPENGER.getKode(), FORELDREPENGER,
            SVANGERSKAPSPENGER.getKode(), SVANGERSKAPSPENGER
    );

    FagsakYtelseType() {
        // Hibernate
    }

    public FagsakYtelseType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public final boolean gjelderEngangsstønad() {
        return ENGANGSTØNAD.getKode().equals(this.getKode());
    }

    public final boolean gjelderForeldrepenger() {
        return FORELDREPENGER.getKode().equals(this.getKode());
    }

    public final boolean gjelderSvangerskapspenger() {
        return SVANGERSKAPSPENGER.getKode().equals(this.getKode());
    }

    public static FagsakYtelseType fraKode(String kode) {
        return YTELSE_TYPER.getOrDefault(kode, UDEFINERT);
    }

}
