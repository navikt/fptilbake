package no.nav.foreldrepenger.tilbakekreving.web.app.tjenester.behandling.dto;

import no.nav.foreldrepenger.tilbakekreving.behandling.modell.BehandlingFeilutbetalingFakta;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class BehandlingFeilutbetalingFaktaDto implements AbacDto {

    private BehandlingFeilutbetalingFakta behandlingFakta;

    public BehandlingFeilutbetalingFakta getBehandlingFakta() {
        return behandlingFakta;
    }

    public void setBehandlingFakta(BehandlingFeilutbetalingFakta behandlingFakta) {
        this.behandlingFakta = behandlingFakta;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();
    }
}
