package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

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
