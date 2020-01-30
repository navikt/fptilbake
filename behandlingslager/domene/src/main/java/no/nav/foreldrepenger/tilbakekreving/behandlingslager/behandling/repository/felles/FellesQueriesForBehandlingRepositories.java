package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.felles;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * Henter behandlinger på vent med autopunkter for gitte aksjonspunktdefinisjoner
     */
    public Collection<Behandling> finnVentendeBehandlingerMedAktivtAksjonspunkt(AksjonspunktDefinisjon... aksjonspunktDefinisjoner) {
        List<String> åpneAksjonspunktKoder = AksjonspunktStatus.getÅpneAksjonspunktKoder();
        String autopunktkode = AksjonspunktType.AUTOPUNKT.getKode();
        String reaktiverkode = ReaktiveringStatus.AKTIV.getKode();

        List<String> aksjonspunktKoder = Arrays.stream(aksjonspunktDefinisjoner).map(AksjonspunktDefinisjon::getKode).collect(Collectors.toList());

        TypedQuery<Behandling> query = entityManager.createQuery(
            "select distinct b" +
                " from Aksjonspunkt ap" +
                " inner join ap.behandling b on ap.behandling.id = b.id" +
                " where ap.status.kode = :åpneAksjonspunktKoder" +
                " and ap.reaktiveringStatus.kode = :reaktiverkode" +
                " and ap.aksjonspunktDefinisjon.aksjonspunktType.kode = :autopunktkode" +
                " and ap.aksjonspunktDefinisjon.kode in (:køetKode)",
            Behandling.class);

        query.setHint(QueryHints.HINT_READONLY, "true");
        query.setParameter("åpneAksjonspunktKoder", åpneAksjonspunktKoder);
        query.setParameter("autopunktkode", autopunktkode);
        query.setParameter("reaktiverkode", reaktiverkode);
        query.setParameter("køetKode", aksjonspunktKoder);

        return query.getResultList();
    }

    /**
     * Henter behandling, gitt at den er på vent med autopunkter for gitte aksjonspunktdefinisjoner
     */
    public Optional<Behandling> finnVentendeBehandlingMedAktivtAksjonspunkt(Long behandingId, AksjonspunktDefinisjon... aksjonspunktDefinisjoner) {
        List<String> åpneAksjonspunktKoder = AksjonspunktStatus.getÅpneAksjonspunktKoder();
        String autopunktkode = AksjonspunktType.AUTOPUNKT.getKode();
        String reaktiverkode = ReaktiveringStatus.AKTIV.getKode();

        List<String> aksjonspunktKoder = Arrays.stream(aksjonspunktDefinisjoner).map(AksjonspunktDefinisjon::getKode).collect(Collectors.toList());

        TypedQuery<Behandling> query = entityManager.createQuery(
            "select b" +
                " from Aksjonspunkt ap" +
                " inner join ap.behandling b on ap.behandling.id = b.id" +
                " where ap.status.kode = :åpneAksjonspunktKoder" +
                " and ap.reaktiveringStatus.kode = :reaktiverkode" +
                " and ap.aksjonspunktDefinisjon.aksjonspunktType.kode = :autopunktkode" +
                " and ap.aksjonspunktDefinisjon.kode in (:køetKode)" +
                " and b.id = :behandlingId",
            Behandling.class);

        query.setHint(QueryHints.HINT_READONLY, "true");
        query.setParameter("behandlingId", behandingId);
        query.setParameter("åpneAksjonspunktKoder", åpneAksjonspunktKoder);
        query.setParameter("autopunktkode", autopunktkode);
        query.setParameter("reaktiverkode", reaktiverkode);
        query.setParameter("køetKode", aksjonspunktKoder);

        List<Behandling> resultat = query.getResultList();
        if (resultat.size() > 1) {
            throw new IllegalStateException("Fant flere enn en behandlig med id=" + behandingId);
        }
        return resultat.stream().findFirst();
    }

}
