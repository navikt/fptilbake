package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.felles;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hibernate.jpa.HibernateHints;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;

@ApplicationScoped
public class FellesQueriesForBehandlingRepositories {

    private EntityManager entityManager;

    public FellesQueriesForBehandlingRepositories() {
        // CDI
    }

    @Inject
    public FellesQueriesForBehandlingRepositories(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Henter behandlinger på vent med autopunkter for gitte aksjonspunktdefinisjoner
     */
    public Collection<Behandling> finnVentendeBehandlingerMedAktivtAksjonspunkt(AksjonspunktDefinisjon... aksjonspunktDefinisjoner) {
        TypedQuery<Behandling> query = entityManager.createQuery("""
                        select distinct b
                          from Aksjonspunkt ap
                          inner join ap.behandling b on ap.behandling.id = b.id
                          where ap.status in (:åpneAksjonspunktKoder)
                          and ap.aksjonspunktDefinisjon in (:aksjonspunkt)
                        """,
                Behandling.class);

        setParametre(query, aksjonspunktDefinisjoner);
        return query.getResultList();
    }

    /**
     * Henter behandling, gitt at den er på vent med autopunkter for gitte aksjonspunktdefinisjoner
     */
    public Optional<Behandling> finnVentendeBehandlingMedAktivtAksjonspunkt(Long behandingId, AksjonspunktDefinisjon... aksjonspunktDefinisjoner) {
        TypedQuery<Behandling> query = entityManager.createQuery("""
                        select b
                          from Behandling b
                          where b.id = :behandlingId
                          and exists (select 1 from Aksjonspunkt ap
                                      where ap.behandling = b
                                      and ap.status in (:åpneAksjonspunktKoder)
                                      and ap.aksjonspunktDefinisjon in (:aksjonspunkt) )
                        """,
                Behandling.class);

        setParametre(query, aksjonspunktDefinisjoner);
        query.setHint(HibernateHints.HINT_READ_ONLY, "true");
        query.setParameter("behandlingId", behandingId);

        List<Behandling> resultat = query.getResultList();
        if (resultat.size() > 1) {
            throw new IllegalStateException("Fant flere enn en behandlig med id=" + behandingId);
        }
        return resultat.stream().findFirst();
    }

    private void setParametre(TypedQuery<Behandling> query, AksjonspunktDefinisjon[] aksjonspunktDefinisjoner) {
        query.setHint(HibernateHints.HINT_READ_ONLY, "true");
        query.setParameter("åpneAksjonspunktKoder", AksjonspunktStatus.getÅpneAksjonspunktStatuser());
        query.setParameter("aksjonspunkt", Arrays.asList(aksjonspunktDefinisjoner));
    }

}
