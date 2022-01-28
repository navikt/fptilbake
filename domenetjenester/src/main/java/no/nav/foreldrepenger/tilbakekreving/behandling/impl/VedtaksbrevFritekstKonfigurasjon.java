package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.util.Set;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;

public interface VedtaksbrevFritekstKonfigurasjon {

    Set<HendelseUnderType> UNDERTYPER_MED_PÅKREVD_FRITEKST = Set.of(
            HendelseUnderType.ANNET_FRITEKST,
            HendelseUnderType.ENDRING_GRUNNLAG,
            HendelseUnderType.SVP_ENDRING_GRUNNLAG
    );
}
