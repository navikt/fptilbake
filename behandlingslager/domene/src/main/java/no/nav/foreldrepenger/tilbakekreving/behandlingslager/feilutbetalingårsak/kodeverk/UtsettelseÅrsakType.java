package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "UtsettelseÅrsakType")
@DiscriminatorValue(UtsettelseÅrsakType.DISCRIMINATOR)
public class UtsettelseÅrsakType extends Kodeliste{

    public static final String DISCRIMINATOR = "UTSETTELSE_AARSAK_TYPE";

    public static final UtsettelseÅrsakType ARBEID = new UtsettelseÅrsakType("UTSETTELSE_ARBEID");

    public static final UtsettelseÅrsakType SYKDOM = new UtsettelseÅrsakType("UTSETTELSE_SKYDOM");

    public static final UtsettelseÅrsakType FERIE = new UtsettelseÅrsakType("UTSETTELSE_FERIE");

    public static final UtsettelseÅrsakType INSTITUSJON = new UtsettelseÅrsakType("UTSETTELSE_INSTITUSJON");

    public UtsettelseÅrsakType() {
        // For Hibernate
    }

    public UtsettelseÅrsakType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
