package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;


public class BehandlingStegTilstandEndringEvent implements BehandlingEvent {

    private Optional<BehandlingStegTilstand> fraTilstand;
    private Optional<BehandlingStegTilstand> tilTilstand;

    private BehandlingskontrollKontekst kontekst;


    public BehandlingStegTilstandEndringEvent(BehandlingskontrollKontekst kontekst, Optional<BehandlingStegTilstand> forrigeTilstand){
        super();
        this.kontekst = kontekst;
        this.fraTilstand = forrigeTilstand;
    }

    public void setNyTilstand(Optional<BehandlingStegTilstand> nyTilstand){
        this.tilTilstand=nyTilstand;
    }

    @Override
    public Long getFagsakId() {
        return kontekst.getFagsakId();
    }

    @Override
    public AktørId getAktørId() {
        return kontekst.getAktørId();
    }

    @Override
    public Long getBehandlingId() {
        return kontekst.getBehandlingId();
    }

    public Optional<BehandlingStegTilstand> getFraTilstand() {
        return fraTilstand;
    }

    public Optional<BehandlingStegTilstand> getTilTilstand() {
        return tilTilstand;
    }
}
