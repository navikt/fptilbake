package no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import org.hibernate.KeyType;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepositoryFeil;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

@ApplicationScoped
public class FagsakLåsRepository {

    private EntityManager entityManager;

    FagsakLåsRepository() {
    }

    @Inject
    public FagsakLåsRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Tar lås på underliggende rader
     *
     * @param saksnummer fagsaken
     * @return låsen
     */
    public FagsakLås taLås(final Saksnummer saksnummer) {
        Fagsak fagsak = entityManager.find(Fagsak.class, saksnummer, KeyType.NATURAL, LockModeType.PESSIMISTIC_WRITE);
        if (fagsak == null) {
            throw BehandlingRepositoryFeil.fantIkkeEntitetForLåsing(Fagsak.class.getSimpleName(), saksnummer);
        }
        return new FagsakLås(saksnummer);
    }

    /**
     * Verifisering av lås
     *
     * @param lås låsen
     */
    public void oppdaterLåsVersjon(FagsakLås lås) {
        verifisertLås(lås.saksnummer());
    }

    private void verifisertLås(Saksnummer saksnummer) {
        var fagsak = entityManager.find(Fagsak.class, saksnummer, KeyType.NATURAL, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
        if (fagsak == null) {
            throw BehandlingRepositoryFeil.fantIkkeEntitetForLåsing(Fagsak.class.getSimpleName(), saksnummer);
        }
    }
}
