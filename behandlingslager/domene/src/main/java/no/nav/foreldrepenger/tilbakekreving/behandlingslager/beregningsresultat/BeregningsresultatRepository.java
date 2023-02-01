package no.nav.foreldrepenger.tilbakekreving.behandlingslager.beregningsresultat;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;

@Dependent
public class BeregningsresultatRepository {

    private EntityManager entityManager;

    @Inject
    public BeregningsresultatRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void lagre(Behandling behandling, Beregningsresultat beregningsresultat){
        lagre(behandling.getId(), beregningsresultat);
    }

    public void lagre(Long behandlingId, Beregningsresultat beregningsresultat){
        Optional<BeregningsresultatAggregate> gammelKobling = hentBeregningsresultatKobling(behandlingId);
        Beregningsresultat gammeltResultat = gammelKobling.map(BeregningsresultatAggregate::getBeregningsresultat).orElse(null);
        if (!Objects.equals(gammeltResultat, beregningsresultat)){
            gammelKobling.ifPresent(this::deaktiverBeregningsresultat);
            BeregningsresultatAggregate kobling = new BeregningsresultatAggregate(behandlingId, beregningsresultat);
            entityManager.persist(beregningsresultat);
            entityManager.persist(kobling);
            entityManager.flush();
        }
    }

    public Optional<Beregningsresultat> hentHvisEksisterer(Long behandlingId) {
        final Optional<BeregningsresultatAggregate> resultat = hentBeregningsresultatKobling(behandlingId);
        return resultat.map(BeregningsresultatAggregate::getBeregningsresultat);
    }

    private Optional<BeregningsresultatAggregate> hentBeregningsresultatKobling(Long behandlingId) {
        var query = entityManager.createQuery("SELECT vr " +
            "FROM BeregningsresultatAggregate vr " +
            "WHERE vr.behandlingId = :behandlingId " +
            "AND vr.aktiv = true", BeregningsresultatAggregate.class);
        query.setParameter("behandlingId", behandlingId);

        return HibernateVerktøy.hentUniktResultat(query);
    }

    public void deaktiverBeregningsresultat(BeregningsresultatAggregate beregningsresultat) {
        beregningsresultat.setAktiv(false);
        entityManager.persist(beregningsresultat);
        entityManager.flush();
    }

}
