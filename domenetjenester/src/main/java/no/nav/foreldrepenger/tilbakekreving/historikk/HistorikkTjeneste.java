package no.nav.foreldrepenger.tilbakekreving.historikk;

import static no.nav.foreldrepenger.tilbakekreving.historikk.HistorikkinnslagDto.Linje.linjeskift;
import static no.nav.foreldrepenger.tilbakekreving.historikk.HistorikkinnslagDto.Linje.tekstlinje;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagDokumentLink;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinje;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagLinjeType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagRepository;
import no.nav.foreldrepenger.tilbakekreving.domene.typer.Saksnummer;

@ApplicationScoped
public class HistorikkTjeneste {

    private BehandlingRepository behandlingRepository;
    private HistorikkinnslagRepository historikkinnslagRepository;

    @Inject
    public HistorikkTjeneste(BehandlingRepository behandlingRepository,
                             HistorikkinnslagRepository historikkinnslagRepository) {
        this.behandlingRepository = behandlingRepository;
        this.historikkinnslagRepository = historikkinnslagRepository;
    }

    HistorikkTjeneste() {
        //CDI
    }

    public List<HistorikkinnslagDto> hentForSak(long behandlingId) {
        return historikkinnslagRepository.hent(behandlingId).stream()
                .map(this::map)
                .toList();
    }

    public List<HistorikkinnslagDto> hentForSak(Saksnummer saksnummer) {
        return historikkinnslagRepository.hent(saksnummer).stream()
                .map(this::map)
                .toList();
    }

    private HistorikkinnslagDto map(Historikkinnslag h) {
        var behandlingId = h.getBehandlingId();
        var uuid = behandlingId == null ? null : behandlingRepository.hentBehandling(behandlingId).getUuid();
        List<HistorikkInnslagDokumentLinkDto> dokumenter = tilDokumentlenker(h.getDokumentLinker());
        var linjer = h.getLinjer()
            .stream()
            .sorted(Comparator.comparing(HistorikkinnslagLinje::getSekvensNr))
            .map(t -> t.getType() == HistorikkinnslagLinjeType.TEKST ? tekstlinje(t.getTekst()) : linjeskift())
            .toList();
        return new HistorikkinnslagDto(uuid, HistorikkinnslagDto.HistorikkAktørDto.fra(h.getAktør(), h.getOpprettetAv()), h.getSkjermlenke(),
            h.getOpprettetTidspunkt(), dokumenter, h.getTittel(), linjer);
    }

    private List<HistorikkInnslagDokumentLinkDto> tilDokumentlenker(List<HistorikkinnslagDokumentLink> dokumentLinker) {
        if (dokumentLinker == null) {
            return List.of();
        }
        return dokumentLinker.stream()
            .map(this::tilDokumentlenke)
            .toList();
    }

    private HistorikkInnslagDokumentLinkDto tilDokumentlenke(HistorikkinnslagDokumentLink lenke) {
        var dto = new HistorikkInnslagDokumentLinkDto();
        dto.setTag(lenke.getLinkTekst());
        dto.setUtgått(false);
        dto.setDokumentId(lenke.getDokumentId());
        dto.setJournalpostId(lenke.getJournalpostId().getVerdi());
        return dto;
    }

}
