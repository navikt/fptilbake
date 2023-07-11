package no.nav.foreldrepenger.tilbakekreving.pip;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakStatus;
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
            // FIXME: hardkoder her fagsakstatus for å kunne ha tilbakekreving på fagsak som er avsluttet
            // .....  bør ideelt ha tilpassede regler for fptilbake
            behandlingData.setFagsakstatus(FagsakStatus.UNDER_BEHANDLING.getKode());
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
                , b.ansvarlig_saksbehandler as ansvarligSaksbehandler
                 from behandling b
                 left join fagsak f on f.id = b.fagsak_id
                 left join bruker u on u.id = f.bruker_id
                 where b.id = :behandlingId
                 """;

        // PipBehandlingInfo-mappingen er definert i Behandling entiteten
        Query query = entityManager.createNativeQuery(sql, "PipBehandlingInfo");
        query.setParameter("behandlingId", behandlingId);

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
                , b.ansvarlig_saksbehandler as ansvarligSaksbehandler
                 from behandling b
                 left join fagsak f on f.id = b.fagsak_id
                 left join bruker u on u.id = f.bruker_id
                 where b.uuid = :behandlingUuid
                 """;

        // PipBehandlingInfo-mappingen er definert i Behandling entiteten
        Query query = entityManager.createNativeQuery(sql, "PipBehandlingInfo");
        query.setParameter("behandlingUuid", behandlingUuid);

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
