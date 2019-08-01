package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "MedlemskapVilkår")
@DiscriminatorValue(MedlemskapVilkår.DISCRIMINATOR)
public class MedlemskapVilkår extends Kodeliste{

    public static final String DISCRIMINATOR = "MEDLEMSKAP_VILKAAR";

    public static final MedlemskapVilkår UTVANDRET_FODSEL = new MedlemskapVilkår("UTVANDRET_FODSEL");
    public static final MedlemskapVilkår MEDLEM_ANNET_FODSEL = new MedlemskapVilkår("MEDLEM_ANNET_FODSEL");
    public static final MedlemskapVilkår IKKE_OPPHOLDSTILLATELSE_FODSEL = new MedlemskapVilkår("IKKE_OPPHOLDSTILLATELSE_FODSEL");
    public static final MedlemskapVilkår IKKE_JOBB_FAMILIE_FODSEL = new MedlemskapVilkår("IKKE_JOBB_FAMILIE_FODSEL");
    public static final MedlemskapVilkår MER_OPPHOLD_UTLANDET_FODSEL = new MedlemskapVilkår("MER_OPPHOLD_UTLANDET_FODSEL");
    public static final MedlemskapVilkår UTVANDRET_ADOPSJON = new MedlemskapVilkår("UTVANDRET_ADOPSJON");
    public static final MedlemskapVilkår MEDLEM_ANNET_ADOPSJON = new MedlemskapVilkår("MEDLEM_ANNET_ADOPSJON");
    public static final MedlemskapVilkår IKKE_OPPHOLDSTILLATELSE_ADOPSJON = new MedlemskapVilkår("IKKE_OPPHOLDSTILLATELSE_ADOPSJON");
    public static final MedlemskapVilkår IKKE_JOBB_FAMILIE_ADOPSJON = new MedlemskapVilkår("IKKE_JOBB_FAMILIE_ADOPSJON");
    public static final MedlemskapVilkår MER_OPPHOLD_UTLANDET_ADOPSJON = new MedlemskapVilkår("MER_OPPHOLD_UTLANDET_ADOPSJON");

    public MedlemskapVilkår() {
        // For Hibernate
    }

    public MedlemskapVilkår(String kode) {
        super(kode, DISCRIMINATOR);
    }
}