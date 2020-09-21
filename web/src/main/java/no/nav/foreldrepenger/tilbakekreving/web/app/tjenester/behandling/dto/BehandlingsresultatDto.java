package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingResultatType;

public class BehandlingsresultatDto {

    private BehandlingResultatType type = BehandlingResultatType.IKKE_FASTSATT;

    public BehandlingResultatType getType() {
        return type;
    }

    public void setType(BehandlingResultatType type) {
        this.type = type;
    }
}
