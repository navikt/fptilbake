package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;

public interface MedlemskapHendelseUndertyper {

    HendelseUnderType UTVANDRET = HendelseUnderType.UTVANDRET;
    HendelseUnderType IKKE_LOVLIG_OPPHOLD = HendelseUnderType.IKKE_LOVLIG_OPPHOLD;
    HendelseUnderType MEDLEM_I_ANNET_LAND = HendelseUnderType.MEDLEM_I_ANNET_LAND;
    HendelseUnderType IKKE_OPPHOLDSRETT_EØS = HendelseUnderType.IKKE_OPPHOLDSRETT_EØS;
    HendelseUnderType IKKE_BOSATT = HendelseUnderType.IKKE_BOSATT;
}
