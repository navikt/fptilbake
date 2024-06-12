package no.nav.foreldrepenger.tilbakekreving.pip;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktKodeDefinisjon;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.BehandlingInfo;

@ApplicationScoped
public class PipRepository {

    private EntityManager entityManager;

    PipRepository() {
        // For CDI proxy
    }

    @Inject
    public PipRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<PipBehandlingData> hentBehandlingData(Long behandlingId) {
        Optional<BehandlingInfo> internBehandlingData = hentInternBehandlingData(behandlingId);
        return getPipBehandlingData(internBehandlingData);
    }

    public Optional<PipBehandlingData> hentBehandlingData(UUID behandlingUuid) {
        Optional<BehandlingInfo> internBehandlingData = hentInternBehandlingData(behandlingUuid);
        return getPipBehandlingData(internBehandlingData);
    }

    private Optional<PipBehandlingData> getPipBehandlingData(Optional<BehandlingInfo> internBehandlingData) {
        if (internBehandlingData.isPresent()) {
            BehandlingInfo internData = internBehandlingData.get();

            PipBehandlingData behandlingData = new PipBehandlingData();
            behandlingData.setBehandlingId(internData.getBehandlingId());
            behandlingData.setAnsvarligSaksbehandler(internData.getAnsvarligSaksbehandler());
            behandlingData.setStatusForBehandling(internData.getBehandlingStatus());
            behandlingData.setSaksnummer(internData.getSaksnummer());
            behandlingData.leggTilAktørId(internData.getAktørId());
            return Optional.of(behandlingData);
        }
        return Optional.empty();
    }

    private Optional<BehandlingInfo> hentInternBehandlingData(long behandlingId) {
        String sql = """
                select b.id as behandlingId
                , f.saksnummer as saksnummer
                , u.aktoer_id as aktørId
                , b.behandling_status as behandlingstatus
                , coalesce(b.ansvarlig_saksbehandler, ap.endret_av) as ansvarligSaksbehandler
                 from behandling b
                 left join fagsak f on f.id = b.fagsak_id
                 left join bruker u on u.id = f.bruker_id
                 left join aksjonspunkt ap on (ap.behandling_id = b.id and aksjonspunkt_def = :foreslå and aksjonspunkt_status = :utført)
                 where b.id = :behandlingId
                 """;

        // PipBehandlingInfo-mappingen er definert i Behandling entiteten
        Query query = entityManager.createNativeQuery(sql, "PipBehandlingInfo")
            .setParameter("behandlingId", behandlingId)
            .setParameter("foreslå", AksjonspunktKodeDefinisjon.FORESLÅ_VEDTAK)
            .setParameter("utført", AksjonspunktStatus.UTFØRT.getKode());

        List resultater = query.getResultList();
        if (resultater.isEmpty()) {
            return Optional.empty();
        } else if (resultater.size() == 1) {
            return Optional.of((BehandlingInfo) resultater.get(0));
        } else {
            throw new IllegalStateException("Utvikler feil: Forventet 0 eller 1 treff etter søk på behandlingId, fikk "
                    + resultater.size() + " [behandlingId: " + behandlingId);
        }
    }

    private Optional<BehandlingInfo> hentInternBehandlingData(UUID behandlingUuid) {
        String sql = """
                select b.id as behandlingId
                , f.saksnummer as saksnummer
                , u.aktoer_id as aktørId
                , b.behandling_status as behandlingstatus
                , coalesce(b.ansvarlig_saksbehandler, ap.endret_av) as ansvarligSaksbehandler
                 from behandling b
                 left join fagsak f on f.id = b.fagsak_id
                 left join bruker u on u.id = f.bruker_id
                 left join aksjonspunkt ap on (ap.behandling_id = b.id and aksjonspunkt_def = :foreslå and aksjonspunkt_status = :utført)
                 where b.uuid = :behandlingUuid
                 """;

        // PipBehandlingInfo-mappingen er definert i Behandling entiteten
        Query query = entityManager.createNativeQuery(sql, "PipBehandlingInfo")
            .setParameter("behandlingUuid", behandlingUuid)
            .setParameter("foreslå", AksjonspunktKodeDefinisjon.FORESLÅ_VEDTAK)
            .setParameter("utført", AksjonspunktStatus.UTFØRT.getKode());

        List resultater = query.getResultList();
        if (resultater.isEmpty()) {
            return Optional.empty();
        } else if (resultater.size() == 1) {
            return Optional.of((BehandlingInfo) resultater.get(0));
        } else {
            throw new IllegalStateException("Utvikler feil: Forventet 0 eller 1 treff etter søk på behandlingId, fikk "
                    + resultater.size() + " [behandlingUuid: " + behandlingUuid);
        }
    }
}
