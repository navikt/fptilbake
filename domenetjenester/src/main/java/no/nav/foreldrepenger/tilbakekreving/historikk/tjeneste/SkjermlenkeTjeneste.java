package no.nav.foreldrepenger.tilbakekreving.historikk.tjeneste;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.skjermlenke.SkjermlenkeType;

public class SkjermlenkeTjeneste {

    private SkjermlenkeTjeneste(){
    }

    public static SkjermlenkeType finnSkjermlenkeType(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        return switch (aksjonspunktDefinisjon) {
            case AVKLART_FAKTA_FEILUTBETALING -> SkjermlenkeType.FAKTA_OM_FEILUTBETALING;
            case VURDER_FORELDELSE -> SkjermlenkeType.FORELDELSE;
            case VURDER_TILBAKEKREVING -> SkjermlenkeType.TILBAKEKREVING;
            case FORESLÅ_VEDTAK -> SkjermlenkeType.VEDTAK;
            default -> SkjermlenkeType.UDEFINERT;
        };
    }
}
