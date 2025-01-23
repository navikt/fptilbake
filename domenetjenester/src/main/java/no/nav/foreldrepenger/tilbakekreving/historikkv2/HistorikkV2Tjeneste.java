package no.nav.foreldrepenger.tilbakekreving.historikkv2;

import static no.nav.foreldrepenger.tilbakekreving.historikkv2.HistorikkinnslagDtoV2.Linje.linjeskift;
import static no.nav.foreldrepenger.tilbakekreving.historikkv2.HistorikkinnslagDtoV2.Linje.tekstlinje;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkRepositoryOld;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagDokumentLink;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinje;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagOld;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

@ApplicationScoped
public class HistorikkV2Tjeneste {

    private HistorikkRepositoryOld historikkRepositoryOld;
    private BehandlingRepository behandlingRepository;
    private HistorikkinnslagRepository historikkinnslagRepository;

    @Inject
    public HistorikkV2Tjeneste(HistorikkRepositoryOld historikkRepositoryOld, BehandlingRepository behandlingRepository,
                               HistorikkinnslagRepository historikkinnslagRepository) {
        this.historikkRepositoryOld = historikkRepositoryOld;
        this.behandlingRepository = behandlingRepository;
        this.historikkinnslagRepository = historikkinnslagRepository;
    }

    HistorikkV2Tjeneste() {
        //CDI
    }

    public List<HistorikkinnslagDtoV2> hentForSak(long behandlingId) {
        var historikkV1 = historikkRepositoryOld.hentHistorikk(behandlingId);
        var historikkV2 = historikkinnslagRepository.hent(behandlingId);
        return filtrerUtMigrerteHistorikkinnslag(historikkV1, historikkV2, null);
    }

    public List<HistorikkinnslagDtoV2> hentForSak(Saksnummer saksnummer, URI dokumentPath) {
        var historikkV1 = historikkRepositoryOld.hentHistorikkForSaksnummer(saksnummer);
        var historikkV2 = historikkinnslagRepository.hent(saksnummer);
        return filtrerUtMigrerteHistorikkinnslag(historikkV1, historikkV2, dokumentPath);
    }

    private List<HistorikkinnslagDtoV2> filtrerUtMigrerteHistorikkinnslag(List<HistorikkinnslagOld> historikkV1, List<Historikkinnslag> historikkV2, URI dokumentPath) {
        var historikkV1SomIkkeErMigrert = historikkV1.stream()
            .filter(h -> historikkV2.stream().noneMatch(v2 -> Objects.equals(v2.getMigrertFraId(), h.getId())))
            .map(h -> map(dokumentPath, h));
        var nyeHistorikkinnslag = historikkV2.stream().map(h -> map(dokumentPath, h));
        return Stream.concat(historikkV1SomIkkeErMigrert, nyeHistorikkinnslag)
            .sorted(Comparator.comparing(HistorikkinnslagDtoV2::opprettetTidspunkt))
            .toList();
    }

    private HistorikkinnslagDtoV2 map(URI dokumentPath, HistorikkinnslagOld h) {
        var behandlingId = h.getBehandlingId();
        var uuid = behandlingId == null ? null : behandlingRepository.hentBehandling(behandlingId).getUuid();
        return HistorikkV2Adapter.map(h, uuid, dokumentPath);
    }

    private HistorikkinnslagDtoV2 map(URI dokumentPath, Historikkinnslag h) {
        var behandlingId = h.getBehandlingId();
        var uuid = behandlingId == null ? null : behandlingRepository.hentBehandling(behandlingId).getUuid();
        List<HistorikkInnslagDokumentLinkDto> dokumenter = tilDokumentlenker(h.getDokumentLinker(), dokumentPath);
        var linjer = h.getLinjer()
            .stream()
            .sorted(Comparator.comparing(HistorikkinnslagLinje::getSekvensNr))
            .map(t -> t.getType() == HistorikkinnslagLinjeType.TEKST ? tekstlinje(t.getTekst()) : linjeskift())
            .toList();
        return new HistorikkinnslagDtoV2(uuid, HistorikkinnslagDtoV2.HistorikkAktørDto.fra(h.getAktør(), h.getOpprettetAv()), h.getSkjermlenke(),
            h.getOpprettetTidspunkt(), dokumenter, h.getTittel(), linjer);
    }

    private static List<HistorikkInnslagDokumentLinkDto> tilDokumentlenker(List<HistorikkinnslagDokumentLink> dokumentLinker, URI dokumentPath) {
        if (dokumentLinker == null) {
            return List.of();
        }
        return dokumentLinker.stream().map(d -> tilDokumentlenker(d, dokumentPath)) //
            .toList();
    }

    private static HistorikkInnslagDokumentLinkDto tilDokumentlenker(HistorikkinnslagDokumentLink lenke, URI dokumentPath) {
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
