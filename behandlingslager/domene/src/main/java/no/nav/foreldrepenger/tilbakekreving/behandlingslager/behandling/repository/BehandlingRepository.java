package no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentEksaktResultat;
import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.hibernate.jpa.HibernateHints;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

@ApplicationScoped
public class BehandlingRepository {

    public static final String KEY_FAGSAK_ID = "fagsakId";
    public static final String KEY_SAKSNUMMER = "saksnummer";
    private static final String KEY_BEHANDLING_TYPE = "behandlingType";

    private EntityManager entityManager;

    @Inject
    public BehandlingRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    BehandlingRepository() {
    }

    EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Hent Behandling med angitt id.
     */
    public Behandling hentBehandling(Long behandlingId) {
        Objects.requireNonNull(behandlingId, "behandlingId"); // NOSONAR
        return hentEksaktResultat(lagBehandlingQuery(behandlingId));
    }

    /**
     * Hent Behandling med angitt UUid.
     */
    public Behandling hentBehandling(UUID uuid) {
        Objects.requireNonNull(uuid, "behandlingUUID"); // NOSONAR
        return hentEksaktResultat(lagBehandlingQuery(uuid));
    }

    public List<Behandling> hentAlleBehandlingerForSaksnummer(Saksnummer saksnummer) {
        Objects.requireNonNull(saksnummer, KEY_SAKSNUMMER);

        var query = getEntityManager().createQuery(
            "SELECT beh from Behandling beh, Fagsak fagsak WHERE beh.fagsak.id=fagsak.id AND fagsak.saksnummer=:saksnummer",
            Behandling.class);
        query.setParameter(KEY_SAKSNUMMER, saksnummer);
        return query.getResultList();
    }

    public Optional<Behandling> finnÅpenTilbakekrevingsbehandling(Saksnummer saksnummer) {
        Objects.requireNonNull(saksnummer, KEY_SAKSNUMMER);
        var query = getEntityManager().createQuery("""
            SELECT beh FROM Behandling beh, Fagsak fagsak WHERE beh.fagsak.id=fagsak.id AND fagsak.saksnummer = :saksnummer
                AND beh.status <>'AVSLU'
                AND beh.behandlingType='BT-007'""", Behandling.class);
        query.setParameter(KEY_SAKSNUMMER, saksnummer);
        return hentUniktResultat(query);
    }

    /**
     * Hent alle behandlinger som ikke er avsluttet på fagsak.
     */
    public List<Behandling> hentBehandlingerSomIkkeErAvsluttetForFagsakId(Long fagsakId) {
        Objects.requireNonNull(fagsakId, KEY_FAGSAK_ID);

        var query = getEntityManager().createQuery(
            "SELECT beh from Behandling beh WHERE beh.fagsak.id = :fagsakId AND beh.status <> :status",
            Behandling.class);
        query.setParameter(KEY_FAGSAK_ID, fagsakId);
        query.setParameter("status", BehandlingStatus.AVSLUTTET);
        query.setHint(HibernateHints.HINT_READ_ONLY, "true");
        return query.getResultList();
    }

    /**
     * Lagrer behandling, sikrer at relevante parent-entiteter (Fagsak, FagsakRelasjon) også oppdateres.
     */
    public Long lagre(Behandling behandling, BehandlingLås lås) {
        if (!Objects.equals(behandling.getId(), lås.getBehandlingId())) {
            // hvis satt må begge være like. (Objects.equals håndterer også at begge er null)
            throw new IllegalArgumentException("Behandling#id og lås#behandlingId må være like, eller begge må være null.");
        }

        long behandlingId = lagre(behandling);
        verifiserBehandlingLås(lås);

        // i tilfelle denne ikke er satt fra før, f.eks. for ny entitet
        lås.setBehandlingId(behandlingId);

        return behandlingId;
    }

    /**
     * Lagrer behandling, og renser first-level cache i JPA
     */
    public Long lagreOgClear(Behandling behandling, BehandlingLås lås) {
        var id = lagre(behandling, lås);
        getEntityManager().clear();
        return id;
    }

    /**
     * Ta lås for oppdatering av behandling/fagsak. Påkrevd før lagring.
     */
    public BehandlingLås taSkriveLås(Behandling behandling) {
        Objects.requireNonNull(behandling, "behandling");
        var låsRepo = new BehandlingLåsRepository(getEntityManager());

        return låsRepo.taLås(behandling.getId());
    }

    // sjekk lås og oppgrader til skriv
    public void verifiserBehandlingLås(BehandlingLås lås) {
        var låsHåndterer = new BehandlingLåsRepository(getEntityManager());
        låsHåndterer.oppdaterLåsVersjon(lås);
    }


