package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "UtsettelseArbeid")
@DiscriminatorValue(UtsettelseArbeid.DISCRIMINATOR)
public class UtsettelseArbeid extends Kodeliste{

    public static final String DISCRIMINATOR = "UTSETTELSE_ARBEID";

    public static final UtsettelseArbeid UTSETTELSE_ARBEID_HELTID = new UtsettelseArbeid("UTSETTELSE_ARBEID_HELTID");
    public static final UtsettelseArbeid UTSETTELSE_ARBEID_DELTID = new UtsettelseArbeid("UTSETTELSE_ARBEID_DELTID");

    public UtsettelseArbeid() {
        // For Hibernate
    }

    public UtsettelseArbeid(String kode) {
        super(kode, DISCRIMINATOR);
    }
}