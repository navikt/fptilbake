package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import java.util.Set;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.FellesUndertyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.FpHendelseUnderTyper;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.konstanter.SvpHendelseUnderTyper;

public interface VedtaksbrevFritekstKonfigurasjon {

    Set<HendelseUnderType> UNDERTYPER_MED_PÅKREVD_FRITEKST = Set.of(
        FellesUndertyper.ANNET_FRITEKST,
        FpHendelseUnderTyper.ENDRING_GRUNNLAG,
        SvpHendelseUnderTyper.SVP_ENDRING_GRUNNLAG
    );
}
