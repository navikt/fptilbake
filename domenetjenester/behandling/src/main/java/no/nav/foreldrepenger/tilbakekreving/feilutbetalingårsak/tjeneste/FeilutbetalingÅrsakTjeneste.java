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
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertyperDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTyperPrYtelseTypeDto;

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

    public List<HendelseTyperPrYtelseTypeDto> hentFeilutbetalingårsaker() {
        List<HendelseTyperPrYtelseTypeDto> resultat = new ArrayList<>();

        Map<FagsakYtelseType, Set<HendelseType>> hendelseTypePrYtelseType = kodeverkRepository.hentKodeRelasjonForKodeverk(FagsakYtelseType.class, HendelseType.class);
        Map<HendelseType, Set<HendelseUnderType>> hendelseUndertypePrHendelseType = kodeverkRepository.hentKodeRelasjonForKodeverk(HendelseType.class, HendelseUnderType.class);

        for (Map.Entry<FagsakYtelseType, Set<HendelseType>> entry : hendelseTypePrYtelseType.entrySet()) {
            FagsakYtelseType ytelseType = entry.getKey();
            List<HendelseType> hendelseTyper = sortereBasertPåEkstradata(entry.getValue());

            List<HendelseTypeMedUndertyperDto> dtoer = new ArrayList<>();
            for (HendelseType hendelseType : hendelseTyper) {
                Set<HendelseUnderType> undertyper = hendelseUndertypePrHendelseType.get(hendelseType);
                List<HendelseUnderType> sorterteUndertyper = sortereBasertPåEkstradata(undertyper);
                dtoer.add(new HendelseTypeMedUndertyperDto(hendelseType, sorterteUndertyper));
            }
            resultat.add(new HendelseTyperPrYtelseTypeDto(ytelseType, dtoer));
        }

        return resultat;
    }

    private static <T extends Kodeliste> List<T> sortereBasertPåEkstradata(Set<T> kodelistene) {
        Set<T> manglerSortering = kodelistene.stream().filter(h -> h.getEkstraData() == null).collect(Collectors.toSet());
        if (!manglerSortering.isEmpty()) {
            throw new IllegalStateException("Utvikler-feil: mangler sorteringsfelt (settes i ekstra-data), gjelder " + manglerSortering);
        }

        return kodelistene
            .stream()
            .sorted(Comparator.comparing(kodeliste -> Long.valueOf(kodeliste.getEkstraData())))
            .collect(Collectors.toList());
    }

}
