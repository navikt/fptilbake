package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;

public abstract class FellesTask {

    private BehandlingRepository behandlingRepository;
    private FagsystemKlient fagsystemKlient;

    protected FellesTask() {
        // for CDI proxy
    }

    public FellesTask(BehandlingRepository behandlingRepository,
                      FagsystemKlient fagsystemKlient) {
        this.behandlingRepository = behandlingRepository;
        this.fagsystemKlient = fagsystemKlient;
    }

    protected boolean finnesYtelsesbehandling(String saksnummer, Henvisning henvisning) {
        return fagsystemKlient.finnesBehandlingIFagsystem(saksnummer, henvisning);
    }

    protected List<EksternBehandlingsinfoDto> hentBehandlingerFraFagsystem(String saksnummer) {
        return fagsystemKlient.hentBehandlingForSaksnummer(saksnummer);
    }

    protected List<Behandling> hentBehandlingerForSaksnummer(String saksnummer) {
        return behandlingRepository.hentAlleBehandlingerForSaksnummer(new Saksnummer(saksnummer));
    }

    protected Optional<Behandling> finnÅpenTilbakekrevingBehandling(String saksnummer){
        List<Behandling> behandlinger = hentBehandlingerForSaksnummer(saksnummer);
        List<Behandling> åpneBehandlinger = behandlinger.stream()
            .filter(beh -> BehandlingType.TILBAKEKREVING.equals(beh.getType()))
            .filter(beh -> !beh.erAvsluttet()).collect(Collectors.toList());
        if(åpneBehandlinger.size() > 1){
            throw new IllegalArgumentException("Utvikler feil: Kan ikke ha flere åpne behandling for saksnummer="+ saksnummer);
        }
        return åpneBehandlinger.stream().findAny();
    }

}
