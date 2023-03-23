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

    private static final Logger LOG = LoggerFactory.getLogger(KravgrunnlagRepository.class);

    private static final String BEHANDLING_ID = "behandlingId";
    private EntityManager entityManager;

    KravgrunnlagRepository() {
        // for CDI
    }

    @Inject
    public KravgrunnlagRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void lagre(Long behandlingId, Kravgrunnlag431 kravgrunnlag) {
        var forrigeKravgrunnlag = finnKravgrunnlagOptional(behandlingId);
        if (forrigeKravgrunnlag.isPresent()) {
            forrigeKravgrunnlag.get().disable();
            entityManager.persist(forrigeKravgrunnlag.get());
        }
        lagreNyttKravgrunnlag(behandlingId, kravgrunnlag);
    }

    public Kravgrunnlag431 hentIsAktivFor(Long behandlingId) {
        var query = getEntityManager().createQuery("""
            SELECT kg431 FROM KravgrunnlagAggregateEntity grunnlag JOIN grunnlag.grunnlagØkonomi kg431
                WHERE grunnlag.behandlingId = :behandlingId
                AND grunnlag.aktiv = true""", Kravgrunnlag431.class);
        query.setParameter(BEHANDLING_ID, behandlingId);
        return hentEksaktResultat(query);
    }

    public boolean finnesIsAktivFor(Long behandlingId) {
        return Optional.ofNullable(hentIsAktivFor(behandlingId)).isPresent();
    }

    public void lagreOgFiksDuplikateKravgrunnlag(Long behandlingId, Kravgrunnlag431 kravgrunnlag) {
        var forrigeKravgrunnlag = finnAktiveKravgrunnlag(behandlingId);
        for (var forrige : forrigeKravgrunnlag) {
            forrige.disable();
            entityManager.persist(forrige);
            if (forrigeKravgrunnlag.size() > 1) {
                LOG.warn("Fikser duplikate kravgrunnlag, disabler kravgrunnlagAggregateEntity {} for behandling {}", forrige.getId(), behandlingId);
            }
        }
        lagreNyttKravgrunnlag(behandlingId, kravgrunnlag);
    }

    private void lagreNyttKravgrunnlag(Long behandlingId, Kravgrunnlag431 kravgrunnlag) {
        var aggregate = new KravgrunnlagAggregateEntity.Builder()
                .medGrunnlagØkonomi(kravgrunnlag)
                .medBehandlingId(behandlingId)
                .medAktiv(true)
                .build();
        entityManager.persist(kravgrunnlag);
        for (var periode432 : kravgrunnlag.getPerioder()) {
            entityManager.persist(periode432);
            for (var beløp433 : periode432.getKravgrunnlagBeloper433()) {
                entityManager.persist(beløp433);
            }
        }
        entityManager.persist(aggregate);
        entityManager.flush(); //TODO unngå flush i repository, det er typisk bare nyttig for tester
    }

    public Kravgrunnlag431 finnKravgrunnlag(Long behandlingId) {
        var aggregate = hentEksaktResultat(finnAktivKravgrunnlagQuery(behandlingId));
        return aggregate.getGrunnlagØkonomi();
    }

    public Optional<Kravgrunnlag431> finnKravgrunnlagOpt(Long behandlingId) {
        var aggregate = finnKravgrunnlagOptional(behandlingId);
        return aggregate.map(KravgrunnlagAggregateEntity::getGrunnlagØkonomi);
    }

    public boolean harGrunnlagForBehandlingId(Long behandlingId) {
        var query = entityManager.createQuery("""
            SELECT count(1) FROM KravgrunnlagAggregateEntity aggr
            WHERE aggr.behandlingId = :behandlingId
            AND aggr.aktiv = true""", Long.class);
        query.setParameter(BEHANDLING_ID, behandlingId);
        return query.getSingleResult() > 0;
    }

    public boolean erKravgrunnlagSperret(Long behandlingId) {
        var kravgrunnlag = finnKravgrunnlagOptional(behandlingId);
        return kravgrunnlag.stream().anyMatch(KravgrunnlagAggregate::isSperret);
    }

    public boolean erKravgrunnlagSomForventet(Long behandlingId) {
        var kravgrunnlag = finnKravgrunnlagOptional(behandlingId).orElseThrow();

        try {
            KravgrunnlagValidator.validerGrunnlag(kravgrunnlag.getGrunnlagØkonomi());
            return true;
        } catch (KravgrunnlagValidator.UgyldigKravgrunnlagException e) {
            LOG.warn(e.getMessage());
            return false;
        }
    }

    public void sperrGrunnlag(Long behandlingId) {
        var aggregate = finnKravgrunnlagOptional(behandlingId);
        if (aggregate.isPresent()) {
            aggregate.get().sperr();
            entityManager.persist(aggregate.get());
        } else {
            LOG.warn("FPT-710434: Forsøker å sperre kravgrunnlag, men det finnes ikke noe kravgrunnlag for behandlingId={}", behandlingId);
        }
    }

    public void opphevGrunnlag(Long behandlingId) {
        var aggregate = finnKravgrunnlagOptional(behandlingId);
        if (aggregate.isPresent()) {
            aggregate.get().opphev();
            entityManager.persist(aggregate.get());
        } else {
            LOG.warn("FPT-710435: Forsøker å oppheve kravgrunnlag, men det finnes ikke noe kravgrunnlag for behandlingId={}", behandlingId);
        }
    }

    private Optional<KravgrunnlagAggregateEntity> finnKravgrunnlagOptional(Long behandlingId) {
        var query = finnAktivKravgrunnlagQuery(behandlingId);
        return hentUniktResultat(query);
    }

    private List<KravgrunnlagAggregateEntity> finnAktiveKravgrunnlag(Long behandlingId) {
        var query = finnAktivKravgrunnlagQuery(behandlingId);
        return query.getResultList();
    }

    private TypedQuery<KravgrunnlagAggregateEntity> finnAktivKravgrunnlagQuery(Long behandlingId) {
        var query = entityManager.createQuery("""
            FROM KravgrunnlagAggregateEntity aggr
            WHERE aggr.behandlingId = :behandlingId
            AND aggr.aktiv = true""", KravgrunnlagAggregateEntity.class);
        query.setParameter(BEHANDLING_ID, behandlingId);
        return query;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
