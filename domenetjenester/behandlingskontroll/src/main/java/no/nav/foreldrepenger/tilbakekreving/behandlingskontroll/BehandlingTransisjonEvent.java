package no.nav.foreldrepenger.tilbakekreving.behandlingskontroll;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.behandlingskontroll.transisjoner.TransisjonIdentifikator;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingEvent;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;

public class BehandlingTransisjonEvent implements BehandlingEvent {

    private final BehandlingskontrollKontekst kontekst;
    private TransisjonIdentifikator transisjonIdentifikator;
    private Optional<BehandlingStegTilstand> fraTilstand = Optional.empty();
    private BehandlingStegType tilStegType;
    private boolean erOverhopp;

    public BehandlingTransisjonEvent(BehandlingskontrollKontekst kontekst, TransisjonIdentifikator transisjonIdentifikator, Optional<BehandlingStegTilstand> fraTilstand, BehandlingStegType tilStegType, boolean erOverhopp) {
        this.kontekst = kontekst;
        this.transisjonIdentifikator = transisjonIdentifikator;
        this.fraTilstand = fraTilstand;
        this.tilStegType = tilStegType;
        this.erOverhopp = erOverhopp;
    }

    @Override
    public Long getBehandlingId() {
        return kontekst.getBehandlingId();
    }

    @Override
    public Long getFagsakId() {
        return kontekst.getFagsakId();
    }

    @Override
    public AktørId getAktørId() {
        return kontekst.getAktørId();
    }

    public BehandlingskontrollKontekst getKontekst() {
        return kontekst;
    }

    public TransisjonIdentifikator getTransisjonIdentifikator() {
        return transisjonIdentifikator;
    }

    public Optional<BehandlingStegStatus> getFørsteStegStatus() {
        Optional<BehandlingStegTilstand> tilstand = getFraTilstand();
        return Optional.ofNullable(tilstand.isPresent() ? tilstand.get().getBehandlingStegStatus() : null);
    }

    public BehandlingStegType getFørsteSteg() {
        // siden hopper framover blir dette fraSteg
        return getFraStegType();
    }

    public BehandlingStegType getFraStegType() {
        Optional<BehandlingStegTilstand> tilstand = getFraTilstand();
        if (tilstand.isPresent()) {
            return tilstand.get().getBehandlingSteg();
        } else {
            return null;
        }
    }

    public Optional<BehandlingStegTilstand> getFraTilstand() {
        return fraTilstand;
    }

    public BehandlingStegType getSisteSteg() {
        // siden hopper framover blir dette tilSteg
        return tilStegType;
    }

    public BehandlingSteg.TransisjonType getSkalTil(){return BehandlingSteg.TransisjonType.FØR_INNGANG;}

    public boolean erOverhopp() {
        return erOverhopp;
    }
}
