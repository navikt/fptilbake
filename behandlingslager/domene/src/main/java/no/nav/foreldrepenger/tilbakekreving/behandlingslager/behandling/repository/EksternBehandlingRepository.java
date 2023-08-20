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
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Henvisning;

@ApplicationScoped
public class EksternBehandlingRepository {

    private static final String EKSTERN_UUID = "eksternUuid";
    private static final String INTERN_ID = "internId";
    private static final String HENVISNING = "henvisning";
    private static final String QRY_INTERINID_AKTIV = "from EksternBehandling where intern_id=:internId and aktiv='J'";
    private EntityManager entityManager;

    EksternBehandlingRepository() {
        // CDI
    }

    @Inject
    public EksternBehandlingRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Lagrer eksternBehandling, setter eksisterende, hvis finnes, til inaktiv
     *
     * @param eksternBehandling
     */
    public void lagre(EksternBehandling eksternBehandling) {
        Optional<EksternBehandling> eksisterende = hentOptionalFraInternId(eksternBehandling.getInternId());
        eksisterende.ifPresent(o -> {
            o.deaktiver();
            entityManager.persist(o);
        });
        Optional<EksternBehandling> eksisterendeDeaktivert = hentEksisterendeDeaktivert(eksternBehandling.getInternId(), eksternBehandling.getHenvisning());
        eksisterendeDeaktivert.ifPresentOrElse(o -> {
            o.reaktivate();
            entityManager.persist(o);
        }, () -> entityManager.persist(eksternBehandling));
        entityManager.flush();
    }

    /**
     * Henter eksternBehandling data for behandlingen
     *
     * @param internBehandlingId
     */
    public EksternBehandling hentFraInternId(long internBehandlingId) {
        TypedQuery<EksternBehandling> query = entityManager.createQuery(QRY_INTERINID_AKTIV, EksternBehandling.class);
        query.setParameter(INTERN_ID, internBehandlingId);
        return hentEksaktResultat(query);
    }

    public Optional<EksternBehandling> hentFraHenvisning(Henvisning henvisning) {
        TypedQuery<EksternBehandling> query = entityManager.createQuery("from EksternBehandling where henvisning=:henvisning and aktiv='J'", EksternBehandling.class);
        query.setParameter(HENVISNING, henvisning);
        return hentUniktResultat(query);
    }

    /**
     * Ikke bruk denne - unntatt i spesialtilfeller for å rydde opp
     */
    public List<EksternBehandling> hentFlereFraHenvisning(Henvisning henvisning) {
        TypedQuery<EksternBehandling> query = entityManager.createQuery("from EksternBehandling where henvisning=:henvisning and aktiv='J'", EksternBehandling.class);
        query.setParameter(HENVISNING, henvisning);
        return query.getResultList();
    }

    /**
     * Finner alle behandlinger som knyttet med uuid fra ekstern system
     *
     * @param eksternUuid
     */
    public List<EksternBehandling> hentAlleBehandlingerMedEksternUuid(UUID eksternUuid) {
        TypedQuery<EksternBehandling> query = entityManager.createQuery("from EksternBehandling where eksternUuid=:eksternUuid and aktiv='J'", EksternBehandling.class);
        query.setParameter(EKSTERN_UUID, eksternUuid);
        return query.getResultList();
    }

    /**
     * Finner siste avsluttet TilbakekkrevingBehandling basert på eksternUuid
     *
     * @param eksternUuid
     * @return
     */
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

    public boolean finnesEksternBehandling(long internId, Henvisning henvisning) {
        TypedQuery<EksternBehandling> query = entityManager.createQuery("from EksternBehandling where henvisning=:henvisning and intern_id=:internId and aktiv='J'", EksternBehandling.class);
        query.setParameter(HENVISNING, henvisning);
        query.setParameter(INTERN_ID, internId);
        return !query.getResultList().isEmpty();
    }

    public boolean finnesAktivtEksternBehandling(long internId) {
        TypedQuery<EksternBehandling> query = entityManager.createQuery(QRY_INTERINID_AKTIV, EksternBehandling.class);
        query.setParameter(INTERN_ID, internId);
        return !query.getResultList().isEmpty();
    }

    public void deaktivateTilkobling(long internId) {
        EksternBehandling eksternBehandling = hentFraInternId(internId);
        eksternBehandling.deaktiver();
        entityManager.persist(eksternBehandling);
        entityManager.flush();
    }

    /**
     * Finner eksternbehandling for siste aktivert internId
     *
     * @param internBehandlingId
     * @return eksternBehandling
     */
    public EksternBehandling hentForSisteAktivertInternId(long internBehandlingId) {
        TypedQuery<EksternBehandling> query = entityManager.createQuery("from EksternBehandling where intern_id=:internId order by opprettetTidspunkt desc", EksternBehandling.class);
        query.setParameter(INTERN_ID, internBehandlingId);
        return query.getResultList().get(0);
    }

    public Optional<EksternBehandling> hentOptionalFraInternId(long internBehandlingId) {
        TypedQuery<EksternBehandling> query = entityManager.createQuery(QRY_INTERINID_AKTIV, EksternBehandling.class);
        query.setParameter(INTERN_ID, internBehandlingId);
        return hentUniktResultat(query);
    }

    public Optional<EksternBehandling> hentEksisterendeDeaktivert(long internBehandlingId, Henvisning henvisning) {
        TypedQuery<EksternBehandling> query = entityManager.createQuery("from EksternBehandling where intern_id=:internId and henvisning=:henvisning order by opprettetTidspunkt desc", EksternBehandling.class);
        query.setParameter(INTERN_ID, internBehandlingId);
        query.setParameter(HENVISNING, henvisning);

        return hentUniktResultat(query);
    }

    /*
     * For idempotens-sjekk
     */
    public boolean harEksternBehandlingForEksternUuid(UUID eksternUuid) {
        return !entityManager.createQuery("from EksternBehandling where eksternUuid=:eksternUuid", EksternBehandling.class)
                .setParameter(EKSTERN_UUID, eksternUuid)
                .getResultList()
                .isEmpty();
    }
}
