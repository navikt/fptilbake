package no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;

//TODO: gå over disse og slette noen??
@Entity(name = "HistorikkEndretFeltType")
@DiscriminatorValue(HistorikkEndretFeltType.DISCRIMINATOR)
public class HistorikkEndretFeltType extends Kodeliste {
    public static final String DISCRIMINATOR = "HISTORIKK_ENDRET_FELT_TYPE"; //$NON-NLS-1$

    public static final HistorikkEndretFeltType UDEFINIERT = new HistorikkEndretFeltType("-");

    public static final HistorikkEndretFeltType ADOPSJONSVILKARET = new HistorikkEndretFeltType("ADOPSJONSVILKARET");
    public static final HistorikkEndretFeltType ADOPTERER_ALENE = new HistorikkEndretFeltType("ADOPTERER_ALENE");
    public static final HistorikkEndretFeltType AKTIVITET = new HistorikkEndretFeltType("AKTIVITET");
    public static final HistorikkEndretFeltType AKTIVITET_PERIODE = new HistorikkEndretFeltType("AKTIVITET_PERIODE");
    public static final HistorikkEndretFeltType ALENEOMSORG = new HistorikkEndretFeltType("ALENEOMSORG");
    public static final HistorikkEndretFeltType ANTALL_BARN = new HistorikkEndretFeltType("ANTALL_BARN");
    public static final HistorikkEndretFeltType AVKLARSAKSOPPLYSNINGER = new HistorikkEndretFeltType("AVKLARSAKSOPPLYSNINGER");
    public static final HistorikkEndretFeltType BEHANDLENDE_ENHET = new HistorikkEndretFeltType("BEHANDLENDE_ENHET");
    public static final HistorikkEndretFeltType BEHANDLING = new HistorikkEndretFeltType("BEHANDLING");
    public static final HistorikkEndretFeltType BRUK_ANTALL_I_SOKNAD = new HistorikkEndretFeltType("BRUK_ANTALL_I_SOKNAD");
    public static final HistorikkEndretFeltType BRUK_ANTALL_I_TPS = new HistorikkEndretFeltType("BRUK_ANTALL_I_TPS");
    public static final HistorikkEndretFeltType BRUK_ANTALL_I_VEDTAKET = new HistorikkEndretFeltType("BRUK_ANTALL_I_VEDTAKET");
    public static final HistorikkEndretFeltType BRUTTO_NAERINGSINNTEKT = new HistorikkEndretFeltType("BRUTTO_NAERINGSINNTEKT");
    public static final HistorikkEndretFeltType DOKUMENTASJON_FORELIGGER = new HistorikkEndretFeltType("DOKUMENTASJON_FORELIGGER");
    public static final HistorikkEndretFeltType EKTEFELLES_BARN = new HistorikkEndretFeltType("EKTEFELLES_BARN");
    public static final HistorikkEndretFeltType ENDRING_NAERING = new HistorikkEndretFeltType("ENDRING_NAERING");
    public static final HistorikkEndretFeltType ENDRING_TIDSBEGRENSET_ARBEIDSFORHOLD = new HistorikkEndretFeltType("ENDRING_TIDSBEGRENSET_ARBEIDSFORHOLD");
    public static final HistorikkEndretFeltType ER_SOKER_BOSATT_I_NORGE = new HistorikkEndretFeltType("ER_SOKER_BOSATT_I_NORGE");
    public static final HistorikkEndretFeltType FODSELSVILKARET = new HistorikkEndretFeltType("FODSELSVILKARET");
    public static final HistorikkEndretFeltType FODSELSDATO = new HistorikkEndretFeltType("FODSELSDATO");
    public static final HistorikkEndretFeltType FORDELING_FOR_ANDEL = new HistorikkEndretFeltType("FORDELING_FOR_ANDEL");
    public static final HistorikkEndretFeltType FORDELING_FOR_NY_ANDEL = new HistorikkEndretFeltType("FORDELING_FOR_NY_ANDEL");
    public static final HistorikkEndretFeltType FORDELING_ETTER_BESTEBEREGNING = new HistorikkEndretFeltType("FORDELING_ETTER_BESTEBEREGNING");
    public static final HistorikkEndretFeltType FORELDREANSVARSVILKARET = new HistorikkEndretFeltType("FORELDREANSVARSVILKARET");
    public static final HistorikkEndretFeltType FRILANS_INNTEKT = new HistorikkEndretFeltType("FRILANS_INNTEKT");
    public static final HistorikkEndretFeltType FRILANSVIRKSOMHET = new HistorikkEndretFeltType("FRILANSVIRKSOMHET");
    public static final HistorikkEndretFeltType GYLDIG_MEDLEM_FOLKETRYGDEN = new HistorikkEndretFeltType("GYLDIG_MEDLEM_FOLKETRYGDEN");
    public static final HistorikkEndretFeltType INNTEKT_FRA_ARBEIDSFORHOLD = new HistorikkEndretFeltType("INNTEKT_FRA_ARBEIDSFORHOLD");
    public static final HistorikkEndretFeltType LØNNSENDRING_I_PERIODEN = new HistorikkEndretFeltType("LØNNSENDRING_I_PERIODEN");
    public static final HistorikkEndretFeltType MANN_ADOPTERER = new HistorikkEndretFeltType("MANN_ADOPTERER");
    public static final HistorikkEndretFeltType MOTTAR_YTELSE_ARBEID = new HistorikkEndretFeltType("MOTTAR_YTELSE_ARBEID");
    public static final HistorikkEndretFeltType MOTTAR_YTELSE_FRILANS = new HistorikkEndretFeltType("MOTTAR_YTELSE_FRILANS");
    public static final HistorikkEndretFeltType MOTTATT_DATO = new HistorikkEndretFeltType("MOTTATT_DATO");
    public static final HistorikkEndretFeltType OMSORG = new HistorikkEndretFeltType("OMSORG");
    public static final HistorikkEndretFeltType OMSORGSOVERTAKELSESDATO = new HistorikkEndretFeltType("OMSORGSOVERTAKELSESDATO");
    public static final HistorikkEndretFeltType OMSORGSVILKAR = new HistorikkEndretFeltType("OMSORGSVILKAR");
    public static final HistorikkEndretFeltType IKKE_OMSORG_PERIODEN = new HistorikkEndretFeltType("IKKE_OMSORG_PERIODEN");
    public static final HistorikkEndretFeltType INNTEKTSKATEGORI_FOR_ANDEL = new HistorikkEndretFeltType("INNTEKTSKATEGORI_FOR_ANDEL");
    public static final HistorikkEndretFeltType OPPHOLDSRETT_EOS = new HistorikkEndretFeltType("OPPHOLDSRETT_EOS");
    public static final HistorikkEndretFeltType OPPHOLDSRETT_IKKE_EOS = new HistorikkEndretFeltType("OPPHOLDSRETT_IKKE_EOS");
    public static final HistorikkEndretFeltType OVERSTYRT_BEREGNING = new HistorikkEndretFeltType("OVERSTYRT_BEREGNING");
    public static final HistorikkEndretFeltType OVERSTYRT_VURDERING = new HistorikkEndretFeltType("OVERSTYRT_VURDERING");
    public static final HistorikkEndretFeltType SELVSTENDIG_NÆRINGSDRIVENDE = new HistorikkEndretFeltType("SELVSTENDIG_NAERINGSDRIVENDE");
    public static final HistorikkEndretFeltType SOKERSOPPLYSNINGSPLIKT = new HistorikkEndretFeltType("SOKERSOPPLYSNINGSPLIKT");
    public static final HistorikkEndretFeltType SOKNADSFRIST = new HistorikkEndretFeltType("SOKNADSFRIST");
    public static final HistorikkEndretFeltType SOKNADSFRISTVILKARET = new HistorikkEndretFeltType("SOKNADSFRISTVILKARET");
    public static final HistorikkEndretFeltType STARTDATO_FRA_SOKNAD = new HistorikkEndretFeltType("STARTDATO_FRA_SOKNAD");
    public static final HistorikkEndretFeltType TERMINDATO = new HistorikkEndretFeltType("TERMINDATO");
    public static final HistorikkEndretFeltType UTSTEDTDATO = new HistorikkEndretFeltType("UTSTEDTDATO");
    public static final HistorikkEndretFeltType VILKAR_SOM_ANVENDES = new HistorikkEndretFeltType("VILKAR_SOM_ANVENDES");
    public static final HistorikkEndretFeltType FASTSETT_RESULTAT_PERIODEN = new HistorikkEndretFeltType("FASTSETT_RESULTAT_PERIODEN");
    public static final HistorikkEndretFeltType AVKLART_PERIODE = new HistorikkEndretFeltType("AVKLART_PERIODE");
    public static final HistorikkEndretFeltType ANDEL_ARBEID = new HistorikkEndretFeltType("ANDEL_ARBEID");
    public static final HistorikkEndretFeltType UTTAK_TREKKDAGER = new HistorikkEndretFeltType("UTTAK_TREKKDAGER");
    public static final HistorikkEndretFeltType UTTAK_STØNADSKONTOTYPE = new HistorikkEndretFeltType("UTTAK_STØNADSKONTOTYPE");
    public static final HistorikkEndretFeltType UTTAK_PERIODE_RESULTAT_TYPE = new HistorikkEndretFeltType("UTTAK_PERIODE_RESULTAT_TYPE");
    public static final HistorikkEndretFeltType UTTAK_PROSENT_UTBETALING = new HistorikkEndretFeltType("UTTAK_PROSENT_UTBETALING");
    public static final HistorikkEndretFeltType UTTAK_SAMTIDIG_UTTAK = new HistorikkEndretFeltType("UTTAK_SAMTIDIG_UTTAK");
    public static final HistorikkEndretFeltType UTTAK_TREKKDAGER_FLERBARN_KVOTE = new HistorikkEndretFeltType("UTTAK_TREKKDAGER_FLERBARN_KVOTE");
    public static final HistorikkEndretFeltType UTTAK_PERIODE_RESULTAT_ÅRSAK = new HistorikkEndretFeltType("UTTAK_PERIODE_RESULTAT_ÅRSAK");
    public static final HistorikkEndretFeltType UTTAK_GRADERING_ARBEIDSFORHOLD = new HistorikkEndretFeltType("UTTAK_GRADERING_ARBEIDSFORHOLD");
    public static final HistorikkEndretFeltType UTTAK_GRADERING_AVSLAG_ÅRSAK = new HistorikkEndretFeltType("UTTAK_GRADERING_AVSLAG_ÅRSAK");
    public static final HistorikkEndretFeltType UTTAK_SPLITT_TIDSPERIODE = new HistorikkEndretFeltType("UTTAK_SPLITT_TIDSPERIODE");
    public static final HistorikkEndretFeltType SYKDOM = new HistorikkEndretFeltType("SYKDOM");
    public static final HistorikkEndretFeltType ARBEIDSFORHOLD = new HistorikkEndretFeltType("ARBEIDSFORHOLD");
    public static final HistorikkEndretFeltType NY_FORDELING = new HistorikkEndretFeltType("NY_FORDELING");
    public static final HistorikkEndretFeltType NY_AKTIVITET = new HistorikkEndretFeltType("NY_AKTIVITET");
    public static final HistorikkEndretFeltType NYTT_REFUSJONSKRAV = new HistorikkEndretFeltType("NYTT_REFUSJONSKRAV");
    public static final HistorikkEndretFeltType INNTEKT = new HistorikkEndretFeltType("INNTEKT");
    public static final HistorikkEndretFeltType INNTEKTSKATEGORI = new HistorikkEndretFeltType("INNTEKTSKATEGORI");
    public static final HistorikkEndretFeltType NAVN = new HistorikkEndretFeltType("NAVN");
    public static final HistorikkEndretFeltType FNR = new HistorikkEndretFeltType("FNR");
    public static final HistorikkEndretFeltType PERIODE_FOM = new HistorikkEndretFeltType("PERIODE_FOM");
    public static final HistorikkEndretFeltType PERIODE_TOM = new HistorikkEndretFeltType("PERIODE_TOM");
    public static final HistorikkEndretFeltType MANDAT = new HistorikkEndretFeltType("MANDAT");
    public static final HistorikkEndretFeltType KONTAKTPERSON = new HistorikkEndretFeltType("KONTAKTPERSON");
    public static final HistorikkEndretFeltType BRUKER_TVUNGEN = new HistorikkEndretFeltType("BRUKER_TVUNGEN");
    public static final HistorikkEndretFeltType TYPE_VERGE = new HistorikkEndretFeltType("TYPE_VERGE");
    public static final HistorikkEndretFeltType DAGPENGER_INNTEKT = new HistorikkEndretFeltType("DAGPENGER_INNTEKT");
    public static final HistorikkEndretFeltType KLAGE_RESULTAT_NFP = new HistorikkEndretFeltType("KLAGE_RESULTAT_NFP");
    public static final HistorikkEndretFeltType KLAGE_RESULTAT_KA = new HistorikkEndretFeltType("KLAGE_RESULTAT_KA");
    public static final HistorikkEndretFeltType KLAGE_OMGJØR_ÅRSAK = new HistorikkEndretFeltType("KLAGE_OMGJØR_ÅRSAK");
    public static final HistorikkEndretFeltType ER_KLAGER_PART = new HistorikkEndretFeltType("ER_KLAGER_PART");
    public static final HistorikkEndretFeltType ER_KLAGE_KONKRET = new HistorikkEndretFeltType("ER_KLAGE_KONKRET");
    public static final HistorikkEndretFeltType ER_KLAGEFRIST_OVERHOLDT = new HistorikkEndretFeltType("ER_KLAGEFRIST_OVERHOLDT");
    public static final HistorikkEndretFeltType ER_KLAGEN_SIGNERT = new HistorikkEndretFeltType("ER_KLAGEN_SIGNERT");
    public static final HistorikkEndretFeltType PA_KLAGD_BEHANDLINGID = new HistorikkEndretFeltType("PA_KLAGD_BEHANDLINGID");
    public static final HistorikkEndretFeltType VURDER_ETTERLØNN_SLUTTPAKKE = new HistorikkEndretFeltType("VURDER_ETTERLØNN_SLUTTPAKKE");
    public static final HistorikkEndretFeltType FASTSETT_ETTERLØNN_SLUTTPAKKE = new HistorikkEndretFeltType("FASTSETT_ETTERLØNN_SLUTTPAKKE");
    public static final HistorikkEndretFeltType ER_VILKÅRENE_TILBAKEKREVING_OPPFYLT = new HistorikkEndretFeltType("ER_VILKARENE_TILBAKEKREVING_OPPFYLT");
    public static final HistorikkEndretFeltType ER_SÆRLIGE_GRUNNER_TIL_REDUKSJON = new HistorikkEndretFeltType("ER_SARLIGE_GRUNNER_TIL_REDUKSJON");
    public static final HistorikkEndretFeltType FASTSETT_VIDERE_BEHANDLING = new HistorikkEndretFeltType("FASTSETT_VIDERE_BEHANDLING");
    public static final HistorikkEndretFeltType RETT_TIL_FORELDREPENGER = new HistorikkEndretFeltType("RETT_TIL_FORELDREPENGER");
    public static final HistorikkEndretFeltType VURDER_GRADERING_PÅ_ANDEL_UTEN_BG = new HistorikkEndretFeltType("VURDER_GRADERING_PÅ_ANDEL_UTEN_BG");
    public static final HistorikkEndretFeltType DEKNINGSGRAD = new HistorikkEndretFeltType("DEKNINGSGRAD");
    public static final HistorikkEndretFeltType HENDELSE_ÅRSAK= new HistorikkEndretFeltType("HENDELSE_AARSAK");
    public static final HistorikkEndretFeltType HENDELSE_UNDER_ÅRSAK = new HistorikkEndretFeltType("HENDELSE_UNDER_AARSAK");
    public static final HistorikkEndretFeltType MOTTAKER_UAKTSOMHET_GRAD = new HistorikkEndretFeltType("MOTTAKER_UAKTSOMHET_GRAD");
    public static final HistorikkEndretFeltType ANDEL_TILBAKEKREVES = new HistorikkEndretFeltType("ANDEL_TILBAKEKREVES");
    public static final HistorikkEndretFeltType BELØP_TILBAKEKREVES = new HistorikkEndretFeltType("BELOEP_TILBAKEKREVES");
    public static final HistorikkEndretFeltType TILBAKEKREV_SMÅBELOEP = new HistorikkEndretFeltType("TILBAKEKREV_SMAABELOEP");
    public static final HistorikkEndretFeltType ER_BELØPET_BEHOLD = new HistorikkEndretFeltType("BELOEP_ER_I_BEHOLD");
    public static final HistorikkEndretFeltType ILEGG_RENTER = new HistorikkEndretFeltType("ILEGG_RENTER");
    public static final HistorikkEndretFeltType FORELDELSE = new HistorikkEndretFeltType("FORELDELSE");

    public HistorikkEndretFeltType() {
        //
    }

    private HistorikkEndretFeltType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}