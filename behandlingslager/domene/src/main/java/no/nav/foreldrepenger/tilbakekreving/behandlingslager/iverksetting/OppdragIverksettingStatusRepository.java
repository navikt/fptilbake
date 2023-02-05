package no.nav.foreldrepenger.tilbakekreving.behandlingslager.iverksetting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.jpa.HibernateVerktøy;

@Dependent
public class OppdragIverksettingStatusRepository {

    private static final Logger logger = LoggerFactory.getLogger(OppdragIverksettingStatusRepository.class);

    private EntityManager entityManager;

    @Inject
    public OppdragIverksettingStatusRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void registrerStarterIverksetting(Long behandlingId, String vedtakId) {
        Optional<OppdragIverksettingStatus> eksisterende = hentOppdragIverksettingStatus(behandlingId);
        if (eksisterende.isPresent()) {
            //skjer hvis prosesstask for iverksetting rekjøres
            logger.info("Har allerede registrert at iverksetting er startet");
        } else {
            entityManager.persist(new OppdragIverksettingStatus(behandlingId, vedtakId));
        }
    }

    public void registrerKvittering(Long behandlingId, boolean kvitteringOk) {
        registrerKvittering(behandlingId, LocalDateTime.now(), kvitteringOk);
    }

    private void registrerKvittering(Long behandlingId, LocalDateTime tidspunkt, boolean kvitteringOk) {
        Optional<OppdragIverksettingStatus> eksisterende = hentOppdragIverksettingStatus(behandlingId);
        if (eksisterende.isEmpty()) {
            throw new IllegalStateException("Kan ikke oppdatere " + OppdragIverksettingStatus.class + " for behandling " + behandlingId + " siden det ikke finnes noen slik fra før");
        }
        if (Boolean.TRUE.equals(eksisterende.get().getKvitteringOk())) {
            throw new IllegalStateException("Har allerede mottatt positiv kvittering for behandlingId=" + behandlingId);
        }
        OppdragIverksettingStatus status = eksisterende.get();
        status.registrerKvittering(tidspunkt, kvitteringOk);
        entityManager.persist(status);
    }

    public Optional<OppdragIverksettingStatus> hentOppdragIverksettingStatus(Long behandlingId) {
        var query = entityManager.createQuery("SELECT ois " +
            "FROM OppdragIverksettingStatus ois " +
            "WHERE ois.behandlingId = :behandlingId " +
            "AND ois.aktiv = true", OppdragIverksettingStatus.class);
        query.setParameter("behandlingId", behandlingId);

        return HibernateVerktøy.hentUniktResultat(query);
    }

    public List<OppdragIverksettingStatus> finnForDato(LocalDate dato){
        TypedQuery<OppdragIverksettingStatus> query = entityManager.createQuery("from OppdragIverksettingStatus where opprettetTidspunkt >= :t0 and opprettetTidspunkt < :t1 order by opprettetTidspunkt desc", OppdragIverksettingStatus.class);
        query.setParameter("t0", dato.atStartOfDay());
        query.setParameter("t1", dato.plusDays(1).atStartOfDay());
        return query.getResultList();
    }

}
