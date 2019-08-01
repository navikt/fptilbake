package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface AksjonspunktKode {

    @JsonIgnore
    String getKode();
}
