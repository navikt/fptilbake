package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.FagsystemKlient;
import no.nav.foreldrepenger.tilbakekreving.fagsystem.klient.dto.EksternBehandlingsinfoDto;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagAggregate;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;

public abstract class FellesTask {

    private KravgrunnlagRepository grunnlagRepository;
    private FagsystemKlient fagsystemKlient;

    protected FellesTask() {
        // for CDI proxy
    }

    public FellesTask(KravgrunnlagRepository grunnlagRepository, FagsystemKlient fagsystemKlient) {
        this.grunnlagRepository = grunnlagRepository;
        this.fagsystemKlient = fagsystemKlient;
    }

    protected boolean finnesYtelsesbehandling(String saksnummer, Henvisning henvisning) {
        return fagsystemKlient.finnesBehandlingIFagsystem(saksnummer, henvisning);
    }

    protected List<EksternBehandlingsinfoDto> hentBehandlingerFraFagsystem(String saksnummer) {
        return fagsystemKlient.hentBehandlingForSaksnummer(saksnummer);
    }

    protected Optional<KravgrunnlagAggregate> finnGrunnlagForVedtakId(long vedtakId) {
        return grunnlagRepository.finnGrunnlagForVedtakId(vedtakId);
    }

}
