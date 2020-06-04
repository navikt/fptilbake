package no.nav.foreldrepenger.tilbakekreving.behandling.steg.hentgrunnlag;

import java.util.Optional;

import no.nav.foreldrepenger.tilbakekreving.fpsak.klient.FpsakKlient;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagAggregate;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;

public abstract class FellesTask {

    private KravgrunnlagRepository grunnlagRepository;
    private FpsakKlient fpsakKlient;

    protected FellesTask() {
        // for CDI proxy
    }

    public FellesTask(KravgrunnlagRepository grunnlagRepository, FpsakKlient fpsakKlient) {
        this.grunnlagRepository = grunnlagRepository;
        this.fpsakKlient = fpsakKlient;
    }

    protected boolean erGyldigTall(String referanse) {
        return referanse != null && referanse.matches("^\\d*$");
    }

    protected boolean erBehandlingFinnesIFpsak(String saksnummer, String henvisning) {
        //FIXME k9-tilbake må begge formater for henvsning. Antar enklest å delegere til spesifikk implementasjon av saksbehandlingKlient
        return fpsakKlient.finnesBehandlingIFpsak(saksnummer,Long.valueOf(henvisning));
    }

    protected String finnSaksnummer(String fagsystemId) {
        //FIXME k9-tilbake Støtte begge formater
        return fagsystemId.substring(0, fagsystemId.length() - 3);
    }

    protected Optional<KravgrunnlagAggregate> finnGrunnlagForVedtakId(long vedtakId) {
        return grunnlagRepository.finnGrunnlagForVedtakId(vedtakId);
    }

}
