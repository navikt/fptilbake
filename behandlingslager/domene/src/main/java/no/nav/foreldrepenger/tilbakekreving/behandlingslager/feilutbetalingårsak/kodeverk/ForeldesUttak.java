package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "ForeldesUttak")
@DiscriminatorValue(ForeldesUttak.DISCRIMINATOR)
public class ForeldesUttak extends Kodeliste{

    public static final String DISCRIMINATOR = "FORELDES_UTTAK";

    public static final ForeldesUttak FORELDES_SAMTIDIGE_UTTAK = new ForeldesUttak("FORELDES_SAMTIDIGE_UTTAK");


    public ForeldesUttak() {
        // For Hibernate
    }

    public ForeldesUttak(String kode) {
        super(kode, DISCRIMINATOR);
    }
}