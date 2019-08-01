package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.aksjonspunkt.oppdaterer;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;


public interface AksjonspunktOppdaterer<T> {

    void oppdater(T dto, Behandling behandling);

}
