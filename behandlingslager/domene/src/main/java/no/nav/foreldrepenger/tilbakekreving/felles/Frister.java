package no.nav.foreldrepenger.tilbakekreving.felles;

import java.time.Period;

public class Frister {
    private Frister() {
    }

    // Vent på tilsvar fra bruker - behandling + oppgitt til bruker
    public static final Period BEHANDLING_TILSVAR = Period.ofWeeks(3);
    public static final Period BRUKER_TILSVAR = Period.ofWeeks(2);

    // Kravgrunnlag - første og når grunnlaget er gammelt nok til å plukkes eller automatisk behandles
    public static final Period KRAVGRUNNLAG_FØRSTE = Period.ofWeeks(4);
    public static final Period KRAVGRUNNLAG_ALDER_GAMMELT = Period.ofWeeks(8);

    // Default frist dersom ikke annen angitt
    public static final Period BEHANDLING_DEFAULT = Period.ofWeeks(4);

}
