package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;


public interface EsHendelseUnderTyper {

    //Fødslesvilkår
    HendelseUnderType ES_BARN_IKKE_REGISTRERT = HendelseUnderType.ES_BARN_IKKE_REGISTRERT;
    HendelseUnderType ES_MOTTAKER_FAR_MEDMOR = HendelseUnderType.ES_MOTTAKER_FAR_MEDMOR;

    //Adopsjonsvilkår
    HendelseUnderType ES_IKKE_OPPFYLT = HendelseUnderType.ES_IKKE_OPPFYLT;
    HendelseUnderType ES_BARN_OVER_15 = HendelseUnderType.ES_BARN_OVER_15;
    HendelseUnderType ES_MANN_IKKE_ALENE = HendelseUnderType.ES_MANN_IKKE_ALENE;
    HendelseUnderType ES_STEBARN = HendelseUnderType.ES_STEBARN;

    //Foreldresansvar
    HendelseUnderType ES_ANDRE_FORELDRE_DODD = HendelseUnderType.ES_ANDRE_FORELDRE_DODD;
    HendelseUnderType ES_IKKE_TILDELT = HendelseUnderType.ES_IKKE_TILDELT;
    HendelseUnderType ES_IKKE_MINDRE_SAMVAER = HendelseUnderType.ES_IKKE_MINDRE_SAMVAER;
    HendelseUnderType ES_FORELDREANSVAR_BARN_OVER_15 = HendelseUnderType.ES_FORELDREANSVAR_BARN_OVER_15;

    //Omsorgsvilkår
    HendelseUnderType ES_FAR_IKKE_OMSORG = HendelseUnderType.ES_FAR_IKKE_OMSORG;
    HendelseUnderType ES_STONADEN_ALLEREDE_UTBETALT = HendelseUnderType.ES_STONADEN_ALLEREDE_UTBETALT;

    //Foreldresansvarfar
    HendelseUnderType ES_FAR_IKKE_ALENE = HendelseUnderType.ES_FAR_IKKE_ALENE;
    HendelseUnderType ES_FAR_IKKE_INNEN_STONADSPERIODE = HendelseUnderType.ES_FAR_IKKE_INNEN_STONADSPERIODE;

    //BrukerRett
    HendelseUnderType ES_BRUKER_RETT_FORELDREPENGER = HendelseUnderType.ES_BRUKER_RETT_FORELDREPENGER;

    //Feilutbetaling
    HendelseUnderType ES_STONAD_FLERE_GANGER = HendelseUnderType.ES_STONAD_FLERE_GANGER;

}
