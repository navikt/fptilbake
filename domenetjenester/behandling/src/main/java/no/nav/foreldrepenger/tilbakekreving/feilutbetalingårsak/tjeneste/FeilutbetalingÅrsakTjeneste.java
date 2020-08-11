package no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.tjeneste;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.tilbakekreving.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseTypePrYtelseType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUnderType;
import no.nav.foreldrepenger.tilbakekreving.behandlingslager.feilutbetalingårsak.kodeverk.HendelseUndertypePrHendelseType;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTypeMedUndertyperDto;
import no.nav.foreldrepenger.tilbakekreving.feilutbetalingårsak.dto.HendelseTyperPrYtelseTypeDto;

@ApplicationScoped
public class FeilutbetalingÅrsakTjeneste {

    public List<HendelseTyperPrYtelseTypeDto> hentFeilutbetalingårsaker() {
        List<HendelseTyperPrYtelseTypeDto> resultat = new ArrayList<>();

        Map<FagsakYtelseType, Set<HendelseType>> hendelseTypePrYtelseType = HendelseTypePrYtelseType.getHendelsetypeHierarki();
        Map<HendelseType, Set<HendelseUnderType>> hendelseUndertypePrHendelseType = HendelseUndertypePrHendelseType.getHendelsetypeHierarki();

        for (Map.Entry<FagsakYtelseType, Set<HendelseType>> entry : hendelseTypePrYtelseType.entrySet()) {
            FagsakYtelseType ytelseType = entry.getKey();
            List<HendelseType> hendelseTyper = sortereBasertPåEkstradata2(entry.getValue());

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

    private static List<HendelseUnderType> sortereBasertPåEkstradata(Set<HendelseUnderType> kodelistene) {
        if (kodelistene.contains(null)) {
            throw new IllegalArgumentException("Har null i " + kodelistene);
        }
        return kodelistene
            .stream()
            .sorted(Comparator.comparing(kodeliste -> kodeliste.getSortering()))
            .collect(Collectors.toList());
    }

    private static List<HendelseType> sortereBasertPåEkstradata2(Set<HendelseType> kodelistene) {
        if (kodelistene.contains(null)) {
            throw new IllegalArgumentException("Har null i " + kodelistene);
        }
        return kodelistene
            .stream()
            .sorted(Comparator.comparing(kodeliste -> kodeliste.getSortering()))
            .collect(Collectors.toList());
    }

}
