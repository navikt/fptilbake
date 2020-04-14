package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentEksaktResultat;
import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.ekstern.EksternBehandling;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class EksternBehandlingRepositoryImpl implements EksternBehandlingRepository {

    private static final String EKSTERN_ID = "eksternId";
    private static final String EKSTERN_UUID = "eksternUuid";
    private static final String INTERN_ID = "internId";
    private EntityManager entityManager;

    EksternBehandlingRepositoryImpl() {
        // CDI
    }

    @Inject
    public EksternBehandlingRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void lagre(EksternBehandling eksternBehandling) {
        Optional<EksternBehandling> eksisterende = hentOptionalFraInternId(eksternBehandling.getInternId());
        eksisterende.ifPresent(o -> {
            o.deaktiver();
            entityManager.persist(o);
        });
        Optional<EksternBehandling> eksisterendeDeaktivert = hentEksisterendeDeaktivert(eksternBehandling.getInternId(),eksternBehandling.getEksternId());
        eksisterendeDeaktivert.ifPresentOrElse(o -> {
            o.reaktivate();
            entityManager.persist(o);
        },() -> entityManager.persist(eksternBehandling));
        entityManager.flush();
    }

    @Override
    public EksternBehandling hentFraInternId(long internBehandlingId) {
        TypedQuery<EksternBehandling> query = entityManager.createQuery("from EksternBehandling where intern_id=:internId and aktiv='J'", EksternBehandling.class);
        query.setParameter(INTERN_ID, internBehandlingId);
        return hentEksaktResultat(query);
    }

    @Override
    public Optional<EksternBehandling> hentFraEksternId(long eksternBehandlingId) {
        TypedQuery<EksternBehandling> query = entityManager.createQuery("from EksternBehandling where ekstern_id=:eksternId and aktiv='J'", EksternBehandling.class);
        query.setParameter(EKSTERN_ID, eksternBehandlingId);
        return hentUniktResultat(query);
    }

    @Override
    public List<EksternBehandling> hentAlleBehandlingerMedEksternUuid(UUID eksternUuid) {
        TypedQuery<EksternBehandling> query = entityManager.createQuery("from EksternBehandling where eksternUuid=:eksternUuid and aktiv='J'", EksternBehandling.class);
        query.setParameter(EKSTERN_UUID, eksternUuid);
        return query.getResultList();
    }

    @Override
    public Optional<EksternBehandling> finnForSisteAvsluttetTbkBehandling(UUID eksternUuid) {
        TypedQuery<EksternBehandling> query = entityManager.createQuery("select eks from EksternBehandling eks , Behandling beh where eks.internId=beh.id " +
            "and eks.eksternUuid=:eksternUuid and beh.behandlingType=:behandlingType " +
            "and beh.status = :behandlingStatus and eks.aktiv='J' " +
            "ORDER BY beh.opprettetTidspunkt DESC", EksternBehandling.class);

        query.setParameter(EKSTERN_UUID, eksternUuid);
        query.setParameter("behandlingType", BehandlingType.TILBAKEKREVING);
        query.setParameter("behandlingStatus", BehandlingStatus.AVSLUTTET);
        List<EksternBehandling> eksternBehandlinger = query.getResultList();

        return eksternBehandlinger.isEmpty() ? Optional.empty() : Optional.of(eksternBehandlinger.get(0));
    }

    @Override
    public boolean finnesEksternBehandling(long internId, long eksternId) {
        TypedQuery<EksternBehandling> query = entityManager.createQuery("from EksternBehandling where ekstern_id=:eksternId and intern_id=:internId and aktiv='J'", EksternBehandling.class);
        query.setParameter(EKSTERN_ID, eksternId);
        query.setParameter(INTERN_ID, internId);
        return !query.getResultList().isEmpty();
    }

    @Override
    public void deaktivateTilkobling(long internId) {
        EksternBehandling eksternBehandling = hentFraInternId(internId);
        eksternBehandling.deaktiver();
        entityManager.persist(eksternBehandling);
    }

    @Override
    public EksternBehandling hentForSisteAktivertInternId(long internBehandlingId) {
        TypedQuery<EksternBehandling> query = entityManager.createQuery("from EksternBehandling where intern_id=:internId order by opprettetTidspunkt desc", EksternBehandling.class);
        query.setParameter(INTERN_ID, internBehandlingId);
        return query.getResultList().get(0);
    }

    @Override
    public Optional<EksternBehandling> hentOptionalFraInternId(long internBehandlingId) {
        TypedQuery<EksternBehandling> query = entityManager.createQuery("from EksternBehandling where intern_id=:internId and aktiv='J'", EksternBehandling.class);
        query.setParameter(INTERN_ID, internBehandlingId);
        return hentUniktResultat(query);
    }

    @Override
    public void oppdaterVerge(long vergeId, long internId){
        EksternBehandling eksternBehandling = hentFraInternId(internId);
        eksternBehandling.setVergeId(vergeId);
        entityManager.persist(eksternBehandling);
    }

    private Optional<EksternBehandling> hentEksisterendeDeaktivert(long internBehandlingId, long eksternBehandlingId){
        TypedQuery<EksternBehandling> query = entityManager.createQuery("from EksternBehandling where intern_id=:internId and ekstern_id=:eksternId order by opprettetTidspunkt desc", EksternBehandling.class);
        query.setParameter(INTERN_ID, internBehandlingId);
        query.setParameter(EKSTERN_ID, eksternBehandlingId);

        return hentUniktResultat(query);
    }
}
