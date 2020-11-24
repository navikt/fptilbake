package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag;

import java.util.List;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;

public abstract class FellesTask {

    private FagsystemKlient fagsystemKlient;

    protected FellesTask() {
        // for CDI proxy
    }

    public FellesTask(FagsystemKlient fagsystemKlient) {
        this.fagsystemKlient = fagsystemKlient;
    }

    protected boolean finnesYtelsesbehandling(String saksnummer, Henvisning henvisning) {
        return fagsystemKlient.finnesBehandlingIFagsystem(saksnummer, henvisning);
    }

    protected List<EksternBehandlingsinfoDto> hentBehandlingerFraFagsystem(String saksnummer) {
        return fagsystemKlient.hentBehandlingForSaksnummer(saksnummer);
    }

}
