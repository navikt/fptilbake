package no.nav.foreldrepenger.tilbakekreving.behandling.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.automatisk.gjenoppta.tjeneste.GjenopptaBehandlingTjeneste;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.Kravgrunnlag431;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagAggregate;
import no.nav.foreldrepenger.tilbakekreving.grunnlag.KravgrunnlagRepository;

@ApplicationScoped
public class KravgrunnlagTjeneste {

    private KravgrunnlagRepository kravgrunnlagRepository;
    private GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste;

    KravgrunnlagTjeneste() {
        // For CDI
    }

    @Inject
    public KravgrunnlagTjeneste(KravgrunnlagRepository kravgrunnlagRepository, GjenopptaBehandlingTjeneste gjenopptaBehandlingTjeneste) {
        this.kravgrunnlagRepository = kravgrunnlagRepository;
        this.gjenopptaBehandlingTjeneste = gjenopptaBehandlingTjeneste;
    }

    public void lagreTilbakekrevingsgrunnlagFraØkonomi(Long behandlingId, Kravgrunnlag431 kravgrunnlag431) {
        KravgrunnlagAggregate kravgrunnlagAggregate = KravgrunnlagAggregate.builder().medBehandlingId(behandlingId)
            .medGrunnlagØkonomi(kravgrunnlag431).build();
        kravgrunnlagRepository.lagre(kravgrunnlagAggregate);
        gjenopptaBehandlingTjeneste.fortsettBehandlingMedGrunnlag(behandlingId);
    }

}
