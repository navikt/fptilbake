package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;

public record HendelseMedUndertype(HendelseType hendelseType,
                                   HendelseUnderType hendelseUnderType) {
}
