package no.nav.foreldrepenger.tilbakekreving.historikkv2;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;

import jakarta.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepository;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag2;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag2DokumentLink;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag2Linje;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag2Repository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeType;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;
import no.nav.foreldrepenger.tilbakekreving.historikk.dto.HistorikkInnslagDokumentLinkDto;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static no.nav.foreldrepenger.tilbakekreving.historikkv2.HistorikkinnslagDtoV2.Linje.*;

@ApplicationScoped
public class HistorikkV2Tjeneste {

    private HistorikkRepository historikkRepository;
    private BehandlingRepository behandlingRepository;
    private Historikkinnslag2Repository historikkinnslag2Repository;

    @Inject
    public HistorikkV2Tjeneste(HistorikkRepository historikkRepository, BehandlingRepository behandlingRepository,
                               Historikkinnslag2Repository historikkinnslag2Repository) {
        this.historikkRepository = historikkRepository;
        this.behandlingRepository = behandlingRepository;
        this.historikkinnslag2Repository = historikkinnslag2Repository;
    }

    HistorikkV2Tjeneste() {
        //CDI
    }

    public List<HistorikkinnslagDtoV2> hentForSak(long behandlingId, URI dokumentPath) {
        var historikkV1 = historikkRepository.hentHistorikk(behandlingId).stream().map(h -> map(dokumentPath, h));
        var historikkV2 = historikkinnslag2Repository.hent(behandlingId).stream().map(h -> map(dokumentPath, h));
        return Stream.concat(historikkV1, historikkV2).sorted(Comparator.comparing(HistorikkinnslagDtoV2::opprettetTidspunkt)).toList();
    }

    public List<HistorikkinnslagDtoV2> hentForSak(Saksnummer saksnummer, URI dokumentPath) {
        var historikkV1 = historikkRepository.hentHistorikkForSaksnummer(saksnummer).stream().map(h -> map(dokumentPath, h));
        var historikkV2 = historikkinnslag2Repository.hent(saksnummer).stream().map(h -> map(dokumentPath, h));

        return Stream.concat(historikkV1, historikkV2).sorted(Comparator.comparing(HistorikkinnslagDtoV2::opprettetTidspunkt)).toList();
    }

    private HistorikkinnslagDtoV2 map(URI dokumentPath, Historikkinnslag h) {
        var behandlingId = h.getBehandlingId();
        var uuid = behandlingId == null ? null : behandlingRepository.hentBehandling(behandlingId).getUuid();
        return HistorikkV2Adapter.map(h, uuid, dokumentPath);
    }

    private HistorikkinnslagDtoV2 map(URI dokumentPath, Historikkinnslag2 h) {
        var behandlingId = h.getBehandlingId();
        var uuid = behandlingId == null ? null : behandlingRepository.hentBehandling(behandlingId).getUuid();
        List<HistorikkInnslagDokumentLinkDto> dokumenter = tilDokumentlenker(h.getDokumentLinker(), dokumentPath);
        var linjer = h.getLinjer()
            .stream()
            .sorted(Comparator.comparing(Historikkinnslag2Linje::getSekvensNr))
            .map(t -> t.getType() == HistorikkinnslagLinjeType.TEKST ? tekstlinje(t.getTekst()) : linjeskift())
            .toList();
        return new HistorikkinnslagDtoV2(uuid, HistorikkinnslagDtoV2.HistorikkAktørDto.fra(h.getAktør(), h.getOpprettetAv()), h.getSkjermlenke(),
            h.getOpprettetTidspunkt(), dokumenter, h.getTittel(), linjer);
    }

    private static List<HistorikkInnslagDokumentLinkDto> tilDokumentlenker(List<Historikkinnslag2DokumentLink> dokumentLinker, URI dokumentPath) {
        if (dokumentLinker == null) {
            return List.of();
        }
        return dokumentLinker.stream().map(d -> tilDokumentlenker(d, dokumentPath)) //
            .toList();
    }

    private static HistorikkInnslagDokumentLinkDto tilDokumentlenker(Historikkinnslag2DokumentLink lenke, URI dokumentPath) {
        var dto = new HistorikkInnslagDokumentLinkDto();
        dto.setTag(lenke.getLinkTekst());
        dto.setUtgått(false);
        dto.setDokumentId(lenke.getDokumentId());
        dto.setJournalpostId(lenke.getJournalpostId().getVerdi());
        if (lenke.getJournalpostId().getVerdi() != null && lenke.getDokumentId() != null && dokumentPath != null) {
            var builder = UriBuilder.fromUri(dokumentPath)
                .queryParam("journalpostId", lenke.getJournalpostId().getVerdi())
                .queryParam("dokumentId", lenke.getDokumentId());
            dto.setUrl(builder.build());
        }
        return dto;
    }

}
