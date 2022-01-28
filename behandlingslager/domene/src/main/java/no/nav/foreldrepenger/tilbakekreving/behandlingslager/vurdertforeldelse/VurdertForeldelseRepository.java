package no.nav.foreldrepenger.tilbakekreving.behandlingslager.vurdertforeldelse;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

@ApplicationScoped
public class VurdertForeldelseRepository {

    private EntityManager entityManager;

    VurdertForeldelseRepository() {
        // CDI
    }

    @Inject
    public VurdertForeldelseRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<VurdertForeldelse> finnVurdertForeldelse(Long behandlingId) {
        return finnVurdertForeldelseForBehandling(behandlingId)
                .map(VurdertForeldelseAggregate::getVurdertForeldelse);
    }

    public Optional<Long> finnVurdertForeldelseAggregateId(Long behandlingId) {
        return finnVurdertForeldelseForBehandling(behandlingId)
                .map(VurdertForeldelseAggregate::getId);
    }

    public boolean harVurdertForeldelseForBehandlingId(Long behandlingId) {
        TypedQuery<Long> query = entityManager.createQuery("select count(1) from VurdertForeldelseAggregate aggr where aggr.behandlingId=:behandlingId " +
                "and aggr.aktiv=:aktiv", Long.class);
        query.setParameter("behandlingId", behandlingId);
        query.setParameter("aktiv", true);
        return query.getSingleResult() > 0;
    }

    public void lagre(Long behandlingId, VurdertForeldelse vurdertForeldelse) {
        disableForrigeAggregat(behandlingId);
        VurdertForeldelseAggregate aggr = VurdertForeldelseAggregate.builder()
                .medAktiv(true)
                .medBehandlingId(behandlingId)
                .medVurdertForeldelse(vurdertForeldelse)
                .build();
        entityManager.persist(vurdertForeldelse);
        for (VurdertForeldelsePeriode periode : vurdertForeldelse.getVurdertForeldelsePerioder()) {
            entityManager.persist(periode);
        }
        entityManager.persist(aggr);
    }

    public void slettForeldelse(Long behandlingId) {
        disableForrigeAggregat(behandlingId);
    }

    private void disableForrigeAggregat(Long behandlingId) {
        Optional<VurdertForeldelseAggregate> forrigeVurdertForeldelse = finnVurdertForeldelseForBehandling(behandlingId);
        if (forrigeVurdertForeldelse.isPresent()) {
            VurdertForeldelseAggregate forrigeVurdertForeldelseAggregate = forrigeVurdertForeldelse.get();
            forrigeVurdertForeldelseAggregate.disable();
            entityManager.persist(forrigeVurdertForeldelseAggregate);
        }
    }

    private Optional<VurdertForeldelseAggregate> finnVurdertForeldelseForBehandling(Long behandlingId) {
        TypedQuery<VurdertForeldelseAggregate> query = entityManager.createQuery("from VurdertForeldelseAggregate aggr where aggr.behandlingId=:behandlingId " +
                "and aggr.aktiv=:aktiv", VurdertForeldelseAggregate.class);
        query.setParameter("behandlingId", behandlingId);
        query.setParameter("aktiv", true);
        return hentUniktResultat(query);
    }


}
