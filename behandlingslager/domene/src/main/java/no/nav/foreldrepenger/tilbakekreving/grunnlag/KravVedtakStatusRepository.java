package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class KravVedtakStatusRepository {

    private EntityManager entityManager;

    KravVedtakStatusRepository() {
        // for CDI
    }

    @Inject
    public KravVedtakStatusRepository(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void lagre(Long behandlingId, KravVedtakStatus437 kravVedtakStatus) {
        Optional<KravVedtakStatusAggregate> forrigeGrunnlag = finnForBehandlingId(behandlingId);
        if (forrigeGrunnlag.isPresent()) {
            forrigeGrunnlag.get().disable();
            entityManager.persist(forrigeGrunnlag.get());
        }
        KravVedtakStatusAggregate aggregate = new KravVedtakStatusAggregate.Builder()
            .medKravVedtakStatus(kravVedtakStatus)
            .medBehandlingId(behandlingId)
            .medAktiv(true)
            .build();
        entityManager.persist(kravVedtakStatus);
        entityManager.persist(aggregate);
        entityManager.flush();
    }

    public Optional<KravVedtakStatusAggregate> finnForBehandlingId(Long behandlingId) {
        TypedQuery<KravVedtakStatusAggregate> query = entityManager.createQuery("from KravVedtakStatusAggregate aggr " +
            "where aggr.behandlingId=:behandlingId and aggr.aktiv=:aktiv", KravVedtakStatusAggregate.class);
        query.setParameter("behandlingId", behandlingId);
        query.setParameter("aktiv", true);
        return hentUniktResultat(query);
    }
}
