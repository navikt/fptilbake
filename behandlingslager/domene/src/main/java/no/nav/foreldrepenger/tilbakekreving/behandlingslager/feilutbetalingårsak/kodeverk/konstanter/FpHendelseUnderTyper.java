package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter;

import java.util.Set;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;


public interface FpHendelseUnderTyper {

    //Opptjening
    HendelseUnderType IKKE_INNTEKT = HendelseUnderType.IKKE_INNTEKT;
    HendelseUnderType IKKE_YRKESAKTIV = HendelseUnderType.IKKE_YRKESAKTIV;

    //Beregning
    HendelseUnderType INNTEKT_UNDER = HendelseUnderType.INNTEKT_UNDER;
    HendelseUnderType ENDRING_GRUNNLAG = HendelseUnderType.ENDRING_GRUNNLAG;

    //Stønadsperioden
    HendelseUnderType ENDRET_DEKNINGSGRAD = HendelseUnderType.ENDRET_DEKNINGSGRAD;
    HendelseUnderType FEIL_FLERBARNSDAGER = HendelseUnderType.FEIL_FLERBARNSDAGER;
    HendelseUnderType OPPHOR_BARN_DOD = HendelseUnderType.OPPHOR_BARN_DOD;
    HendelseUnderType OPPHOR_MOTTAKER_DOD = HendelseUnderType.OPPHOR_MOTTAKER_DOD;

    //UttakGenerelt
    HendelseUnderType STONADSPERIODE_OVER_3 = HendelseUnderType.STONADSPERIODE_OVER_3;
    HendelseUnderType NY_STONADSPERIODE = HendelseUnderType.NY_STONADSPERIODE;
    HendelseUnderType IKKE_OMSORG = HendelseUnderType.IKKE_OMSORG;
    HendelseUnderType MOTTAKER_I_ARBEID = HendelseUnderType.MOTTAKER_I_ARBEID;
    HendelseUnderType FORELDRES_UTTAK = HendelseUnderType.FORELDRES_UTTAK;
    HendelseUnderType STONADSPERIODE_MANGEL = HendelseUnderType.STONADSPERIODE_MANGEL;

    //UttakUtsettelse
    HendelseUnderType LOVBESTEMT_FERIE = HendelseUnderType.LOVBESTEMT_FERIE;
    HendelseUnderType ARBEID_HELTID = HendelseUnderType.ARBEID_HELTID;
    HendelseUnderType MOTTAKER_HELT_AVHENGIG = HendelseUnderType.MOTTAKER_HELT_AVHENGIG;
    HendelseUnderType MOTTAKER_INNLAGT = HendelseUnderType.MOTTAKER_INNLAGT;
    HendelseUnderType BARN_INNLAGT = HendelseUnderType.BARN_INNLAGT;

    //UttakKvotene
    HendelseUnderType KVO_MOTTAKER_HELT_AVHENGIG = HendelseUnderType.KVO_MOTTAKER_HELT_AVHENGIG;
    HendelseUnderType KVO_MOTTAKER_INNLAGT = HendelseUnderType.KVO_MOTTAKER_INNLAGT;
    HendelseUnderType KVO_SAMTIDIG_UTTAK = HendelseUnderType.KVO_SAMTIDIG_UTTAK;

    //VilkårGenerelle
    HendelseUnderType MOR_IKKE_ARBEID = HendelseUnderType.MOR_IKKE_ARBEID;
    HendelseUnderType MOR_IKKE_STUDERT = HendelseUnderType.MOR_IKKE_STUDERT;
    HendelseUnderType MOR_IKKE_ARBEID_OG_STUDER = HendelseUnderType.MOR_IKKE_ARBEID_OG_STUDER;
    HendelseUnderType MOR_IKKE_HELT_AVHENGIG = HendelseUnderType.MOR_IKKE_HELT_AVHENGIG;
    HendelseUnderType MOR_IKKE_INNLAGT = HendelseUnderType.MOR_IKKE_INNLAGT;
    HendelseUnderType MOR_IKKE_I_IP = HendelseUnderType.MOR_IKKE_I_IP;
    HendelseUnderType MOR_IKKE_I_KP = HendelseUnderType.MOR_IKKE_I_KP;

    //KunRett
    HendelseUnderType FEIL_I_ANTALL_DAGER = HendelseUnderType.FEIL_I_ANTALL_DAGER;

    //UttakAleneomsorg
    HendelseUnderType IKKE_ALENEOMSORG = HendelseUnderType.IKKE_ALENEOMSORG;

    //UttakGradert
    HendelseUnderType GRADERT_UTTAK = HendelseUnderType.GRADERT_UTTAK;

    Set<HendelseUnderType> ALLE = Set.of(
        IKKE_INNTEKT,
        IKKE_YRKESAKTIV,
        INNTEKT_UNDER,
        ENDRING_GRUNNLAG,
        ENDRET_DEKNINGSGRAD,
        FEIL_FLERBARNSDAGER,
        OPPHOR_BARN_DOD,
        OPPHOR_MOTTAKER_DOD,
        STONADSPERIODE_OVER_3,
        NY_STONADSPERIODE,
        IKKE_OMSORG,
        MOTTAKER_I_ARBEID,
        FORELDRES_UTTAK,
        STONADSPERIODE_MANGEL,
        LOVBESTEMT_FERIE,
        ARBEID_HELTID,
        MOTTAKER_HELT_AVHENGIG,
        MOTTAKER_INNLAGT,
        BARN_INNLAGT,
        KVO_MOTTAKER_HELT_AVHENGIG,
        KVO_MOTTAKER_INNLAGT,
        KVO_SAMTIDIG_UTTAK,
        MOR_IKKE_ARBEID,
        MOR_IKKE_STUDERT,
        MOR_IKKE_ARBEID_OG_STUDER,
        MOR_IKKE_HELT_AVHENGIG,
        MOR_IKKE_INNLAGT,
        MOR_IKKE_I_IP,
        MOR_IKKE_I_KP,
        FEIL_I_ANTALL_DAGER,
        IKKE_ALENEOMSORG,
        GRADERT_UTTAK
    );

}
