package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@ApplicationScoped
public class SlettGrunnlagEventPubliserer {

    private Event<KravgrunnlagEndretEvent> slettKravgrunnlagEvent;

    SlettGrunnlagEventPubliserer() {
        // for CDI
    }

    @Inject
    public SlettGrunnlagEventPubliserer(Event<KravgrunnlagEndretEvent> slettKravgrunnlagEvent) {
        this.slettKravgrunnlagEvent = slettKravgrunnlagEvent;
    }

    public void fireKravgrunnlagEndretEvent(Long behandlingId) {
        KravgrunnlagEndretEvent endretKravgrunnlag = new KravgrunnlagEndretEvent(behandlingId);
        slettKravgrunnlagEvent.fire(endretKravgrunnlag);
    }

}
