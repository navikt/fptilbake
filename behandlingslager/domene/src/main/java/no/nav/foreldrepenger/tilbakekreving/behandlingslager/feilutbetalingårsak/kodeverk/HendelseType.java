package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "HendelseType")
@DiscriminatorValue(HendelseType.DISCRIMINATOR)
public class HendelseType extends Kodeliste {

    public static final String DISCRIMINATOR = "HENDELSE_TYPE";

    public static final HendelseType MEDLEMSKAP_TYPE = new HendelseType("MEDLEMSKAP");
    public static final HendelseType ØKONOMI_FEIL = new HendelseType("OKONOMI_FEIL");
    public static final HendelseType FP_OPPTJENING_TYPE = new HendelseType("OPPTJENING_TYPE");
    public static final HendelseType FP_BEREGNING_TYPE = new HendelseType("BEREGNING_TYPE");
    public static final HendelseType FP_STONADSPERIODEN_TYPE = new HendelseType("STONADSPERIODEN_TYPE");
    public static final HendelseType FP_UTTAK_GENERELT_TYPE = new HendelseType("UTTAK_GENERELT_TYPE");
    public static final HendelseType FP_UTTAK_UTSETTELSE_TYPE = new HendelseType("UTTAK_UTSETTELSE_TYPE");
    public static final HendelseType FP_UTTAK_KVOTENE_TYPE = new HendelseType("UTTAK_KVOTENE_TYPE");
    public static final HendelseType FP_VILKAAR_GENERELLE_TYPE = new HendelseType("VILKAAR_GENERELLE_TYPE");
    public static final HendelseType FP_KUN_RETT_TYPE = new HendelseType("KUN_RETT_TYPE");
    public static final HendelseType FP_UTTAK_ALENEOMSORG_TYPE = new HendelseType("UTTAK_ALENEOMSORG_TYPE");
    public static final HendelseType FP_UTTAK_GRADERT_TYPE = new HendelseType("UTTAK_GRADERT_TYPE");
    public static final HendelseType FP_ANNET_HENDELSE_TYPE = new HendelseType("FP_ANNET_HENDELSE_TYPE");

    public static final HendelseType ES_MEDLEMSKAP_TYPE = new HendelseType("ES_MEDLEMSKAP_TYPE");
    public static final HendelseType ES_FODSELSVILKAARET_TYPE = new HendelseType("ES_FODSELSVILKAARET_TYPE");
    public static final HendelseType ES_ADOPSJONSVILKAARET_TYPE = new HendelseType("ES_ADOPSJONSVILKAARET_TYPE");
    public static final HendelseType ES_FORELDREANSVAR_TYPE = new HendelseType("ES_FORELDREANSVAR_TYPE");
    public static final HendelseType ES_OMSORGSVILKAAR_TYPE = new HendelseType("ES_OMSORGSVILKAAR_TYPE");
    public static final HendelseType ES_FORELDREANSVAR_FAR_TYPE = new HendelseType("ES_FORELDREANSVAR_FAR_TYPE");
    public static final HendelseType ES_RETT_PAA_FORELDREPENGER_TYPE = new HendelseType("ES_RETT_PAA_FORELDREPENGER_TYPE");
    public static final HendelseType ES_FEIL_UTBETALING_TYPE = new HendelseType("ES_FEIL_UTBETALING_TYPE");
    public static final HendelseType ES_ANNET_TYPE = new HendelseType("ES_ANNET_TYPE");

    public static final HendelseType SVP_FAKTA_TYPE = new HendelseType("SVP_FAKTA_TYPE");
    public static final HendelseType SVP_ARBEIDSGIVERS_FORHOLD_TYPE = new HendelseType("SVP_ARBEIDSGIVERS_FORHOLD_TYPE");
    public static final HendelseType SVP_OPPTJENING_TYPE = new HendelseType("SVP_OPPTJENING_TYPE");
    public static final HendelseType SVP_BEREGNING_TYPE = new HendelseType("SVP_BEREGNING_TYPE");
    public static final HendelseType SVP_UTTAK_TYPE = new HendelseType("SVP_UTTAK_TYPE");
    public static final HendelseType SVP_OPPHØR = new HendelseType("OPPHØR");
    public static final HendelseType SVP_ANNET_TYPE = new HendelseType("SVP_ANNET_TYPE");

    public HendelseType() {
        // For Hibernate
    }

    public HendelseType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
