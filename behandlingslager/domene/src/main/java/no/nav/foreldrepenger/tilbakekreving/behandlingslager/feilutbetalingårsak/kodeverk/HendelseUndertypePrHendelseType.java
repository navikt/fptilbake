package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk;

import java.util.Map;
import java.util.Set;

public class HendelseUndertypePrHendelseType {

    private static final Map<HendelseType, Set<HendelseUnderType>> HIERARKI = Map.of();

    static {
        //TODO legg inn mapping her
    }

    public static Map<HendelseType, Set<HendelseUnderType>> getHendelsetypeHierarki() {
        return HIERARKI;
    }

    public static Set<HendelseUnderType> getHendelsetyper(HendelseType hendelseType) {
        if (!HIERARKI.containsKey(hendelseType)) {
            throw new IllegalArgumentException("Ikke-støttet hendelseType: " + hendelseType);
        }
        return HIERARKI.get(hendelseType);
    }


}
