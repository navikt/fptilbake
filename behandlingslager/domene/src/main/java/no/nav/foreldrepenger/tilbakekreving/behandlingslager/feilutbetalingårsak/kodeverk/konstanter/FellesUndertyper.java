package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;

public interface FellesUndertyper {

    HendelseUnderType IKKE_SATT = new HendelseUnderType("-");

    HendelseUnderType ANNET_FRITEKST = new HendelseUnderType("ANNET_FRITEKST");
    HendelseUnderType REFUSJON_ARBEIDSGIVER = new HendelseUnderType("REFUSJON_ARBGIVER");
}
