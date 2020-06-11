package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;
import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FagsystemKlient;
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
        return fagsystemKlient.finnesBehandlingIFpsak(saksnummer, henvisning);
    }

    protected String finnSaksnummer(String fagsystemId) {
        //FIXME k9-tilbake Støtte begge formater
        return fagsystemId.substring(0, fagsystemId.length() - 3);
    }

    protected Optional<KravgrunnlagAggregate> finnGrunnlagForVedtakId(long vedtakId) {
        return grunnlagRepository.finnGrunnlagForVedtakId(vedtakId);
    }

}
