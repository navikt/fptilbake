package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "AktivitetKrav")
@DiscriminatorValue(AktivitetKrav.DISCRIMINATOR)
public class AktivitetKrav extends Kodeliste{

    public static final String DISCRIMINATOR = "MORS_AKTIVITET_KRAV";

    public static final AktivitetKrav MORS_AKTIVITET_TYPE = new AktivitetKrav("MORS_AKTIVITET_TYPE");

    public AktivitetKrav() {
        // For Hibernate
    }

    public AktivitetKrav(String kode) {
        super(kode, DISCRIMINATOR);
    }
}