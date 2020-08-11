package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;

public interface FellesUndertyper {

    HendelseUnderType IKKE_SATT = new HendelseUnderType("-", "-", null, 0);

    HendelseUnderType ANNET_FRITEKST = new HendelseUnderType("ANNET_FRITEKST", "ANNET_FRITEKST", "Annet - fritekst", 3);
    HendelseUnderType REFUSJON_ARBEIDSGIVER = new HendelseUnderType("REFUSJON_ARBGIVER", "REFUSJON_ARBGIVER", "Refusjon til arbeidsgiver", 1);
}
