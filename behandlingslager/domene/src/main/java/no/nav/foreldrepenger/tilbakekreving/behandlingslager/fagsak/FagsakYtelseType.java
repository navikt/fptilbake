package no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak;

import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

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

    public static FagsakYtelseType fraKode(String kode) {
        if (YTELSE_TYPER.containsKey(kode)) {
            return YTELSE_TYPER.get(kode);
        }
        throw FagsakYtelseTypeFeil.FEILFACTORY.ugyldigFagsakYtelseType(kode).toException();
    }

    interface FagsakYtelseTypeFeil extends DeklarerteFeil {

        FagsakYtelseType.FagsakYtelseTypeFeil FEILFACTORY = FeilFactory.create(FagsakYtelseType.FagsakYtelseTypeFeil.class);

        @TekniskFeil(feilkode = "FPT-312906", feilmelding = "FagsakYtelseType '%s' er ugyldig", logLevel = LogLevel.WARN)
        Feil ugyldigFagsakYtelseType(String fagsakYtelseType);
    }

}
