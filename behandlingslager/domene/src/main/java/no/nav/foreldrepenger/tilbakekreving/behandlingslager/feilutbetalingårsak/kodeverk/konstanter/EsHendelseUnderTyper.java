package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;


public interface EsHendelseUnderTyper {

    //Fødslesvilkår
    HendelseUnderType ES_BARN_IKKE_REGISTRERT = new HendelseUnderType("ES_BARN_IKKE_REGISTRERT", "ES_BARN_IKKE_REGISTRERT","Barn ikke registrert",0);
    HendelseUnderType ES_MOTTAKER_FAR_MEDMOR = new HendelseUnderType("ES_MOTTAKER_FAR_MEDMOR", "ES_MOTTAKER_FAR_MEDMOR","Mottaker er far eller medmor",1);

    //Adopsjonsvilkår
    HendelseUnderType ES_IKKE_OPPFYLT = new HendelseUnderType("ES_IKKE_OPPFYLT", "ES_IKKE_OPPFYLT","Adopsjonsvilkår ikke oppfylt",0);
    HendelseUnderType ES_BARN_OVER_15 = new HendelseUnderType("ES_BARN_OVER_15", "ES_BARN_OVER_15","Barn over 15 år",1);
    HendelseUnderType ES_MANN_IKKE_ALENE = new HendelseUnderType("ES_MANN_IKKE_ALENE", "ES_MANN_IKKE_ALENE","Mann adopterer ikke alene",2);
    HendelseUnderType ES_STEBARN = new HendelseUnderType("ES_STEBARN", "ES_STEBARN","Stebarnsadopsjon",3);

    //Foreldresansvar
    HendelseUnderType ES_ANDRE_FORELDRE_DODD = new HendelseUnderType("ES_ANDRE_FORELDRE_DODD", "ES_ANDRE_FORELDRE_DODD","Har ikke foreldreansvar ved andre foreldres død",0);
    HendelseUnderType ES_IKKE_TILDELT = new HendelseUnderType("ES_IKKE_TILDELT", "ES_IKKE_TILDELT","Ikke tildelt foreldreansvar etter barneloven",1);
    HendelseUnderType ES_IKKE_MINDRE_SAMVAER = new HendelseUnderType("ES_IKKE_MINDRE_SAMVAER", "ES_IKKE_MINDRE_SAMVAER","Ikke hatt mindre samvær enn barneloven §43",2);
    HendelseUnderType ES_FORELDREANSVAR_BARN_OVER_15 = new HendelseUnderType("ES_FORELDREANSVAR_BARN_OVER_15", "ES_FORELDREANSVAR_BARN_OVER_15","Barn over 15 år",3);

    //Omsorgsvilkår
    HendelseUnderType ES_FAR_IKKE_OMSORG = new HendelseUnderType("ES_FAR_IKKE_OMSORG", "ES_FAR_IKKE_OMSORG","Far ikke omsorg for barnet",0);
    HendelseUnderType ES_STONADEN_ALLEREDE_UTBETALT = new HendelseUnderType("ES_STONADEN_ALLEREDE_UTBETALT", "ES_STONADEN_ALLEREDE_UTBETALT","Stønaden allerede utbetalt til mor",1);

    //Foreldresansvarfar
    HendelseUnderType ES_FAR_IKKE_ALENE = new HendelseUnderType("ES_FAR_IKKE_ALENE", "ES_FAR_IKKE_ALENE","Far overtar ikke foreldreansvar alene",0);
    HendelseUnderType ES_FAR_IKKE_INNEN_STONADSPERIODE = new HendelseUnderType("ES_FAR_IKKE_INNEN_STONADSPERIODE", "ES_FAR_IKKE_INNEN_STONADSPERIODE","Far overtar ikke innen stønadsperioden",1);

    //BrukerRett
    HendelseUnderType ES_BRUKER_RETT_FORELDREPENGER = new HendelseUnderType("ES_BRUKER_RETT_FORELDREPENGER", "ES_BRUKER_RETT_FORELDREPENGER","Bruker har likevel rett på foreldrepenger",0);

    //Feilutbetaling
    HendelseUnderType ES_STONAD_FLERE_GANGER = new HendelseUnderType("ES_STONAD_FLERE_GANGER", "ES_STONAD_FLERE_GANGER","Stønad gitt for samme barn flere ganger",0);

}
