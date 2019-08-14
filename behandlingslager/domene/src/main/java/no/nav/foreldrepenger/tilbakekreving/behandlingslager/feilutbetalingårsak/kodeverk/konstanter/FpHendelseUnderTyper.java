package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;


public interface FpHendelseUnderTyper {

    // Medlemskap
    HendelseUnderType UTVANDRET = new HendelseUnderType("UTVANDRET");
    HendelseUnderType IKKE_OPPHOLDSTILLATLSE = new HendelseUnderType("IKKE_OPPHOLDSTILLATLSE");
    HendelseUnderType MEDLEM_I_ANNET_LAND = new HendelseUnderType("MEDLEM_I_ANNET_LAND");
    HendelseUnderType IKKE_JOBBET = new HendelseUnderType("IKKE_JOBBET");
    HendelseUnderType MER_OPPHOLD = new HendelseUnderType("MER_OPPHOLD");

    //Opptjening
    HendelseUnderType IKKE_INNTEKT = new HendelseUnderType("IKKE_INNTEKT");
    HendelseUnderType IKKE_YRKESAKTIV = new HendelseUnderType("IKKE_YRKESAKTIV");

    //Beregning
    HendelseUnderType INNTEKT_UNDER = new HendelseUnderType("INNTEKT_UNDER");
    HendelseUnderType ENDRING_GRUNNLAG = new HendelseUnderType("ENDRING_GRUNNLAG");

    //Stønadsperioden
    HendelseUnderType ENDRET_DEKNINGSGRAD = new HendelseUnderType("ENDRET_DEKNINGSGRAD");
    HendelseUnderType FEIL_FLERBARNSDAGER = new HendelseUnderType("FEIL_FLERBARNSDAGER");
    HendelseUnderType OPPHOR_BARN_DOD = new HendelseUnderType("OPPHOR_BARN_DOD");
    HendelseUnderType OPPHOR_MOTTAKER_DOD = new HendelseUnderType("OPPHOR_MOTTAKER_DOD");

    //UttakGenerelt
    HendelseUnderType STONADSPERIODE_OVER_3 = new HendelseUnderType("STONADSPERIODE_OVER_3");
    HendelseUnderType NY_STONADSPERIODE = new HendelseUnderType("NY_STONADSPERIODE");
    HendelseUnderType IKKE_OMSORG = new HendelseUnderType("IKKE_OMSORG");
    HendelseUnderType MOTTAKER_I_ARBEID = new HendelseUnderType("MOTTAKER_I_ARBEID");
    HendelseUnderType FORELDRES_UTTAK = new HendelseUnderType("FORELDRES_UTTAK");
    HendelseUnderType STONADSPERIODE_MANGEL = new HendelseUnderType("STONADSPERIODE_MANGEL");

    //UttakUtsettelse
    HendelseUnderType LOVBESTEMT_FERIE = new HendelseUnderType("LOVBESTEMT_FERIE");
    HendelseUnderType ARBEID_HELTID = new HendelseUnderType("ARBEID_HELTID");
    HendelseUnderType MOTTAKER_HELT_AVHENGIG = new HendelseUnderType("MOTTAKER_HELT_AVHENGIG");
    HendelseUnderType MOTTAKER_INNLAGT = new HendelseUnderType("MOTTAKER_INNLAGT");
    HendelseUnderType BARN_INNLAGT = new HendelseUnderType("BARN_INNLAGT");

    //UttakKvotene
    HendelseUnderType KVO_MOTTAKER_HELT_AVHENGIG = new HendelseUnderType("KVO_MOTTAKER_HELT_AVHENGIG");
    HendelseUnderType KVO_MOTTAKER_INNLAGT = new HendelseUnderType("KVO_MOTTAKER_INNLAGT");

    //VilkårGenerelle
    HendelseUnderType MOR_IKKE_ARBEID = new HendelseUnderType("MOR_IKKE_ARBEID");
    HendelseUnderType MOR_IKKE_STUDERT = new HendelseUnderType("MOR_IKKE_STUDERT");
    HendelseUnderType MOR_IKKE_ARBEID_OG_STUDER = new HendelseUnderType("MOR_IKKE_ARBEID_OG_STUDER");
    HendelseUnderType MOR_IKKE_HELT_AVHENGIG = new HendelseUnderType("MOR_IKKE_HELT_AVHENGIG");
    HendelseUnderType MOR_IKKE_INNLAGT = new HendelseUnderType("MOR_IKKE_INNLAGT");
    HendelseUnderType MOR_IKKE_I_IP = new HendelseUnderType("MOR_IKKE_I_IP");
    HendelseUnderType MOR_IKKE_I_KP = new HendelseUnderType("MOR_IKKE_I_KP");

    //KunRett
    HendelseUnderType FEIL_I_ANTALL_DAGER = new HendelseUnderType("FEIL_I_ANTALL_DAGER");

    //UttakAleneomsorg
    HendelseUnderType IKKE_ALENEOMSORG = new HendelseUnderType("IKKE_ALENEOMSORG");

    //UttakGradert
    HendelseUnderType GRADERT_UTTAK = new HendelseUnderType("GRADERT_UTTAK");

    //Annet
    HendelseUnderType OKONOMI_DOBBELUTBETALING = new HendelseUnderType("OKONOMI_DOBBELUTBETALING");
    HendelseUnderType OKONOMI_UTBETALT = new HendelseUnderType("OKONOMI_UTBETALT");
    HendelseUnderType OKONOMI_FEIL_TREKK = new HendelseUnderType("OKONOMI_FEIL_TREKK");
    HendelseUnderType ANNET_FRITEKST = new HendelseUnderType("ANNET_FRITEKST");

}
