package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentEksaktResultat;
import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class KravgrunnlagRepository {

    private static final String BEHANDLING_ID = "behandlingId";
    private static final String AKTIV = "aktiv";
    private EntityManager entityManager;

    KravgrunnlagRepository() {
        // for CDI
    }

    @Inject
    public KravgrunnlagRepository(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Deprecated //KravgrunnlagAggregate skal ikke brukes utenfor repository. Bruk alternativ lagre-metode
    public void lagre(KravgrunnlagAggregate kravgrunnlagAggregate) {
        Optional<KravgrunnlagAggregate> forrigeGrunnlag = finnGrunnlagForBehandlingId(kravgrunnlagAggregate.getBehandlingId());
        if (forrigeGrunnlag.isPresent()) {
            forrigeGrunnlag.get().disable();
            entityManager.persist(forrigeGrunnlag.get());
        }
        entityManager.persist(kravgrunnlagAggregate.getGrunnlagØkonomi());
        entityManager.persist(kravgrunnlagAggregate);
        entityManager.flush();
    }

    public void lagre(Long behandlingId, Kravgrunnlag431 kravgrunnlag) {
        Optional<KravgrunnlagAggregate> forrigeGrunnlag = finnGrunnlagForBehandlingId(behandlingId);
        if (forrigeGrunnlag.isPresent()) {
            forrigeGrunnlag.get().disable();
            entityManager.persist(forrigeGrunnlag.get());
        }
        KravgrunnlagAggregate aggregate = new KravgrunnlagAggregate.Builder()
            .medGrunnlagØkonomi(kravgrunnlag)
            .medBehandlingId(behandlingId)
            .medAktiv(true)
            .build();
        entityManager.persist(kravgrunnlag);
        entityManager.persist(aggregate);
        entityManager.flush();
    }

    public Optional<KravgrunnlagAggregate> finnGrunnlagForBehandlingId(Long behandlingId) {
        TypedQuery<KravgrunnlagAggregate> query = formFinngrunnlagQuery(behandlingId);
        return hentUniktResultat(query);
    }

    public KravgrunnlagAggregate finnEksaktGrunnlagForBehandlingId(Long behandlingId) {
        TypedQuery<KravgrunnlagAggregate> query = formFinngrunnlagQuery(behandlingId);
        return hentEksaktResultat(query);
    }

    public boolean harGrunnlagForBehandlingId(Long behandlingId) {
        TypedQuery<Long> query = entityManager.createQuery("select count(1) from KravgrunnlagAggregate aggr " +
            "where aggr.behandlingId=:behandlingId and aggr.aktiv=:aktiv", Long.class);
        query.setParameter(BEHANDLING_ID, behandlingId);
        query.setParameter(AKTIV, true);
        return query.getSingleResult() > 0;
    }

    public Optional<KravgrunnlagAggregate> finnGrunnlagForVedtakId(Long vedtakId) {
        TypedQuery<KravgrunnlagAggregate> query = entityManager.createQuery("from KravgrunnlagAggregate aggr " +
            "where aggr.grunnlagØkonomi.vedtakId=:vedtakId and aggr.aktiv=:aktiv", KravgrunnlagAggregate.class);
        query.setParameter("vedtakId", vedtakId);
        query.setParameter(AKTIV, true);
        return hentUniktResultat(query);
    }

    public void sperrGrunnlag(Long behandlingId){
        Optional<KravgrunnlagAggregate> aggregate = finnGrunnlagForBehandlingId(behandlingId);
        if(aggregate.isPresent()){
            aggregate.get().sperr();
            entityManager.persist(aggregate.get());
            entityManager.flush();
        }
    }

    private TypedQuery<KravgrunnlagAggregate> formFinngrunnlagQuery(Long behandlingId) {
        TypedQuery<KravgrunnlagAggregate> query = entityManager.createQuery("from KravgrunnlagAggregate aggr " +
            "where aggr.behandlingId=:behandlingId and aggr.aktiv=:aktiv", KravgrunnlagAggregate.class);
        query.setParameter(BEHANDLING_ID, behandlingId);
        query.setParameter(AKTIV, true);
        return query;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
