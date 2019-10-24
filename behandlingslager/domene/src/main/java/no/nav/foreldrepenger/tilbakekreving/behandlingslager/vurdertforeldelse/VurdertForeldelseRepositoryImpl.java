package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class VurdertForeldelseRepositoryImpl implements VurdertForeldelseRepository {

    private EntityManager entityManager;

    VurdertForeldelseRepositoryImpl() {
        // CDI
    }

    @Inject
    public VurdertForeldelseRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Deprecated(forRemoval = true) //bruk den andre lagre-metoden
    @Override
    public void lagre(VurdertForeldelseAggregate vurdertForeldelseAggregate) {
        disableForrigeAggregat(vurdertForeldelseAggregate.getBehandlingId());
        entityManager.persist(vurdertForeldelseAggregate.getVurdertForeldelse());
        entityManager.persist(vurdertForeldelseAggregate);
        entityManager.flush();
    }

    @Override
    public void lagre(Long behandlingId, VurdertForeldelse vurdertForeldelse) {
        disableForrigeAggregat(behandlingId);
        VurdertForeldelseAggregate aggr = VurdertForeldelseAggregate.builder()
            .medAktiv(true)
            .medBehandlingId(behandlingId)
            .medVurdertForeldelse(vurdertForeldelse)
            .build();
        entityManager.persist(vurdertForeldelse);
        entityManager.persist(aggr);
    }

    private void disableForrigeAggregat(Long behandlingId) {
        Optional<VurdertForeldelseAggregate> forrigeVurdertForeldelse = finnVurdertForeldelseForBehandling(behandlingId);
        if (forrigeVurdertForeldelse.isPresent()) {
            VurdertForeldelseAggregate forrigeVurdertForeldelseAggregate = forrigeVurdertForeldelse.get();
            forrigeVurdertForeldelseAggregate.disable();
            entityManager.persist(forrigeVurdertForeldelseAggregate);
        }
    }

    @Override
    public Optional<VurdertForeldelseAggregate> finnVurdertForeldelseForBehandling(Long behandlingId) {
        TypedQuery<VurdertForeldelseAggregate> query = entityManager.createQuery("from VurdertForeldelseAggregate aggr where aggr.behandlingId=:behandlingId " +
            "and aggr.aktiv=:aktiv", VurdertForeldelseAggregate.class);
        query.setParameter("behandlingId", behandlingId);
        query.setParameter("aktiv", true);
        return hentUniktResultat(query);
    }

    @Override
    public boolean harVurdertForeldelseForBehandlingId(Long behandlingId) {
        TypedQuery<Long> query = entityManager.createQuery("select count(1) from VurdertForeldelseAggregate aggr where aggr.behandlingId=:behandlingId " +
            "and aggr.aktiv=:aktiv", Long.class);
        query.setParameter("behandlingId", behandlingId);
        query.setParameter("aktiv", true);
        return query.getSingleResult() > 0;
    }

    @Override
    public void sletteForeldelse(Long behandlingId) {
        disableForrigeAggregat(behandlingId);
    }


}
