package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

@ApplicationScoped
public class SlettGrunnlagEventPubliserer {

    private Event<SlettGrunnlagEvent> slettKravgrunnlagEvent;

    SlettGrunnlagEventPubliserer() {
        // for CDI
    }

    @Inject
    public SlettGrunnlagEventPubliserer(Event<SlettGrunnlagEvent> slettKravgrunnlagEvent) {
        this.slettKravgrunnlagEvent = slettKravgrunnlagEvent;
    }

    public void fireEvent(Long behandlingId) {
        SlettGrunnlagEvent endretKravgrunnlag = new SlettGrunnlagEvent(behandlingId);
        slettKravgrunnlagEvent.fire(endretKravgrunnlag);
    }

}
