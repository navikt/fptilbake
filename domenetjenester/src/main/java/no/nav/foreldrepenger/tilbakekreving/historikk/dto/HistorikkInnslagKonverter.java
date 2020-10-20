package no.nav.foreldrepenger.tilbakekreving.historikk.dto;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.Historikkinnslag;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.historikk.HistorikkinnslagDokumentLink;

@ApplicationScoped
public class HistorikkInnslagKonverter {

    private AksjonspunktRepository aksjonspunktRepository;

    public HistorikkInnslagKonverter() {// NOSONAR
    }

    @Inject
    public HistorikkInnslagKonverter(AksjonspunktRepository aksjonspunktRepository) {
        this.aksjonspunktRepository = aksjonspunktRepository;
    }

    public HistorikkinnslagDto mapFra(Historikkinnslag historikkinnslag) {
        HistorikkinnslagDto dto = new HistorikkinnslagDto();
        dto.setBehandlingId(historikkinnslag.getBehandlingId());
        List<HistorikkinnslagDelDto> historikkinnslagDeler = HistorikkinnslagDelDto.mapFra(historikkinnslag.getHistorikkinnslagDeler(), aksjonspunktRepository);
        dto.setHistorikkinnslagDeler(historikkinnslagDeler);
        List<HistorikkInnslagDokumentLinkDto> dokumentLinks = mapLenker(historikkinnslag.getDokumentLinker());
        dto.setDokumentLinks(dokumentLinks);
        if (historikkinnslag.getOpprettetAv() != null) {
            dto.setOpprettetAv(medStorBokstav(historikkinnslag.getOpprettetAv()));
        }
        dto.setOpprettetTidspunkt(historikkinnslag.getOpprettetTidspunkt());
        dto.setType(historikkinnslag.getType());
        dto.setAktoer(historikkinnslag.getAktør());
        dto.setKjoenn(historikkinnslag.getKjoenn());
        return dto;
    }

    private List<HistorikkInnslagDokumentLinkDto> mapLenker(List<HistorikkinnslagDokumentLink> lenker) {
        return lenker.stream().map(lenke -> map(lenke)).collect(Collectors.toList());
    }

    private HistorikkInnslagDokumentLinkDto map(HistorikkinnslagDokumentLink lenke) {
        HistorikkInnslagDokumentLinkDto dto = new HistorikkInnslagDokumentLinkDto();
        dto.setTag(lenke.getLinkTekst());
        dto.setUtgått(false);
        dto.setDokumentId(lenke.getDokumentId());
        dto.setJournalpostId(lenke.getJournalpostId().getVerdi());
        return dto;
    }

    private String medStorBokstav(String opprettetAv) {
        return opprettetAv.substring(0, 1).toUpperCase() + opprettetAv.substring(1);
    }
}
