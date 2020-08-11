package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;

public interface ØkonomiUndertyper {

    HendelseUnderType DOBBELTUTBETALING = HendelseUnderType.DOBBELTUTBETALING;
    HendelseUnderType FOR_MYE_UTBETALT = HendelseUnderType.FOR_MYE_UTBETALT;
    HendelseUnderType FEIL_TREKK = HendelseUnderType.FEIL_TREKK;
    HendelseUnderType FEIL_FERIEPENGER = HendelseUnderType.FEIL_FERIEPENGER;
}
