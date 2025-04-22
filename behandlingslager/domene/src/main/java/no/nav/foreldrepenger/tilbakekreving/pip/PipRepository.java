package no.nav.foreldrepenger.tilbakekreving.pip;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.AktørId;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.vedtak.util.LRUCache;

@ApplicationScoped
public class PipRepository {

    private static final LRUCache<String, AktørId> SAK_EIER = new LRUCache<>(500, TimeUnit.MILLISECONDS.convert(4, TimeUnit.HOURS));
    private static final String SAK_EIER_QUERY = """
        SELECT br.aktørId FROM Fagsak fag JOIN Bruker br ON fag.navBruker = br
        WHERE fag.saksnummer = :saksnummer AND br.aktørId IS NOT NULL
        """;
    private static final String BEHANDLING_QUERY = """
        select b.id as behandlingId
        , b.uuid as behandlingUuid
        , f.saksnummer as saksnummer
        , u.aktørId as aktørId
        , b.status as behandlingStatus
        , coalesce(b.ansvarligSaksbehandler, ap.endretAv) as ansvarligSaksbehandler
        from Behandling b
        join Fagsak f on b.fagsak = f
        join Bruker u on f.navBruker = u
        left outer join Aksjonspunkt ap on (ap.behandling = b and ap.aksjonspunktDefinisjon = :foreslå and ap.status = :utført)
        """;

    private EntityManager entityManager;

    PipRepository() {
        // For CDI proxy
    }

    @Inject
    public PipRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<PipBehandlingData> hentBehandlingData(Long behandlingId) {
        return hentInternBehandlingData(behandlingId);
    }

    public Optional<PipBehandlingData> hentBehandlingData(UUID behandlingUuid) {
        return hentInternBehandlingData(behandlingUuid);
    }

    public Optional<AktørId> hentAktørIdSomEierFagsak(String saksnummer) {
        Objects.requireNonNull(saksnummer, "saksnummer");
        return Optional.ofNullable(SAK_EIER.get(saksnummer))
            .or(() -> {
                var eier = entityManager.createQuery(SAK_EIER_QUERY, AktørId.class)
                    .setParameter("saksnummer" , new Saksnummer(saksnummer))
                    .getResultList().stream().findFirst();
                eier.ifPresent(e -> SAK_EIER.put(saksnummer, e));
                return eier;
            });

    }

    private Optional<PipBehandlingData> hentInternBehandlingData(long behandlingId) {
        var sql = BEHANDLING_QUERY + "where b.id = :behandlingId";

        var resultater = entityManager.createQuery(sql, PipBehandlingData.class)
            .setParameter("behandlingId", behandlingId)
            .setParameter("foreslå", AksjonspunktDefinisjon.FORESLÅ_VEDTAK)
            .setParameter("utført", AksjonspunktStatus.UTFØRT)
            .getResultList();

        return sjekkResultat(resultater, String.valueOf(behandlingId));
    }

    private Optional<PipBehandlingData> hentInternBehandlingData(UUID behandlingUuid) {
        var sql = BEHANDLING_QUERY + "where b.uuid = :behandlingUuid";

        var resultater = entityManager.createQuery(sql, PipBehandlingData.class)
            .setParameter("behandlingUuid", behandlingUuid)
            .setParameter("foreslå", AksjonspunktDefinisjon.FORESLÅ_VEDTAK)
            .setParameter("utført", AksjonspunktStatus.UTFØRT)
            .getResultList();

        return sjekkResultat(resultater, behandlingUuid.toString());
    }

    private Optional<PipBehandlingData> sjekkResultat(List<PipBehandlingData> resultater, String behandlingRef) {
        if (resultater.isEmpty()) {
            return Optional.empty();
        } else if (resultater.size() == 1) {
            return Optional.of(resultater.getFirst());
        } else {
            throw new IllegalStateException("Utvikler feil: Forventet 0 eller 1 treff etter søk på behandling, fikk "
                + resultater.size() + " [behandling: " + behandlingRef + "]");
        }
    }
}
