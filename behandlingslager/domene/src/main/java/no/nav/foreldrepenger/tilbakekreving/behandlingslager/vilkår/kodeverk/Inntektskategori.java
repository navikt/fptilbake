package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "Inntektskategori")
@DiscriminatorValue(Inntektskategori.DISCRIMINATOR)
public class Inntektskategori extends Kodeliste {

    public static final String DISCRIMINATOR = "INNTEKTS_KATEGORI";

    public static final Inntektskategori FØDSEL_ES = new Inntektskategori("FØDSEL"); //$NON-NLS-1$
    public static final Inntektskategori ADOPSJON_ES = new Inntektskategori("ADOPSJON"); //$NON-NLS-1$
    public static final Inntektskategori ARBEIDSTAKER = new Inntektskategori("ARBEIDSTAKER"); //$NON-NLS-1$
    public static final Inntektskategori FRILANSER = new Inntektskategori("FRILANSER"); //$NON-NLS-1$
    public static final Inntektskategori SELVSTENDIG_NÆRINGSDRIVENDE = new Inntektskategori("SELVSTENDIG_NÆRINGSDRIVENDE"); //$NON-NLS-1$
    public static final Inntektskategori ARBEIDSLEDIG = new Inntektskategori("ARBEIDSLEDIG"); //$NON-NLS-1$
    public static final Inntektskategori SJØMANN = new Inntektskategori("SJØMANN"); //$NON-NLS-1$
    public static final Inntektskategori DAGMAMMA = new Inntektskategori("DAGMAMMA"); //$NON-NLS-1$
    public static final Inntektskategori JORDBRUKER = new Inntektskategori("JORDBRUKER"); //$NON-NLS-1$
    public static final Inntektskategori FISKER = new Inntektskategori("FISKER"); //$NON-NLS-1$
    public static final Inntektskategori FERIEPENGER_ARBEIDSTAKER = new Inntektskategori("FERIEPENGER_ARBEIDSTAKER"); //$NON-NLS-1$

    public static final Inntektskategori UDEFINERT = new Inntektskategori("-"); //$NON-NLS-1$

    Inntektskategori() {
        // Hibernate trenger en
    }

    private Inntektskategori(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
