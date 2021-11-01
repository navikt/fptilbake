package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll;


import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;

public record BehandlingStegUtfall(BehandlingStegType behandlingStegType, BehandlingStegStatus resultat) {
}
