package no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;

/**
 * Undertyper som p.t. ikke kan velges, men som er brukt i produksjonsmiljøet.
 * <p>
 * Disse kan ikke fjernes uten at tidligere bruk evt. migreres til andre koder. Hvis ikke vil oppslag/innsyn i behandlingen feile
 */
public interface LegacyUnderTyper {

    HendelseUnderType LEGACY_ØKONOMI_UTBETALT_FOR_MYE = new HendelseUnderType("ØKONOMI_UTBETALT_FOR_MYE", "ØKONOMI_UTBETALT_FOR_MYE", "Feil i økonomi - utbetalt for mye", 1);

}
