package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;

public interface ØkonomiUndertyper {

    HendelseUnderType DOBBELTUTBETALING = new HendelseUnderType("OKONOMI_DOBBELUTBETALING","OKONOMI_DOBBELUTBETALING","Dobbeltutbetaling",0);
    HendelseUnderType FOR_MYE_UTBETALT = new HendelseUnderType("OKONOMI_UTBETALT","OKONOMI_UTBETALT","Utbetalt for mye",1);
    HendelseUnderType FEIL_TREKK = new HendelseUnderType("OKONOMI_FEIL_TREKK","OKONOMI_FEIL_TREKK","Feil trekk",2);
    HendelseUnderType FEIL_FERIEPENGER = new HendelseUnderType("OKONOMI_FEIL_FERIEPENGER","OKONOMI_FEIL_FERIEPENGER","Feil feriepengeutbetaling",5);
}
