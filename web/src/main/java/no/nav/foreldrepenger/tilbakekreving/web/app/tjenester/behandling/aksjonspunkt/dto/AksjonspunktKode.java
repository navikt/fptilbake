package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;

public interface AksjonspunktKode {

    @JsonIgnore
    AksjonspunktDefinisjon getAksjonspunktDefinisjon();
}
