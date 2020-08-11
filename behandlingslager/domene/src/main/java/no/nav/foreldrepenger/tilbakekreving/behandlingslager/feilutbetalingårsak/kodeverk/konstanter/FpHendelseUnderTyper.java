package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter;

import java.util.Set;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;


public interface FpHendelseUnderTyper {

    //Opptjening
    HendelseUnderType IKKE_INNTEKT = new HendelseUnderType("IKKE_INNTEKT","IKKE_INNTEKT","Ikke inntekt 6 av siste 10 måneder",0);
    HendelseUnderType IKKE_YRKESAKTIV = new HendelseUnderType("IKKE_YRKESAKTIV","IKKE_YRKESAKTIV","Ikke yrkesaktiv med pensjonsgivende inntekt",1);

    //Beregning
    HendelseUnderType INNTEKT_UNDER = new HendelseUnderType("INNTEKT_UNDER","INNTEKT_UNDER","Inntekt under 1/2 G",1);
    HendelseUnderType ENDRING_GRUNNLAG = new HendelseUnderType("ENDRING_GRUNNLAG","ENDRING_GRUNNLAG","Endring i selve grunnlaget",0);

    //Stønadsperioden
    HendelseUnderType ENDRET_DEKNINGSGRAD = new HendelseUnderType("ENDRET_DEKNINGSGRAD","ENDRET_DEKNINGSGRAD","Endret dekningsgrad",0);
    HendelseUnderType FEIL_FLERBARNSDAGER = new HendelseUnderType("FEIL_FLERBARNSDAGER","FEIL_FLERBARNSDAGER","Feil i flerbarnsdager",1);
    HendelseUnderType OPPHOR_BARN_DOD = new HendelseUnderType("OPPHOR_BARN_DOD","OPPHOR_BARN_DOD","Opphør barn død",2);
    HendelseUnderType OPPHOR_MOTTAKER_DOD = new HendelseUnderType("OPPHOR_MOTTAKER_DOD","OPPHOR_MOTTAKER_DOD","Opphør mottaker død",3);

    //UttakGenerelt
    HendelseUnderType STONADSPERIODE_OVER_3 = new HendelseUnderType("STONADSPERIODE_OVER_3","STONADSPERIODE_OVER_3","Stønadsperiode over 3 år",0);
    HendelseUnderType NY_STONADSPERIODE = new HendelseUnderType("NY_STONADSPERIODE","NY_STONADSPERIODE","Ny stønadsperiode for nytt barn",1);
    HendelseUnderType IKKE_OMSORG = new HendelseUnderType("IKKE_OMSORG","IKKE_OMSORG","Ikke omsorg for barnet",2);
    HendelseUnderType MOTTAKER_I_ARBEID = new HendelseUnderType("MOTTAKER_I_ARBEID","MOTTAKER_I_ARBEID","Mottaker i arbeid heltid",3);
    HendelseUnderType FORELDRES_UTTAK = new HendelseUnderType("FORELDRES_UTTAK","FORELDRES_UTTAK","Ikke rett til samtidig uttak",4);
    HendelseUnderType STONADSPERIODE_MANGEL = new HendelseUnderType("STONADSPERIODE_MANGEL","STONADSPERIODE_MANGEL","Manglende stønadsperiode",5);

    //UttakUtsettelse
    HendelseUnderType LOVBESTEMT_FERIE = new HendelseUnderType("LOVBESTEMT_FERIE","LOVBESTEMT_FERIE","Lovbestemt ferie",0);
    HendelseUnderType ARBEID_HELTID = new HendelseUnderType("ARBEID_HELTID","ARBEID_HELTID","Arbeid heltid",1);
    HendelseUnderType MOTTAKER_HELT_AVHENGIG = new HendelseUnderType("MOTTAKER_HELT_AVHENGIG","MOTTAKER_HELT_AVHENGIG","Mottaker helt avhenig av hjelp til å ta seg av barnet",2);
    HendelseUnderType MOTTAKER_INNLAGT = new HendelseUnderType("MOTTAKER_INNLAGT","MOTTAKER_INNLAGT","Mottaker innlagt i helseinstitusjon",3);
    HendelseUnderType BARN_INNLAGT = new HendelseUnderType("BARN_INNLAGT","BARN_INNLAGT","Barn innlagt i helseinstitusjon",4);

    //UttakKvotene
    HendelseUnderType KVO_MOTTAKER_HELT_AVHENGIG = new HendelseUnderType("KVO_MOTTAKER_HELT_AVHENGIG","KVO_MOTTAKER_HELT_AVHENGIG","Mottaker helt avhenig av hjelp til å ta seg av barnet",0);
    HendelseUnderType KVO_MOTTAKER_INNLAGT = new HendelseUnderType("KVO_MOTTAKER_INNLAGT","KVO_MOTTAKER_INNLAGT","Mottaker innlagt i helseinstitusjon",1);
    HendelseUnderType KVO_SAMTIDIG_UTTAK = new HendelseUnderType("KVO_SAMTIDIG_UTTAK","KVO_SAMTIDIG_UTTAK","Samtidig uttak",3);

    //VilkårGenerelle
    HendelseUnderType MOR_IKKE_ARBEID = new HendelseUnderType("MOR_IKKE_ARBEID","MOR_IKKE_ARBEID","Mor ikke arbeidet heltid",0);
    HendelseUnderType MOR_IKKE_STUDERT = new HendelseUnderType("MOR_IKKE_STUDERT","MOR_IKKE_STUDERT","Mor ikke studert heltid",1);
    HendelseUnderType MOR_IKKE_ARBEID_OG_STUDER = new HendelseUnderType("MOR_IKKE_ARBEID_OG_STUDER","MOR_IKKE_ARBEID_OG_STUDER","Mor ikke arbeid og studier - heltid",2);
    HendelseUnderType MOR_IKKE_HELT_AVHENGIG = new HendelseUnderType("MOR_IKKE_HELT_AVHENGIG","MOR_IKKE_HELT_AVHENGIG","Mor ikke helt avhengig av hjelp til å ta seg av barnet",3);
    HendelseUnderType MOR_IKKE_INNLAGT = new HendelseUnderType("MOR_IKKE_INNLAGT","MOR_IKKE_INNLAGT","Mor ikke innlagt helseinstitusjon",4);
    HendelseUnderType MOR_IKKE_I_IP = new HendelseUnderType("MOR_IKKE_I_IP","MOR_IKKE_I_IP","Mor ikke i introduksjonsprogram",5);
    HendelseUnderType MOR_IKKE_I_KP = new HendelseUnderType("MOR_IKKE_I_KP","MOR_IKKE_I_KP","Mor ikke i kvalifiseringsprogram",6);

    //KunRett
    HendelseUnderType FEIL_I_ANTALL_DAGER = new HendelseUnderType("FEIL_I_ANTALL_DAGER","FEIL_I_ANTALL_DAGER","Feil i antall dager",0);

    //UttakAleneomsorg
    HendelseUnderType IKKE_ALENEOMSORG = new HendelseUnderType("IKKE_ALENEOMSORG","IKKE_ALENEOMSORG","Ikke aleneomsorg",0);

    //UttakGradert
    HendelseUnderType GRADERT_UTTAK = new HendelseUnderType("GRADERT_UTTAK","GRADERT_UTTAK","Gradert uttak",0);

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
