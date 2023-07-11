package no.nav.foreldrepenger.tilbakekreving.grunnlag;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import no.nav.foreldrepenger.tilbakekreving.grunnlag.kodeverk.KravStatusKode;

@ApplicationScoped
public class KravVedtakStatusRepository {

    private EntityManager entityManager;

    KravVedtakStatusRepository() {
        // for CDI
    }

    @Inject
    public KravVedtakStatusRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void lagre(Long behandlingId, KravVedtakStatus437 kravVedtakStatus) {
        var forrigeGrunnlagStatus = finnKravStatusForBehandlingId(behandlingId);
        if (forrigeGrunnlagStatus.isPresent()) {
            forrigeGrunnlagStatus.get().disable();
            entityManager.persist(forrigeGrunnlagStatus.get());
        }
        var aggregate = new KravVedtakStatusAggregate.Builder()
            .medKravVedtakStatus(kravVedtakStatus)
            .medBehandlingId(behandlingId)
            .medAktiv(true)
            .build();
        entityManager.persist(kravVedtakStatus);
        entityManager.persist(aggregate);
        entityManager.flush();
    }


    public Optional<KravStatusKode> finnKravStatus(Long behandlingId) {
        return finnKravStatusForBehandlingId(behandlingId)
            .map(ks -> ks.getKravVedtakStatus().getKravStatusKode());
    }

    private Optional<KravVedtakStatusAggregate> finnKravStatusForBehandlingId(Long behandlingId) {
        var query = entityManager.createQuery("""
            FROM KravVedtakStatusAggregate aggr
            WHERE aggr.behandlingId = :behandlingId
            AND aggr.aktiv = true""", KravVedtakStatusAggregate.class);
        query.setParameter("behandlingId", behandlingId);
        return hentUniktResultat(query);
    }
}
