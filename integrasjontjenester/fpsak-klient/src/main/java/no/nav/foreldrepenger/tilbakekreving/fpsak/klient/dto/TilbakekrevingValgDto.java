package no.nav.foreldrepenger.tilbakekreving.fpsak.klient.dto;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.tilbakekrevingsvalg.VidereBehandling;

public class TilbakekrevingValgDto {

    private VidereBehandling videreBehandling;

    public TilbakekrevingValgDto() {}

    public TilbakekrevingValgDto(VidereBehandling videreBehandling) {
        this.videreBehandling = videreBehandling;
    }

    public VidereBehandling getVidereBehandling() {
        return videreBehandling;
    }

    public boolean harTilbakekrevingValg() {
        return videreBehandling != null;
    }

}
