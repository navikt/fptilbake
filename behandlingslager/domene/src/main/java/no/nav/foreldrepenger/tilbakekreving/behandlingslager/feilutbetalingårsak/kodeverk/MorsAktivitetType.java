package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "MorsAktivitetType")
@DiscriminatorValue(MorsAktivitetType.DISCRIMINATOR)
public class MorsAktivitetType extends Kodeliste{

    public static final String DISCRIMINATOR = "MORS_AKTIVITET_TYPE";

    public static final MorsAktivitetType IKKE_ARBEIDET_HELTID = new MorsAktivitetType("IKKE_ARBEIDET_HELTID");
    public static final MorsAktivitetType IKKE_STUDERT_HELTID = new MorsAktivitetType("IKKE_STUDERT_HELTID");
    public static final MorsAktivitetType IKKE_ARBEIDET_STUDERT_HELTID = new MorsAktivitetType("IKKE_ARBEIDET_STUDERT_HELTID");
    public static final MorsAktivitetType IKKE_INNLAGT = new MorsAktivitetType("IKKE_INNLAGT");
    public static final MorsAktivitetType IKKE_HELT_AVHENGIG = new MorsAktivitetType("IKKE_HELT_AVHENGIG");
    public static final MorsAktivitetType IKKE_KVALIFISERINGSPROGRAM = new MorsAktivitetType("IKKE_KVALIFISERINGSPROGRAM");
    public static final MorsAktivitetType IKKE_INTRODUKSJONSPROGRAM = new MorsAktivitetType("IKKE_INTRODUKSJONSPROGRAM");

    public MorsAktivitetType() {
        // For Hibernate
    }

    public MorsAktivitetType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}