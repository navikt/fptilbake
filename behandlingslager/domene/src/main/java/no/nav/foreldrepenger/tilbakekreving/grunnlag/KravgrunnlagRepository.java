package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentEksaktResultat;
import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

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
    public KravgrunnlagRepository( EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void lagre(Long behandlingId, Kravgrunnlag431 kravgrunnlag) {
        Optional<KravgrunnlagAggregateEntity> forrigeGrunnlag = finnKravgrunnlagOptional(behandlingId);
        if (forrigeGrunnlag.isPresent()) {
            forrigeGrunnlag.get().disable();
            entityManager.persist(forrigeGrunnlag.get());
        }
        KravgrunnlagAggregateEntity aggregate = new KravgrunnlagAggregateEntity.Builder()
            .medGrunnlagØkonomi(kravgrunnlag)
            .medBehandlingId(behandlingId)
            .medAktiv(true)
            .build();
        entityManager.persist(kravgrunnlag);
        entityManager.persist(aggregate);
        entityManager.flush(); //TODO unngå flush i repository, det er typisk bare nyttig for tester
    }

    public Kravgrunnlag431 finnKravgrunnlag(Long behandlingId) {
        TypedQuery<KravgrunnlagAggregateEntity> query = lagFinnKravgrunnlagQuery(behandlingId);
        KravgrunnlagAggregateEntity aggregate = hentEksaktResultat(query);
        return aggregate.getGrunnlagØkonomi();
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
            e.getFeil().log(logger);
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
            KravgrunnlagRepositoryFeil.FEILFACTORY.kanIkkeSperreGrunnlagSomIkkeFinnes(behandlingId).log(logger);
        }
    }

    public void opphevGrunnlag(Long behandlingId) {
        Optional<KravgrunnlagAggregateEntity> aggregate = finnKravgrunnlagOptional(behandlingId);
        if (aggregate.isPresent()) {
            aggregate.get().opphev();
            entityManager.persist(aggregate.get());
        } else {
            KravgrunnlagRepositoryFeil.FEILFACTORY.kanIkkeOppheveGrunnlagSomIkkeFinnes(behandlingId).log(logger);
        }
    }

    private Optional<KravgrunnlagAggregateEntity> finnKravgrunnlagOptional(Long behandlingId) {
        TypedQuery<KravgrunnlagAggregateEntity> query = lagFinnKravgrunnlagQuery(behandlingId);
        return hentUniktResultat(query);
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

    interface KravgrunnlagRepositoryFeil extends DeklarerteFeil {

        KravgrunnlagRepositoryFeil FEILFACTORY = FeilFactory.create(KravgrunnlagRepositoryFeil.class);

        @TekniskFeil(feilkode = "FPT-710434", feilmelding = "Forsøker å sperre kravgrunnlag, men det finnes ikke noe kravgrunnlag for behandlingId=%s", logLevel = LogLevel.WARN)
        Feil kanIkkeSperreGrunnlagSomIkkeFinnes(Long behandlingId);

        @TekniskFeil(feilkode = "FPT-710435", feilmelding = "Forsøker å oppheve kravgrunnlag, men det finnes ikke noe kravgrunnlag for behandlingId=%s", logLevel = LogLevel.WARN)
        Feil kanIkkeOppheveGrunnlagSomIkkeFinnes(Long behandlingId);
    }
}
