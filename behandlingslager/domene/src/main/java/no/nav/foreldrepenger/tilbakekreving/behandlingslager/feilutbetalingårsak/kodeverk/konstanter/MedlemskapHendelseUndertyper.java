package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;

public interface MedlemskapHendelseUndertyper {

    HendelseUnderType UTVANDRET = new HendelseUnderType("UTVANDRET");
    HendelseUnderType IKKE_LOVLIG_OPPHOLD = new HendelseUnderType("IKKE_LOVLIG_OPPHOLD");
    HendelseUnderType MEDLEM_I_ANNET_LAND = new HendelseUnderType("MEDLEM_I_ANNET_LAND");
    HendelseUnderType IKKE_OPPHOLDSRETT_EØS = new HendelseUnderType("IKKE_OPPHOLDSRETT_EØS");
    HendelseUnderType IKKE_BOSATT = new HendelseUnderType("IKKE_BOSATT");
}
