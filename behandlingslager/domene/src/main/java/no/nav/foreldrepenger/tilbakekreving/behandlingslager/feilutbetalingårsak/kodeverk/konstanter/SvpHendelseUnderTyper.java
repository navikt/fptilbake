package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;

public interface SvpHendelseUnderTyper {

    //Fakta
    HendelseUnderType SVP_ENDRING_TERMINDATO = new HendelseUnderType("SVP_ENDRING_TERMINDATO");
    HendelseUnderType SVP_TIDLIG_FODSEL = new HendelseUnderType("SVP_TIDLIG_FODSEL");
    HendelseUnderType SVP_IKKE_HELSEFARLIG = new HendelseUnderType("SVP_IKKE_HELSEFARLIG");

    //Arbeidsgiverforhold
    HendelseUnderType SVP_TILRETTELEGGING_FULLT_MULIG = new HendelseUnderType("SVP_TILRETTELEGGING_FULLT_MULIG");
    HendelseUnderType SVP_TILRETTELEGGING_DELVIS_MULIG = new HendelseUnderType("SVP_TILRETTELEGGING_DELVIS_MULIG");
    HendelseUnderType SVP_TILRETTELEGGING_IKKE_MULIG = new HendelseUnderType("SVP_TILRETTELEGGING_IKKE_MULIG");

    HendelseUnderType SVP_MANGLER_ARBEIDSFORHOLD = new HendelseUnderType("MANGLER_ARBEIDSFORHOLD");

    //Opptjening
    HendelseUnderType SVP_IKKE_ARBEID = new HendelseUnderType("SVP_IKKE_ARBEID");

    //Beregning
    HendelseUnderType SVP_INNTEKT_UNDER = new HendelseUnderType("SVP_INNTEKT_UNDER");
    HendelseUnderType SVP_ENDRING_GRUNNLAG = new HendelseUnderType("SVP_ENDRING_GRUNNLAG");

    //Uttak
    HendelseUnderType SVP_ENDRING_PROSENT = new HendelseUnderType("SVP_ENDRING_PROSENT");
    HendelseUnderType SVP_ENDRING_PERIODE = new HendelseUnderType("SVP_ENDRING_PERIODE");

    //opphør
    HendelseUnderType MOTTAKER_DØD = new HendelseUnderType("MOTTAKER_DØD");
    HendelseUnderType MOTTAKER_IKKE_GRAVID = new HendelseUnderType("MOTTAKER_IKKE_GRAVID");

    //inntekt
    HendelseUnderType SVP_INNTEKT_IKKE_TAP = new HendelseUnderType("SVP_INNTEKT_IKKE_TAP");

}
