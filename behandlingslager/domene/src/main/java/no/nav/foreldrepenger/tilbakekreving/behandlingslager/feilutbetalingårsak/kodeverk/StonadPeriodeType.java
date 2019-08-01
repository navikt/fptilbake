package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "StonadPeriodeType")
@DiscriminatorValue(StonadPeriodeType.DISCRIMINATOR)
public class StonadPeriodeType extends Kodeliste{

    public static final String DISCRIMINATOR = "STONAD_PERIODE_TYPE";

    public static final StonadPeriodeType ENDRET_DEKNINGSGRAD = new StonadPeriodeType("ENDRET_DEKNINGSGRAD");
    public static final StonadPeriodeType FLERBARNSDAGER = new StonadPeriodeType("FLERBARNSDAGER");

    public StonadPeriodeType() {
        // For Hibernate
    }

    public StonadPeriodeType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}