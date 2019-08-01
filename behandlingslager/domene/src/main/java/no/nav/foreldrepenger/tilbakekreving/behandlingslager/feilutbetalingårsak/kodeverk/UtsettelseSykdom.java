package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "UtsettelseSykdom")
@DiscriminatorValue(UtsettelseSykdom.DISCRIMINATOR)
public class UtsettelseSykdom extends Kodeliste{

    public static final String DISCRIMINATOR = "UTSETTELSE_SYKDOM";

    public static final UtsettelseSykdom STONADSMOTTAKER_BEHOV = new UtsettelseSykdom("STONADSMOTTAKER_BEHOV");

    public UtsettelseSykdom() {
        // For Hibernate
    }

    public UtsettelseSykdom(String kode) {
        super(kode, DISCRIMINATOR);
    }
}