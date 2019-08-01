package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentEksaktResultat;
import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.List;
import java.util.Optional;

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
    private EntityManager entityManager;

    EksternBehandlingRepositoryImpl() {
        // CDI
    }

    @Inject
    public EksternBehandlingRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Long lagre(EksternBehandling eksternBehandling) {
        Optional<EksternBehandling> eksisterende = hentOptionalFraInternId(eksternBehandling.getInternId());
        eksisterende.ifPresent(o -> {
            o.setInaktiv();
            entityManager.persist(o);
        });
        entityManager.persist(eksternBehandling);
        entityManager.flush();
        return eksternBehandling.getId();
    }

    @Override
    public EksternBehandling hentFraInternId(long internBehandlingId) {
        TypedQuery<EksternBehandling> query = entityManager.createQuery("from EksternBehandling where intern_id=:internId and aktiv='J'", EksternBehandling.class);
        query.setParameter("internId", internBehandlingId);
        return hentEksaktResultat(query);
    }

    @Override
    public Optional<EksternBehandling> hentFraEksternId(long eksternBehandlingId) {
        TypedQuery<EksternBehandling> query = entityManager.createQuery("from EksternBehandling where ekstern_id=:eksternId and aktiv='J'", EksternBehandling.class);
        query.setParameter(EKSTERN_ID, eksternBehandlingId);
        return hentUniktResultat(query);
    }

    @Override
    public List<EksternBehandling> hentAlleBehandlingerMedEksternId(long eksternBehandlingId) {
        TypedQuery<EksternBehandling> query = entityManager.createQuery("from EksternBehandling where ekstern_id=:eksternId and aktiv='J'", EksternBehandling.class);
        query.setParameter(EKSTERN_ID, eksternBehandlingId);
        return query.getResultList();
    }

    @Override
    public Optional<EksternBehandling> finnForSisteAvsluttetTbkBehandling(long eksternBehandlingId){
        TypedQuery<EksternBehandling> query = entityManager.createQuery("select eks from EksternBehandling eks , Behandling beh where eks.internId=beh.id " +
            "and eks.eksternId=:eksternId and beh.behandlingType=:behandlingType " +
            "and beh.status = :behandlingStatus and eks.aktiv='J' " +
            "ORDER BY beh.opprettetTidspunkt DESC", EksternBehandling.class);

        query.setParameter(EKSTERN_ID, eksternBehandlingId);
        query.setParameter("behandlingType", BehandlingType.TILBAKEKREVING);
        query.setParameter("behandlingStatus", BehandlingStatus.AVSLUTTET);
        List<EksternBehandling> eksternBehandlinger =  query.getResultList();

        return eksternBehandlinger.isEmpty() ? Optional.empty() : Optional.of(eksternBehandlinger.get(0));
    }

    private Optional<EksternBehandling> hentOptionalFraInternId(long internBehandlingId) {
        TypedQuery<EksternBehandling> query = entityManager.createQuery("from EksternBehandling where intern_id=:internId and aktiv='J'", EksternBehandling.class);
        query.setParameter("internId", internBehandlingId);
        return hentUniktResultat(query);
    }
}
