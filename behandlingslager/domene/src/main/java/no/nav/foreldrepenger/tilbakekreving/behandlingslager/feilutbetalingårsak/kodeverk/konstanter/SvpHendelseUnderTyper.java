package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;

public interface SvpHendelseUnderTyper {

    //Fakta
    HendelseUnderType SVP_ENDRING_TERMINDATO = HendelseUnderType.SVP_ENDRING_TERMINDATO;
    HendelseUnderType SVP_TIDLIG_FODSEL = HendelseUnderType.SVP_TIDLIG_FODSEL;
    HendelseUnderType SVP_IKKE_HELSEFARLIG = HendelseUnderType.SVP_IKKE_HELSEFARLIG;
    //Arbeidsgiverforhold
    HendelseUnderType SVP_TILRETTELEGGING_FULLT_MULIG = HendelseUnderType.SVP_TILRETTELEGGING_FULLT_MULIG;
    HendelseUnderType SVP_TILRETTELEGGING_DELVIS_MULIG = HendelseUnderType.SVP_TILRETTELEGGING_DELVIS_MULIG;
    //Opptjening
    HendelseUnderType SVP_IKKE_ARBEID = HendelseUnderType.SVP_IKKE_ARBEID;
    //Beregning
    HendelseUnderType SVP_INNTEKT_UNDER = HendelseUnderType.SVP_INNTEKT_UNDER;
    HendelseUnderType SVP_ENDRING_GRUNNLAG = HendelseUnderType.SVP_ENDRING_GRUNNLAG;
    //Uttak
    HendelseUnderType SVP_ENDRING_PROSENT = HendelseUnderType.SVP_ENDRING_PROSENT;
    HendelseUnderType SVP_ENDRING_PERIODE = HendelseUnderType.SVP_ENDRING_PERIODE;
    //opphør
    HendelseUnderType MOTTAKER_DØD = HendelseUnderType.MOTTAKER_DØD;
    HendelseUnderType MOTTAKER_IKKE_GRAVID = HendelseUnderType.MOTTAKER_IKKE_GRAVID;
    //inntekt
    HendelseUnderType SVP_INNTEKT_IKKE_TAP = HendelseUnderType.SVP_INNTEKT_IKKE_TAP;




}
