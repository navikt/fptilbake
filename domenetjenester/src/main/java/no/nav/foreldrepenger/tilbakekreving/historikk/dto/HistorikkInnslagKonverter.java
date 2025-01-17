package no.nav.foreldrepenger.tilbakekreving.historikk.dto;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagOld;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagOldDokumentLink;

@ApplicationScoped
public class HistorikkInnslagKonverter {

    private BehandlingRepository behandlingRepository;

    @Inject
    public HistorikkInnslagKonverter(BehandlingRepository behandlingRepository) {
        this.behandlingRepository = behandlingRepository;
    }

    public HistorikkInnslagKonverter() {// NOSONAR
    }

    public HistorikkinnslagDto mapFra(HistorikkinnslagOld historikkinnslag, URI dokumentPath) {
        HistorikkinnslagDto dto = new HistorikkinnslagDto();
        if (historikkinnslag.getBehandlingId() != null) {
            dto.setBehandlingId(historikkinnslag.getBehandlingId());
            dto.setBehandlingUuid(behandlingRepository.hentBehandling(historikkinnslag.getBehandlingId()).getUuid());
        }
        List<HistorikkinnslagDelDto> historikkinnslagDeler = HistorikkinnslagDelDto.mapFra(historikkinnslag.getHistorikkinnslagDeler());
        dto.setHistorikkinnslagDeler(historikkinnslagDeler);
        List<HistorikkInnslagDokumentLinkDto> dokumentLinks = mapLenker(historikkinnslag.getDokumentLinker(), dokumentPath);
        dto.setDokumentLinks(dokumentLinks);
        if (historikkinnslag.getOpprettetAv() != null) {
            dto.setOpprettetAv(medStorBokstav(historikkinnslag.getOpprettetAv()));
        }
        dto.setOpprettetTidspunkt(historikkinnslag.getOpprettetTidspunkt());
        dto.setType(historikkinnslag.getType());
        dto.setAktoer(historikkinnslag.getAktør());
        return dto;
    }

    private List<HistorikkInnslagDokumentLinkDto> mapLenker(List<HistorikkinnslagOldDokumentLink> lenker, URI dokumentPath) {
        return lenker.stream().map(lenke -> map(lenke, dokumentPath)).collect(Collectors.toList());
    }

    private HistorikkInnslagDokumentLinkDto map(HistorikkinnslagOldDokumentLink lenke, URI dokumentPath) {
        HistorikkInnslagDokumentLinkDto dto = new HistorikkInnslagDokumentLinkDto();
        dto.setTag(lenke.getLinkTekst());
        dto.setUtgått(false);
        dto.setDokumentId(lenke.getDokumentId());
        dto.setJournalpostId(lenke.getJournalpostId().getVerdi());
        dto.setUrl(UriBuilder.fromUri(dokumentPath).queryParam("journalpostId", lenke.getJournalpostId().getVerdi()).queryParam("dokumentId", lenke.getDokumentId()).build());
        return dto;
    }

    private String medStorBokstav(String opprettetAv) {
        return opprettetAv.substring(0, 1).toUpperCase() + opprettetAv.substring(1);
    }
}
