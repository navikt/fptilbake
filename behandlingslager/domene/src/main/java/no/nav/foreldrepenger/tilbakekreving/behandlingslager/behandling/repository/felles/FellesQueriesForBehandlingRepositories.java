package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.felles;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.ReaktiveringStatus;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class FellesQueriesForBehandlingRepositories {

    private EntityManager entityManager;

    public FellesQueriesForBehandlingRepositories() {
        // CDI
    }

    @Inject
    public FellesQueriesForBehandlingRepositories(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Henter behandlinger på vent med autopunkter for gitt aksjonspunktdefinisjon
     * @param aksjonspunktDefinisjon - aksjonspunktdefinisjon for ønsket aksjonspunkt
     * @return liste med behandlinger
     */
    public List<Behandling> finnVentendeBehandlingMedAktivtAksjonspunkt(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        List<String> åpneAksjonspunktKoder = AksjonspunktStatus.getÅpneAksjonspunktKoder();
        String autopunktkode = AksjonspunktType.AUTOPUNKT.getKode();
        String reaktiverkode = ReaktiveringStatus.AKTIV.getKode();

        TypedQuery<Behandling> query = entityManager.createQuery(
                "select distinct b" +
                        " from Aksjonspunkt ap" +
                        " inner join ap.behandling b on ap.behandling.id = b.id" +
                        " where ap.status.kode = :åpneAksjonspunktKoder" +
                        " and ap.reaktiveringStatus.kode = :reaktiverkode" +
                        " and ap.aksjonspunktDefinisjon.aksjonspunktType.kode = :autopunktkode" +
                        " and ap.aksjonspunktDefinisjon.kode = :køetKode",
                Behandling.class);

        query.setHint(QueryHints.HINT_READONLY, "true");
        query.setParameter("åpneAksjonspunktKoder", åpneAksjonspunktKoder);
        query.setParameter("autopunktkode", autopunktkode);
        query.setParameter("reaktiverkode", reaktiverkode);
        query.setParameter("køetKode", aksjonspunktDefinisjon.getKode());

        return query.getResultList();
    }

}
