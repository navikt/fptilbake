package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentEksaktResultat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

@ApplicationScoped
public class BehandlingRepositoryImpl implements BehandlingRepository {

    public static final String KEY_FAGSAK_ID = "fagsakId";
    private static final String KEY_BEHANDLING_TYPE = "behandlingType";

    private EntityManager entityManager;

    @Inject
    public BehandlingRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    BehandlingRepositoryImpl() {
    }

    EntityManager getEntityManager() {
        return entityManager;
    }


    @Override
    public Behandling hentBehandling(Long behandlingId) {
        Objects.requireNonNull(behandlingId, "behandlingId"); // NOSONAR //$NON-NLS-1$
        return hentEksaktResultat(lagBehandlingQuery(behandlingId));
    }

    @Override
    public Behandling hentBehandling(UUID uuid) {
        Objects.requireNonNull(uuid, "behandlingUUID"); // NOSONAR //$NON-NLS-1$
        return hentEksaktResultat(lagBehandlingQuery(uuid));
    }

    @Override
    public List<Behandling> hentAlleBehandlingerForSaksnummer(Saksnummer saksnummer) {
        Objects.requireNonNull(saksnummer, "saksnummer"); //$NON-NLS-1$

        TypedQuery<Behandling> query = getEntityManager().createQuery(
            "SELECT beh from Behandling beh, Fagsak fagsak WHERE beh.fagsak.id=fagsak.id AND fagsak.saksnummer=:saksnummer", //$NON-NLS-1$
            Behandling.class);
        query.setParameter("saksnummer", saksnummer); //$NON-NLS-1$
        return query.getResultList();
    }

    @Override
    public String hentSaksnummerForBehandling(long behandlingId) {
        Query query = getEntityManager().createNativeQuery("select f.saksnummer from Fagsak f inner join Behandling b on b.fagsak_id = f.id where b.id = :behandlingId");
        query.setParameter("behandlingId", behandlingId);
        List<Object> resultat = query.getResultList();
        if (resultat.isEmpty()) {
            throw new IllegalStateException("Utviklerfeil: fant ingen fagsaker knyttet til behandlingId ('" + behandlingId + "')");
        }
        if (resultat.size() > 1) {
            throw new IllegalStateException("Utviklerfeil: fant flere fagsaker knyttet til samme behandlingId ('" + behandlingId + "')");
        }
        return (String) resultat.get(0);
    }

    @Override
    public List<Long> hentAlleBehandlingIder() {
        TypedQuery<Long> query = getEntityManager().createQuery("select id from Behandling", Long.class); //$NON-NLS-1$
        return query.getResultList();
    }

    @Override
    public List<Long> hentAlleAvsluttetBehandlingIder() {
        TypedQuery<Long> query = getEntityManager().createQuery("select id from Behandling where status=:status", Long.class); //$NON-NLS-1$
        query.setParameter("status", BehandlingStatus.AVSLUTTET);
        return query.getResultList();
    }

    @Override
    public List<Behandling> hentBehandlingerSomIkkeErAvsluttetForFagsakId(Long fagsakId) {
        Objects.requireNonNull(fagsakId, KEY_FAGSAK_ID); //$NON-NLS-1$

        TypedQuery<Behandling> query = getEntityManager().createQuery(
            "SELECT beh from Behandling beh WHERE beh.fagsak.id = :fagsakId AND beh.status <> :status", //$NON-NLS-1$
            Behandling.class);
        query.setParameter(KEY_FAGSAK_ID, fagsakId); //$NON-NLS-1$
        query.setParameter("status", BehandlingStatus.AVSLUTTET); //$NON-NLS-1$
        query.setHint(QueryHints.HINT_READONLY, "true"); //$NON-NLS-1$
        return query.getResultList();
    }

    @Override
    public Long lagre(Behandling behandling, BehandlingLås lås) {
        if (!Objects.equals(behandling.getId(), lås.getBehandlingId())) {
            // hvis satt må begge være like. (Objects.equals håndterer også at begge er null)
            throw new IllegalArgumentException("Behandling#id og lås#behandlingId må være like, eller begge må være null."); //$NON-NLS-1$
        }

        long behandlingId = lagre(behandling);
        verifiserBehandlingLås(lås);

        // i tilfelle denne ikke er satt fra før, f.eks. for ny entitet
        lås.setBehandlingId(behandlingId);

        return behandlingId;
    }

