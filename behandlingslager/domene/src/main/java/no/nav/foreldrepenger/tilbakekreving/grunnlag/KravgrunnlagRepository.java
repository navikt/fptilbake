package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentEksaktResultat;
import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class KravgrunnlagRepository {

    private static final Logger logger = LoggerFactory.getLogger(KravgrunnlagRepository.class);

    private static final String BEHANDLING_ID = "behandlingId";
    private static final String AKTIV = "aktiv";
    private EntityManager entityManager;

    KravgrunnlagRepository() {
        // for CDI
    }

    @Inject
    public KravgrunnlagRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void lagre(Long behandlingId, Kravgrunnlag431 kravgrunnlag) {
        Optional<KravgrunnlagAggregateEntity> forrigeKravgrunnlag = finnKravgrunnlagOptional(behandlingId);
        if (forrigeKravgrunnlag.isPresent()) {
            forrigeKravgrunnlag.get().disable();
            entityManager.persist(forrigeKravgrunnlag.get());
        }
        lagreNyttKravgrunnlag(behandlingId, kravgrunnlag);
    }

    public Kravgrunnlag431 hentIsAktivFor(Long behandlingId) {
        TypedQuery<Kravgrunnlag431> query = getEntityManager().createQuery("""
            SELECT kg431 FROM KravgrunnlagAggregateEntity grunnlag JOIN grunnlag.grunnlagØkonomi kg431
                WHERE grunnlag.behandlingId = :behandlingId
                AND grunnlag.aktiv = true""", Kravgrunnlag431.class);
        query.setParameter("behandlingId", behandlingId);
        return hentEksaktResultat(query);
    }

    public boolean finnesIsAktivFor(Long behandlingId) {
        return Optional.ofNullable(hentIsAktivFor(behandlingId)).isPresent();
    }

    public void lagreOgFiksDuplikateKravgrunnlag(Long behandlingId, Kravgrunnlag431 kravgrunnlag) {
        List<KravgrunnlagAggregateEntity> forrigeKravgrunnlag = finnAktiveKravgrunnlag(behandlingId);
        for (KravgrunnlagAggregateEntity forrige : forrigeKravgrunnlag) {
            forrige.disable();
            entityManager.persist(forrige);
            if (forrigeKravgrunnlag.size() > 1) {
                logger.warn("Fikser duplikate kravgrunnlag, disabler kravgrunnlagAggregateEntity {} for behandling {}", forrige.getId(), behandlingId);
            }
        }
        lagreNyttKravgrunnlag(behandlingId, kravgrunnlag);
    }

    private void lagreNyttKravgrunnlag(Long behandlingId, Kravgrunnlag431 kravgrunnlag) {
        KravgrunnlagAggregateEntity aggregate = new KravgrunnlagAggregateEntity.Builder()
                .medGrunnlagØkonomi(kravgrunnlag)
                .medBehandlingId(behandlingId)
                .medAktiv(true)
                .build();
        entityManager.persist(kravgrunnlag);
        for (KravgrunnlagPeriode432 periode432 : kravgrunnlag.getPerioder()) {
            entityManager.persist(periode432);
            for (KravgrunnlagBelop433 belop433 : periode432.getKravgrunnlagBeloper433()) {
                entityManager.persist(belop433);
            }
        }
        entityManager.persist(aggregate);
        entityManager.flush(); //TODO unngå flush i repository, det er typisk bare nyttig for tester
    }

    public Kravgrunnlag431 finnKravgrunnlag(Long behandlingId) {
        TypedQuery<KravgrunnlagAggregateEntity> query = lagFinnKravgrunnlagQuery(behandlingId);
        KravgrunnlagAggregateEntity aggregate = hentEksaktResultat(query);
        return aggregate.getGrunnlagØkonomi();
    }

    public Optional<Kravgrunnlag431> finnKravgrunnlagOpt(Long behandlingId) {
        TypedQuery<KravgrunnlagAggregateEntity> query = lagFinnKravgrunnlagQuery(behandlingId);
        Optional<KravgrunnlagAggregateEntity> aggregate = hentUniktResultat(query);
        return aggregate.map(KravgrunnlagAggregateEntity::getGrunnlagØkonomi);
    }

    public boolean harGrunnlagForBehandlingId(Long behandlingId) {
        TypedQuery<Long> query = entityManager.createQuery("select count(1) from KravgrunnlagAggregateEntity aggr " +
                "where aggr.behandlingId=:behandlingId and aggr.aktiv=:aktiv", Long.class);
        query.setParameter(BEHANDLING_ID, behandlingId);
        query.setParameter(AKTIV, true);
        return query.getSingleResult() > 0;
    }

    public boolean erKravgrunnlagSperret(Long behandlingId) {
        TypedQuery<KravgrunnlagAggregateEntity> query = lagFinnKravgrunnlagQuery(behandlingId);
        Optional<KravgrunnlagAggregateEntity> kravgrunnlag = hentUniktResultat(query);
        return kravgrunnlag.stream().anyMatch(KravgrunnlagAggregate::isSperret);
    }

    public boolean erKravgrunnlagSomForventet(Long behandlingId) {
        TypedQuery<KravgrunnlagAggregateEntity> query = lagFinnKravgrunnlagQuery(behandlingId);
        KravgrunnlagAggregateEntity kravgrunnlag = hentUniktResultat(query).orElseThrow();

        try {
            KravgrunnlagValidator.validerGrunnlag(kravgrunnlag.getGrunnlagØkonomi());
            return true;
        } catch (KravgrunnlagValidator.UgyldigKravgrunnlagException e) {
            logger.warn(e.getMessage());
            return false;
        }
    }

    public Optional<KravgrunnlagAggregate> finnGrunnlagForVedtakId(Long vedtakId) {
        TypedQuery<KravgrunnlagAggregateEntity> query = entityManager.createQuery("from KravgrunnlagAggregateEntity aggr " +
                "where aggr.grunnlagØkonomi.vedtakId=:vedtakId and aggr.aktiv=:aktiv", KravgrunnlagAggregateEntity.class);
        query.setParameter("vedtakId", vedtakId);
        query.setParameter(AKTIV, true);
        Optional<KravgrunnlagAggregateEntity> resultat = hentUniktResultat(query);
        return Optional.ofNullable(resultat.orElse(null));
    }

    public void sperrGrunnlag(Long behandlingId) {
        Optional<KravgrunnlagAggregateEntity> aggregate = finnKravgrunnlagOptional(behandlingId);
        if (aggregate.isPresent()) {
            aggregate.get().sperr();
            entityManager.persist(aggregate.get());
        } else {
            logger.warn("FPT-710434: Forsøker å sperre kravgrunnlag, men det finnes ikke noe kravgrunnlag for behandlingId={}", behandlingId);
        }
    }

    public void opphevGrunnlag(Long behandlingId) {
        Optional<KravgrunnlagAggregateEntity> aggregate = finnKravgrunnlagOptional(behandlingId);
        if (aggregate.isPresent()) {
            aggregate.get().opphev();
            entityManager.persist(aggregate.get());
        } else {
            logger.warn("FPT-710435: Forsøker å oppheve kravgrunnlag, men det finnes ikke noe kravgrunnlag for behandlingId={}", behandlingId);
        }
    }

    private Optional<KravgrunnlagAggregateEntity> finnKravgrunnlagOptional(Long behandlingId) {
        TypedQuery<KravgrunnlagAggregateEntity> query = lagFinnKravgrunnlagQuery(behandlingId);
        return hentUniktResultat(query);
    }

    private List<KravgrunnlagAggregateEntity> finnAktiveKravgrunnlag(Long behandlingId) {
        TypedQuery<KravgrunnlagAggregateEntity> query = lagFinnKravgrunnlagQuery(behandlingId);
        return query.getResultList();
    }

    private TypedQuery<KravgrunnlagAggregateEntity> lagFinnKravgrunnlagQuery(Long behandlingId) {
        TypedQuery<KravgrunnlagAggregateEntity> query = entityManager.createQuery("from KravgrunnlagAggregateEntity aggr " +
                "where aggr.behandlingId=:behandlingId and aggr.aktiv=:aktiv", KravgrunnlagAggregateEntity.class);
        query.setParameter(BEHANDLING_ID, behandlingId);
        query.setParameter(AKTIV, true);
        return query;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
