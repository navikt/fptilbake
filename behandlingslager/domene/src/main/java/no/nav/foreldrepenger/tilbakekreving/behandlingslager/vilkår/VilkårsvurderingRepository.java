package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vilkår;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class VilkårsvurderingRepository {

    private EntityManager entityManager;

    VilkårsvurderingRepository() {
        // for CDI
    }

    @Inject
    public VilkårsvurderingRepository(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    @Deprecated(forRemoval = true) // bruk heller metode som har signatur (behandingId, VilkårVurdering)
    public void lagre(VilkårVurderingAggregateEntitet aggregateEntitet) {
        Long behandlingId = aggregateEntitet.getBehandlingId();
        disableForrigeVurdering(behandlingId);
        lagreVilkårVurdering(aggregateEntitet.getManuellVilkår());
        entityManager.persist(aggregateEntitet);
        entityManager.flush();
    }

    public void lagre(Long behandlingId, VilkårVurderingEntitet vilkårVurdering) {
        disableForrigeVurdering(behandlingId);
        lagreVilkårVurdering(vilkårVurdering);
        lagreAggregat(behandlingId, vilkårVurdering);
    }

    private void lagreAggregat(Long behandlingId, VilkårVurderingEntitet vilkårVurdering) {
        VilkårVurderingAggregateEntitet aggregatEntitet = new VilkårVurderingAggregateEntitet.Builder()
            .medBehandlingId(behandlingId)
            .medManuellVilkår(vilkårVurdering)
            .medAktiv(true)
            .build();
        entityManager.persist(aggregatEntitet);
        entityManager.flush();
    }

    private void disableForrigeVurdering(Long behandlingId) {
        Optional<VilkårVurderingAggregateEntitet> forrigeAggregateEntitet = finnVilkårsvurderingForBehandlingId(behandlingId);
        forrigeAggregateEntitet.ifPresent(sisteAggregate -> {
            sisteAggregate.disable();
            entityManager.persist(sisteAggregate);
        });
    }

    private void lagreVilkårVurdering(VilkårVurderingEntitet vilkårVurdering) {
        entityManager.persist(vilkårVurdering);
        for (VilkårVurderingPeriodeEntitet periodeEntitet : vilkårVurdering.getPerioder()) {
            entityManager.persist(periodeEntitet);
            if (periodeEntitet.getGodTro() != null) {
                entityManager.persist(periodeEntitet.getGodTro());
            } else {
                entityManager.persist(periodeEntitet.getAktsomhet());
                for (VilkårVurderingSærligGrunnEntitet særligGrunnEntitet : periodeEntitet.getAktsomhet().getSærligGrunner()) {
                    entityManager.persist(særligGrunnEntitet);
                }
            }
        }
    }

    public Optional<VilkårVurderingEntitet> finnVilkårsvurdering(Long behandlingId) {
        return finnVilkårsvurderingForBehandlingId(behandlingId).map(VilkårVurderingAggregateEntitet::getManuellVilkår);
    }

    //TODO AggregatEntiteten skal bare benyttes i repository, ikke deles ut til andre deler av koden
    public Optional<VilkårVurderingAggregateEntitet> finnVilkårsvurderingForBehandlingId(Long behandlingId) {
        TypedQuery<VilkårVurderingAggregateEntitet> query = entityManager.createQuery("from VilkårVurderingAggregate aggr " +
            "where aggr.behandlingId=:behandlingId and aggr.aktiv=:aktiv", VilkårVurderingAggregateEntitet.class);
        query.setParameter("behandlingId", behandlingId);
        query.setParameter("aktiv", true);
        return hentUniktResultat(query);
    }

    public void slettVilkårsvurdering(Long behandlingId) {
        Optional<VilkårVurderingAggregateEntitet> vilkårVurderingAggregateEntitet = finnVilkårsvurderingForBehandlingId(behandlingId);
        vilkårVurderingAggregateEntitet.ifPresent(aggregateEntitet -> {
            aggregateEntitet.disable();
            entityManager.persist(aggregateEntitet);
            entityManager.flush();
        });
    }


}
