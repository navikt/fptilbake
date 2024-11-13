package no.nav.foreldrepenger.tilbakekreving.historikkv2;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;

import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class HistorikkV2Tjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(HistorikkV2Tjeneste.class);

    private HistorikkRepository historikkRepository;
    private BehandlingRepository behandlingRepository;

    @Inject
    public HistorikkV2Tjeneste(HistorikkRepository historikkRepository, BehandlingRepository behandlingRepository) {
        this.historikkRepository = historikkRepository;
        this.behandlingRepository = behandlingRepository;
    }

    HistorikkV2Tjeneste() {
        //CDI
    }

    public List<HistorikkinnslagDtoV2> hentForSak(Saksnummer saksnummer, URI dokumentPath) {
        try {
            var historikkinnslag = historikkRepository.hentHistorikkForSaksnummer(saksnummer);
            return historikkinnslag
                .stream()
                .map(h -> {
                    var behandlingId = h.getBehandlingId();
                    var uuid = behandlingId == null ? null : behandlingRepository.hentBehandling(behandlingId).getUuid();
                    return HistorikkV2Adapter.map(h, uuid, dokumentPath);
                })
                .sorted(Comparator.comparing(HistorikkinnslagDtoV2::opprettetTidspunkt))
                .toList();
        } catch (Exception e) {
            LOG.warn("Ny historikktjeneste feilet", e);
            return List.of();
        }
    }

}
