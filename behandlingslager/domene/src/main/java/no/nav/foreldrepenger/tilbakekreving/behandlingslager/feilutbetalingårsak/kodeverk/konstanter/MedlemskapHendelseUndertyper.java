package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;

public interface MedlemskapHendelseUndertyper {

    HendelseUnderType UTVANDRET = new HendelseUnderType("UTVANDRET","UTVANDRET","Utvandret",0);
    HendelseUnderType IKKE_LOVLIG_OPPHOLD = new HendelseUnderType("IKKE_LOVLIG_OPPHOLD","IKKE_LOVLIG_OPPHOLD","Ikke lovlig opphold",3);
    HendelseUnderType MEDLEM_I_ANNET_LAND = new HendelseUnderType("MEDLEM_I_ANNET_LAND","MEDLEM_I_ANNET_LAND","Unntak medlemskap/medlem annet land",4);
    HendelseUnderType IKKE_OPPHOLDSRETT_EØS = new HendelseUnderType("IKKE_OPPHOLDSRETT_EØS","IKKE_OPPHOLDSRETT_EØS","Ikke oppholdsrett EØS",2);
    HendelseUnderType IKKE_BOSATT = new HendelseUnderType("IKKE_BOSATT","IKKE_BOSATT","Ikke bosatt",1);
}