    @Override
    public Long lagreOgClear(Behandling behandling, BehandlingLås lås) {
        Long id = lagre(behandling, lås);
        getEntityManager().clear();
        return id;
    }

    @Override
    public BehandlingLås taSkriveLås(Behandling behandling) {
        Objects.requireNonNull(behandling, "behandling"); //$NON-NLS-1$
        BehandlingLåsRepositoryImpl låsRepo = new BehandlingLåsRepositoryImpl(getEntityManager());

        return låsRepo.taLås(behandling.getId());
    }

    // sjekk lås og oppgrader til skriv
    @Override
    public void verifiserBehandlingLås(BehandlingLås lås) {
        BehandlingLåsRepositoryImpl låsHåndterer = new BehandlingLåsRepositoryImpl(getEntityManager());
        låsHåndterer.oppdaterLåsVersjon(lås);
    }


    @Override
    public BehandlingStegType finnBehandlingStegType(String kode) {
        return getEntityManager().find(BehandlingStegType.class, kode);
    }

    @Override
    public Optional<Behandling> hentSisteBehandlingForFagsakId(Long fagsakId, BehandlingType behandlingType) {
        return finnSisteBehandling(fagsakId, behandlingType, false);
    }

    @Override
    public Boolean erVersjonUendret(Long behandlingId, Long versjon) {
        Query query = getEntityManager().createNativeQuery(
            "select count(*) from dual " +
                "where exists (select 1 from behandling " +
                "where (behandling.id = ?) " +
                "and (behandling.versjon = ?))");
        query.setParameter(1, behandlingId);
        query.setParameter(2, versjon);
        return ((BigDecimal) query.getSingleResult()).intValue() == 1;
    }

    @Override
    public Behandling opprettNyBehandlingBasertPåTidligere(Behandling gammelBehandling, BehandlingType behandlingType) {
        Behandling.Builder nyBuilder = Behandling.nyBehandlingFor(gammelBehandling.getFagsak(), behandlingType);

        Behandling nyBehandling = nyBuilder.build();
        BehandlingLås lås = taSkriveLås(nyBehandling);
        lagre(nyBehandling, lås);
        return nyBehandling;
    }

    private Optional<Behandling> finnSisteBehandling(Long fagsakId, BehandlingType behandlingType, boolean readOnly) {
        Objects.requireNonNull(fagsakId, KEY_FAGSAK_ID); // NOSONAR //$NON-NLS-1$
        Objects.requireNonNull(behandlingType, KEY_BEHANDLING_TYPE); // NOSONAR //$NON-NLS-1$

        TypedQuery<Behandling> query = getEntityManager().createQuery(
            "from Behandling where fagsak.id=:fagsakId and behandlingType=:behandlingType order by opprettetTidspunkt desc", //$NON-NLS-1$
            Behandling.class);
        query.setParameter(KEY_FAGSAK_ID, fagsakId); //$NON-NLS-1$
        query.setParameter(KEY_BEHANDLING_TYPE, behandlingType); //$NON-NLS-1$
        if (readOnly) {
            query.setHint(QueryHints.HINT_READONLY, "true"); //$NON-NLS-1$
        }
        return optionalFirst(query.getResultList());
    }

    private TypedQuery<Behandling> lagBehandlingQuery(Long behandlingId) {
        TypedQuery<Behandling> query = getEntityManager().createQuery("from Behandling where id=:behandlingId", Behandling.class); //$NON-NLS-1$
        query.setParameter("behandlingId", behandlingId); //$NON-NLS-1$
        return query;
    }

    private TypedQuery<Behandling> lagBehandlingQuery(UUID behandlingUUID) {
        TypedQuery<Behandling> query = getEntityManager().createQuery("from Behandling where uuid=:behandlingUUID", Behandling.class); //$NON-NLS-1$
        query.setParameter("behandlingUUID", behandlingUUID); //$NON-NLS-1$
        return query;
    }

    Long lagre(Behandling behandling) {
        getEntityManager().persist(behandling);

        List<BehandlingÅrsak> behandlingÅrsaker = behandling.getBehandlingÅrsaker();
        if (!behandlingÅrsaker.isEmpty()) {
            behandlingÅrsaker.forEach(getEntityManager()::persist);
        }

        getEntityManager().flush();
        return behandling.getId();
    }

    private static Optional<Behandling> optionalFirst(List<Behandling> behandlinger) {
        return behandlinger.isEmpty() ? Optional.empty() : Optional.of(behandlinger.get(0));
    }
}
