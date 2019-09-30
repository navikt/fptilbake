package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;

public interface ØkonomiUndertyper {

    HendelseUnderType DOBBELTUTBETALING = new HendelseUnderType("OKONOMI_DOBBELUTBETALING");
    HendelseUnderType FOR_MYE_UTBETALT = new HendelseUnderType("OKONOMI_UTBETALT");
    HendelseUnderType FEIL_TREKK = new HendelseUnderType("OKONOMI_FEIL_TREKK");
    HendelseUnderType FEIL_FERIEPENGER = new HendelseUnderType("OKONOMI_FEIL_FERIEPENGER");
}
