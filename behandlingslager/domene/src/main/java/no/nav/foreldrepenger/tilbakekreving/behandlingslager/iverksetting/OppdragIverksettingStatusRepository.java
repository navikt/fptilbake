package no.nav.foreldrepenger.tilbakekreving.behandlingslager.iverksetting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import no.nav.vedtak.felles.jpa.HibernateVerktøy;

@Dependent
public class OppdragIverksettingStatusRepository {

    private EntityManager entityManager;

    @Inject
    public OppdragIverksettingStatusRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void registrerKvittertVedtak(Long behandlingId, String vedtakId) {
        var kvittering = hentOppdragIverksettingStatus(behandlingId).orElseGet(() -> new OppdragIverksettingStatusEntitet(behandlingId, vedtakId));
        kvittering.registrerKvittering(LocalDateTime.now(), true);
        entityManager.persist(kvittering);
        entityManager.flush();
    }

    public Optional<OppdragIverksettingStatusEntitet> hentOppdragIverksettingStatus(Long behandlingId) {
        var query = entityManager.createQuery("""
           SELECT ois
           FROM OppdragIverksettingStatus ois
           WHERE ois.behandlingId = :behandlingId
           AND ois.aktiv = true""", OppdragIverksettingStatusEntitet.class);
        query.setParameter("behandlingId", behandlingId);

        return HibernateVerktøy.hentUniktResultat(query);
    }

    public List<OppdragIverksettingStatusEntitet> finnForDato(LocalDate dato){
        var query = entityManager.createQuery("""
        from OppdragIverksettingStatus
        where opprettetTidspunkt >= :t0
        and opprettetTidspunkt < :t1
        order by opprettetTidspunkt desc""", OppdragIverksettingStatusEntitet.class);
        query.setParameter("t0", dato.atStartOfDay());
        query.setParameter("t1", dato.plusDays(1).atStartOfDay());

        return query.getResultList();
    }
}
