package no.nav.foreldrepenger.tilbakekreving.simulering.tjeneste;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.simulering.klient.FpOppdragRestKlient;
import no.nav.foreldrepenger.tilbakekreving.simulering.kontrakt.SimuleringResultatDto;

@ApplicationScoped
public class SimuleringIntegrasjonTjeneste {

    private FpOppdragRestKlient restKlient;

    public SimuleringIntegrasjonTjeneste() {
        // CDI
    }

    @Inject
    public SimuleringIntegrasjonTjeneste(FpOppdragRestKlient restKlient) {
        this.restKlient = restKlient;
    }

    public Optional<SimuleringResultatDto> hentResultat(long behandlingId) {
        return restKlient.hentResultat(behandlingId);
    }

}
