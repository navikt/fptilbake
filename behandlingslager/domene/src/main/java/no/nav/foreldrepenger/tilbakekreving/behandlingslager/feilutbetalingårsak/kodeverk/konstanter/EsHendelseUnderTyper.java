package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;


public interface EsHendelseUnderTyper {

    //Medlemskap
    HendelseUnderType ES_UTVANDRET = new HendelseUnderType("ES_UTVANDRET");
    HendelseUnderType ES_IKKE_BOSATT = new HendelseUnderType("ES_IKKE_BOSATT");
    HendelseUnderType ES_IKKE_OPPHOLDSRETT = new HendelseUnderType("ES_IKKE_OPPHOLDSRETT");
    HendelseUnderType ES_IKKE_LOVLIG_OPPHOLD = new HendelseUnderType("ES_IKKE_LOVLIG_OPPHOLD");
    HendelseUnderType ES_MEDLEMSKAP_UNNTAKK = new HendelseUnderType("ES_MEDLEMSKAP_UNNTAKK");

    //Fødslesvilkår
    HendelseUnderType ES_BARN_IKKE_REGISTRERT = new HendelseUnderType("ES_BARN_IKKE_REGISTRERT");
    HendelseUnderType ES_MOTTAKER_FAR_MEDMOR = new HendelseUnderType("ES_MOTTAKER_FAR_MEDMOR");

    //Adopsjonsvilkår
    HendelseUnderType ES_IKKE_OPPFYLT = new HendelseUnderType("ES_IKKE_OPPFYLT");
    HendelseUnderType ES_BARN_OVER_15 = new HendelseUnderType("ES_BARN_OVER_15");
    HendelseUnderType ES_MANN_IKKE_ALENE = new HendelseUnderType("ES_MANN_IKKE_ALENE");
    HendelseUnderType ES_STEBARN = new HendelseUnderType("ES_STEBARN");

    //Foreldresansvar
    HendelseUnderType ES_ANDRE_FORELDRE_DODD = new HendelseUnderType("ES_ANDRE_FORELDRE_DODD");
    HendelseUnderType ES_IKKE_TILDELT = new HendelseUnderType("ES_IKKE_TILDELT");
    HendelseUnderType ES_IKKE_MINDRE_SAMVAER = new HendelseUnderType("ES_IKKE_MINDRE_SAMVAER");
    HendelseUnderType ES_FORELDREANSVAR_BARN_OVER_15 = new HendelseUnderType("ES_FORELDREANSVAR_BARN_OVER_15");

    //Omsorgsvilkår
    HendelseUnderType ES_FAR_IKKE_OMSORG = new HendelseUnderType("ES_FAR_IKKE_OMSORG");
    HendelseUnderType ES_STONADEN_ALLEREDE_UTBETALT = new HendelseUnderType("ES_STONADEN_ALLEREDE_UTBETALT");

    //Foreldresansvarfar
    HendelseUnderType ES_FAR_IKKE_ALENE = new HendelseUnderType("ES_FAR_IKKE_ALENE");
    HendelseUnderType ES_FAR_IKKE_INNEN_STONADSPERIODE = new HendelseUnderType("ES_FAR_IKKE_INNEN_STONADSPERIODE");

    //BrukerRett
    HendelseUnderType ES_BRUKER_RETT_FORELDREPENGER = new HendelseUnderType("ES_BRUKER_RETT_FORELDREPENGER");

    //Feilutbetaling
    HendelseUnderType ES_STONAD_FLERE_GANGER = new HendelseUnderType("ES_STONAD_FLERE_GANGER");

}