    public BehandlingStegType finnBehandlingStegType(String kode) {
        return getEntityManager().find(BehandlingStegType.class, kode);
    }

    /**
     * Hent siste behandling for angitt {@link Fagsak#id} og behandling type
     */
    public Optional<Behandling> hentSisteBehandlingForFagsakId(Long fagsakId, BehandlingType behandlingType) {
        return finnSisteBehandling(fagsakId, behandlingType, false);
    }

    /**
     * Sjekker om versjonen på behandlingen har blitt endret
     */
    public Boolean erVersjonUendret(Long behandlingId, Long versjon) {
        var query = getEntityManager().createNativeQuery(
            "select count(*) from dual " +
                "where exists (select 1 from behandling " +
                "where (behandling.id = ?) " +
                "and (behandling.versjon = ?))");
        query.setParameter(1, behandlingId);
        query.setParameter(2, versjon);
        return ((BigDecimal) query.getSingleResult()).intValue() == 1;
    }

    /**
     * Lager en ny Behandling basert på en gammel, med samme grunnlag strukturer.
     */
    private Optional<Behandling> finnSisteBehandling(Long fagsakId, BehandlingType behandlingType, boolean readOnly) {
        Objects.requireNonNull(fagsakId, KEY_FAGSAK_ID); // NOSONAR
        Objects.requireNonNull(behandlingType, KEY_BEHANDLING_TYPE); // NOSONAR

        var query = getEntityManager().createQuery(
            "from Behandling where fagsak.id=:fagsakId and behandlingType=:behandlingType order by opprettetTidspunkt desc",
            Behandling.class);
        query.setParameter(KEY_FAGSAK_ID, fagsakId);
        query.setParameter(KEY_BEHANDLING_TYPE, behandlingType);
        if (readOnly) {
            query.setHint(HibernateHints.HINT_READ_ONLY, "true");
        }
        return optionalFirst(query.getResultList());
    }

    private TypedQuery<Behandling> lagBehandlingQuery(Long behandlingId) {
        var query = getEntityManager().createQuery("from Behandling where id=:behandlingId", Behandling.class);
        query.setParameter("behandlingId", behandlingId);
        return query;
    }

    private TypedQuery<Behandling> lagBehandlingQuery(UUID behandlingUUID) {
        var query = getEntityManager().createQuery("from Behandling where uuid=:behandlingUUID", Behandling.class);
        query.setParameter("behandlingUUID", behandlingUUID);
        return query;
    }

    Long lagre(Behandling behandling) {
        getEntityManager().persist(behandling);

        var behandlingÅrsaker = behandling.getBehandlingÅrsaker();
        if (!behandlingÅrsaker.isEmpty()) {
            behandlingÅrsaker.forEach(getEntityManager()::persist);
        }

        getEntityManager().flush();
        return behandling.getId();
    }

    private static Optional<Behandling> optionalFirst(List<Behandling> behandlinger) {
        return behandlinger.isEmpty() ? Optional.empty() : Optional.of(behandlinger.get(0));
    }

    public List<Behandling> finnBehandlingerIkkeAvsluttetPåAngittEnhet(String enhetId) {

        var query = entityManager.createQuery(
            "FROM Behandling behandling " +
                "WHERE behandling.status <> :status " +
                "  AND behandling.behandlendeEnhetId = :enhet ",
            Behandling.class);

        query.setParameter("enhet", enhetId);
        query.setParameter("status", BehandlingStatus.AVSLUTTET);
        query.setHint(HibernateHints.HINT_READ_ONLY, "true");
        return query.getResultList();
    }

    public List<Behandling> finnBehandlingerPåVent() {

        var query = entityManager.createQuery("FROM Behandling behandling WHERE behandling.status <> :status ", Behandling.class);

        query.setParameter("status", BehandlingStatus.AVSLUTTET)
            .setHint(HibernateHints.HINT_READ_ONLY, "true");
        return query.getResultList().stream().filter(Behandling::isBehandlingPåVent).toList();
    }

    public void avbrytÅpentAksjonspunktForAvsluttetBehandling() {
        entityManager.createNativeQuery("""
                update aksjonspunkt
                set aksjonspunkt_status = :avbrutt
                where aksjonspunkt_status = :opprettet and behandling_id in (select id from behandling where behandling_status = :avsluttet)
                """)
            .setParameter("avbrutt", AksjonspunktStatus.AVBRUTT.getKode())
            .setParameter("opprettet", AksjonspunktStatus.OPPRETTET.getKode())
            .setParameter("avsluttet", BehandlingStatus.AVSLUTTET.getKode())
            .executeUpdate();
        entityManager.flush();

    }

}
