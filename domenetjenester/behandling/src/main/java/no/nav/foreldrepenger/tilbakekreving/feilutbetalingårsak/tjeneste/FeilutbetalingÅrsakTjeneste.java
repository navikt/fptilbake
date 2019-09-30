package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.FeiltubetalingÅrsakerYtelseTypeDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.FeilutbetalingÅrsakDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.UnderÅrsakDto;

@ApplicationScoped
public class FeilutbetalingÅrsakTjeneste {

    private KodeverkRepository kodeverkRepository;

    FeilutbetalingÅrsakTjeneste() {
        // For CDI
    }

    @Inject
    public FeilutbetalingÅrsakTjeneste(KodeverkRepository kodeverkRepository) {
        this.kodeverkRepository = kodeverkRepository;
    }

    public List<FeiltubetalingÅrsakerYtelseTypeDto> hentFeilutbetalingårsaker() {
        List<FeiltubetalingÅrsakerYtelseTypeDto> resultat = new ArrayList<>();

        Map<FagsakYtelseType, Set<HendelseType>> hendelseTypePrYtelseType = kodeverkRepository.hentKodeRelasjonForKodeverk(FagsakYtelseType.class, HendelseType.class);
        Map<HendelseType, Set<HendelseUnderType>> hendelseUndertypePrHendelseType = kodeverkRepository.hentKodeRelasjonForKodeverk(HendelseType.class, HendelseUnderType.class);

        for (Map.Entry<FagsakYtelseType, Set<HendelseType>> entry : hendelseTypePrYtelseType.entrySet()) {
            FagsakYtelseType ytelseType = entry.getKey();
            Set<HendelseType> hendelseTyper = entry.getValue();

            List<FeilutbetalingÅrsakDto> dtoer = new ArrayList<>();
            for (HendelseType hendelseType : hendelseTyper) {
                FeilutbetalingÅrsakDto feilutbetalingÅrsakDto = new FeilutbetalingÅrsakDto();
                feilutbetalingÅrsakDto.setÅrsakKode(hendelseType.getKode());
                feilutbetalingÅrsakDto.setÅrsak(hendelseType.getNavn());
                feilutbetalingÅrsakDto.setKodeverk(hendelseType.getKodeverk());
                if (hendelseUndertypePrHendelseType.containsKey(hendelseType)) {
                    // sortere basert på forhåndsdefinert rekkefølge
                    List<HendelseUnderType> hendelseUnderTyper = sortereHendelseUnderTyper(hendelseUndertypePrHendelseType.get(hendelseType));
                    for (HendelseUnderType hendelseUnderType : hendelseUnderTyper) {
                        feilutbetalingÅrsakDto.leggTilUnderÅrsaker(new UnderÅrsakDto(hendelseUnderType.getNavn(), hendelseUnderType.getKode(), hendelseUnderType.getKodeverk()));
                    }
                }
                dtoer.add(feilutbetalingÅrsakDto);
            }

            resultat.add(new FeiltubetalingÅrsakerYtelseTypeDto(ytelseType, dtoer));
        }

        return resultat;
    }

    private List<HendelseUnderType> sortereHendelseUnderTyper(Set<HendelseUnderType> hendelseUnderTyper) {
        Set<HendelseUnderType> manglerSortering = hendelseUnderTyper.stream().filter(h -> h.getEkstraData() == null).collect(Collectors.toSet());
        if (!manglerSortering.isEmpty()) {
            throw new IllegalStateException("Utvikler-feil: hendelse-undertype mangler sorteringsfelt (settes i ekstra-data), gjelder " + manglerSortering);
        }

        return hendelseUnderTyper
            .stream()
            .sorted(Comparator.comparing(hendelseUnderType -> Long.valueOf(hendelseUnderType.getEkstraData())))
            .collect(Collectors.toList());
    }

}
