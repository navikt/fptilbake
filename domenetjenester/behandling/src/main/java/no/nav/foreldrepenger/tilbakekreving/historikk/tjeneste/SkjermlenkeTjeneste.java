package no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste;

import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLART_FAKTA_FEILUTBETALING;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.FORESLÅ_VEDTAK;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.VURDER_FORELDELSE;
import static no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.VURDER_TILBAKEKREVING;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;

public class SkjermlenkeTjeneste {

    private SkjermlenkeTjeneste(){
    }

    public static SkjermlenkeType finnSkjermlenkeType(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        if (AVKLART_FAKTA_FEILUTBETALING.equals(aksjonspunktDefinisjon)) {
            return SkjermlenkeType.FAKTA_OM_FEILUTBETALING;
        } else if (VURDER_FORELDELSE.equals(aksjonspunktDefinisjon)) {
            return SkjermlenkeType.FORELDELSE;
        } else if (VURDER_TILBAKEKREVING.equals(aksjonspunktDefinisjon)) {
            return SkjermlenkeType.TILBAKEKREVING;
        } else if (FORESLÅ_VEDTAK.equals(aksjonspunktDefinisjon)) {
            return SkjermlenkeType.VEDTAK;
        }
        return SkjermlenkeType.UDEFINERT;
    }
}
