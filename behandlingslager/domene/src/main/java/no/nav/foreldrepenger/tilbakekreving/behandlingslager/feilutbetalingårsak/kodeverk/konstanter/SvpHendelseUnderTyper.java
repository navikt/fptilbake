package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;

public interface SvpHendelseUnderTyper {

    //Fakta
    HendelseUnderType SVP_ENDRING_TERMINDATO = new HendelseUnderType("SVP_ENDRING_TERMINDATO", "SVP_ENDRING_TERMINDATO", "Endring i termindato", 0);
    HendelseUnderType SVP_TIDLIG_FODSEL = new HendelseUnderType("SVP_TIDLIG_FODSEL", "SVP_TIDLIG_FODSEL", "Tidlig fødsel", 1);
    HendelseUnderType SVP_IKKE_HELSEFARLIG = new HendelseUnderType("SVP_IKKE_HELSEFARLIG", "SVP_IKKE_HELSEFARLIG", "Ikke helsefarlig for ventende barn", 2);
    //Arbeidsgiverforhold
    HendelseUnderType SVP_TILRETTELEGGING_FULLT_MULIG = new HendelseUnderType("SVP_TILRETTELEGGING_FULLT_MULIG", "SVP_TILRETTELEGGING_FULLT_MULIG", "Tilrettelegging fullt mulig", 0);
    HendelseUnderType SVP_TILRETTELEGGING_DELVIS_MULIG = new HendelseUnderType("SVP_TILRETTELEGGING_DELVIS_MULIG", "SVP_TILRETTELEGGING_DELVIS_MULIG", "Tilrettelegging delvis mulig", 1);
    //Opptjening
    HendelseUnderType SVP_IKKE_ARBEID = new HendelseUnderType("SVP_IKKE_ARBEID", "SVP_IKKE_ARBEID", "Ikke i arbeid siste 4 uker", 10);
    //Beregning
    HendelseUnderType SVP_INNTEKT_UNDER = new HendelseUnderType("SVP_INNTEKT_UNDER", "SVP_INNTEKT_UNDER", "Inntekt under 1/2 G", 30);
    HendelseUnderType SVP_ENDRING_GRUNNLAG = new HendelseUnderType("SVP_ENDRING_GRUNNLAG", "SVP_ENDRING_GRUNNLAG", "Endring i selve grunnlaget", 0);
    //Uttak
    HendelseUnderType SVP_ENDRING_PROSENT = new HendelseUnderType("SVP_ENDRING_PROSENT", "SVP_ENDRING_PROSENT", "Endring i uttaksprosent/gradering", 0);
    HendelseUnderType SVP_ENDRING_PERIODE = new HendelseUnderType("SVP_ENDRING_PERIODE", "SVP_ENDRING_PERIODE", "Endring av periode", 1);
    //opphør
    HendelseUnderType MOTTAKER_DØD = new HendelseUnderType("MOTTAKER_DØD", "MOTTAKER_DØD", "Mottaker død", 1);
    HendelseUnderType MOTTAKER_IKKE_GRAVID = new HendelseUnderType("MOTTAKER_IKKE_GRAVID", "MOTTAKER_IKKE_GRAVID", "Mottaker ikke lenger gravid", 2);
    //inntekt
    HendelseUnderType SVP_INNTEKT_IKKE_TAP = new HendelseUnderType("SVP_INNTEKT_IKKE_TAP", "SVP_INNTEKT_IKKE_TAP", "Ikke tap av pensjonsgivende inntekt", 20);




}
